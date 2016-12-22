/*
 * Copyright (c) 2016 Adrian Videnie
 *
 * This file is part of Expenses.
 *
 * Expenses is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Expenses is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Expenses. If not, see <http://www.gnu.org/licenses/>.
 */

package ro.expectations.expenses.restore;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;
import ro.expectations.expenses.model.Account;
import ro.expectations.expenses.model.AccountType;
import ro.expectations.expenses.model.CardType;
import ro.expectations.expenses.model.Category;
import ro.expectations.expenses.model.OnlineAccountType;
import ro.expectations.expenses.model.Payee;
import ro.expectations.expenses.model.Project;
import ro.expectations.expenses.model.Transaction;
import ro.expectations.expenses.model.TransactionSplit;

public class FinancistoImportIntentService extends AbstractRestoreIntentService {

    public static final String ACTION_SUCCESS = "FinancistoImportIntentService.ACTION_SUCCESS";
    public static final String ACTION_FAILURE = "FinancistoImportIntentService.ACTION_FAILURE";

    private static final String TAG = FinancistoImportIntentService.class.getSimpleName();

    private final Map<String, String> mCurrencies = new ArrayMap<>();
    private final List<Bundle> mAccounts = new ArrayList<>();
    private final List<Bundle> mPayees = new ArrayList<>();
    private final List<Bundle> mProjects = new ArrayList<>();
    private final List<Bundle> mCategories = new ArrayList<>();
    private final SparseIntArray mMigrateCategories = new SparseIntArray();
    private final List<Bundle> mTransactions = new ArrayList<>();
    private final SparseArray<List<Bundle>> mTransactionSplits = new SparseArray<>();

    @Override
    protected void process(InputStream input) throws IOException {

        InputStreamReader reader = new InputStreamReader(input, "UTF-8");
        try (BufferedReader br = new BufferedReader(reader, 65535)) {
            boolean insideEntity = false;
            Map<String, String> values = new ArrayMap<>();
            String line;
            String tableName = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("$")) {
                    if ("$$".equals(line)) {
                        if (tableName != null && values.size() > 0) {
                            processEntry(tableName, values);
                            tableName = null;
                            insideEntity = false;
                        }
                    } else {
                        int i = line.indexOf(":");
                        if (i > 0) {
                            tableName = line.substring(i + 1);
                            insideEntity = true;
                            values.clear();
                        }
                    }
                } else {
                    if (insideEntity) {
                        int i = line.indexOf(":");
                        if (i > 0) {
                            String columnName = line.substring(0, i);
                            String value = line.substring(i + 1);
                            values.put(columnName, value);
                        }
                    }
                }
            }
        }
        Log.i(TAG, "Finished parsing Financisto backup file");

        processCategoryEntries();
        processAccountEntries();
        processPayeeEntries();
        processProjectEntries();
        processTransactionEntries();
        Log.i(TAG, "Finished processing backup file");
    }

    @Override
    protected void notifySuccess() {
        Intent successIntent = new Intent(ACTION_SUCCESS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(successIntent);
    }

    @Override
    protected void notifyFailure(Exception e) {
        Intent failureIntent = new Intent(ACTION_FAILURE);
        failureIntent.putExtra("exception", e);
        LocalBroadcastManager.getInstance(this).sendBroadcast(failureIntent);
    }

    private void processEntry(String tableName, Map<String, String> values) {
        switch(tableName) {
            case "category":
                processCategoryEntry(values);
                break;
            case "currency":
                processCurrencyEntry(values);
                break;
            case "account":
                processAccountEntry(values);
                break;
            case "payee":
                processPayeeEntry(values);
                break;
            case "project":
                processProjectEntry(values);
                break;
            case "transactions":
                if (!values.containsKey("is_template") || Integer.parseInt(values.get("is_template")) == 0) {
                    processTransactionEntry(values);
                }
                break;
        }
    }

    private void processCategoryEntry(Map<String, String> values) {

        Bundle categoryValues = new Bundle();
        categoryValues.putInt("id", Integer.parseInt(values.get("_id")));
        categoryValues.putString("name", values.get("title"));
        categoryValues.putInt("left", Integer.valueOf(values.get("left")));
        categoryValues.putInt("right", Integer.valueOf(values.get("right")));

        mCategories.add(categoryValues);
    }

    private void processCurrencyEntry(Map<String, String> values) {
        String currencyCode = values.get("name");
        try {
            Currency currency = Currency.getInstance(currencyCode);
            mCurrencies.put(values.get("_id"), currency.getCurrencyCode());
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Could not find currency for currency code '" + currencyCode + "', will default to EUR");
        }
    }

    private void processAccountEntry(Map<String, String> values) {
        AccountType type;
        CardType cardType = null;
        OnlineAccountType onlineAccountType = null;

        switch (values.get("type")) {
            case "CASH":
                type = AccountType.CASH;
                break;
            case "BANK":
                type = AccountType.BANK;
                break;
            case "DEBIT_CARD":
                type = AccountType.DEBIT_CARD;
                cardType = getCardType(values.get("card_issuer"));
                break;
            case "CREDIT_CARD":
                type = AccountType.CREDIT_CARD;
                cardType = getCardType(values.get("card_issuer"));
                break;
            case "ASSET":
                type = AccountType.SAVINGS;
                break;
            case "LIABILITY":
                type = AccountType.LOAN;
                break;
            case "ONLINE":
                type = AccountType.ONLINE;
                onlineAccountType = getOnlineAccountType(values.get("card_issuer"));
                break;
            case "PAYPAL":
                type = AccountType.ONLINE;
                onlineAccountType = OnlineAccountType.PAYPAL;
                break;
            case "OTHER":
            default:
                type = AccountType.OTHER;
                break;
        }

        Bundle accountValues = new Bundle();
        accountValues.putInt("id", Integer.parseInt(values.get("_id")));
        accountValues.putString("title", values.get("title"));
        accountValues.putString("currency", values.get("currency_id"));
        accountValues.putLong("balance", Long.parseLong(values.get("total_amount")));
        accountValues.putString("type", type.name());
        if (cardType != null) {
            accountValues.putString("card_type", cardType.name());
        }
        if (onlineAccountType != null) {
            accountValues.putString("online_account_type", onlineAccountType.name());
        }
        accountValues.putBoolean("is_active", Integer.parseInt(values.get("is_active")) == 1);
        accountValues.putBoolean("include_into_totals", Integer.parseInt(values.get("is_include_into_totals")) == 1);
        accountValues.putInt("sort_order", Integer.parseInt(values.get("sort_order")));
        accountValues.putString("note", values.get("note"));
        accountValues.putLong("created_at", Long.parseLong(values.get("creation_date")));

        mAccounts.add(accountValues);
    }

    private CardType getCardType(String cardType) {
        switch(cardType) {
            case "VISA":
                return CardType.VISA;
            case "VISA_ELECTRON":
                return CardType.VISA_ELECTRON;
            case "MASTERCARD":
                return CardType.MASTERCARD;
            case "MAESTRO":
                return CardType.MAESTRO;
            case "CIRRUS":
                return CardType.CIRRUS;
            case "AMEX":
                return CardType.AMERICAN_EXPRESS;
            case "JCB":
                return CardType.JCB;
            case "DINERS":
                return CardType.DINERS;
            case "DISCOVER":
                return CardType.DISCOVER;
            case "UNIONPAY":
                return CardType.UNIONPAY;
            case "EPS":
                return CardType.EPS;
            case "NETS":
            default:
                return CardType.OTHER;
        }
    }

    private OnlineAccountType getOnlineAccountType(String onlineAccountType) {
        switch(onlineAccountType) {
            case "PAYPAL":
                return OnlineAccountType.PAYPAL;
            case "AMAZON":
                return OnlineAccountType.AMAZON;
            case "GOOGLE_WALLET":
                return OnlineAccountType.GOOGLE_WALLET;
            default:
                return OnlineAccountType.OTHER;
        }
    }

    private void processPayeeEntry(Map<String, String> values) {

        Bundle payeeValues = new Bundle();
        payeeValues.putInt("id", Integer.parseInt(values.get("_id")));
        payeeValues.putString("name", values.get("title"));
        payeeValues.putInt("last_category_id", Integer.parseInt(values.get("last_category_id")));

        mPayees.add(payeeValues);
    }

    private void processProjectEntry(Map<String, String> values) {

        Bundle projectValues = new Bundle();
        int projectId = Integer.parseInt(values.get("_id"));
        if (projectId > 0) {
            projectValues.putInt("id", projectId);
            projectValues.putString("title", values.get("title"));
            projectValues.putBoolean("is_active", Integer.parseInt(values.get("is_active")) == 1);
            projectValues.putLong("updated_at", Long.parseLong(values.get("updated_on")));

            mProjects.add(projectValues);
        }
    }

    private void processTransactionEntry(Map<String, String> values) {

        Bundle transactionBundle = new Bundle();

        int id = Integer.parseInt(values.get("_id"));
        int fromAccountId = Integer.parseInt(values.get("from_account_id"));
        long fromAmount = Long.parseLong(values.get("from_amount"));
        int toAccountId = Integer.parseInt(values.get("to_account_id"));
        long toAmount = Long.parseLong(values.get("to_amount"));

        String parentId = values.get("parent_id");
        if (parentId != null && !parentId.equals("0")) {

            if (toAccountId > 0) {

                Map<String, String> newValues = new HashMap<>(values);
                newValues.put("parent_id", "0");
                processTransactionEntry(newValues);

            } else {

                Bundle splitBundle = new Bundle();
                splitBundle.putInt("id", id);
                splitBundle.putLong("amount", 0 - fromAmount);

                if (values.containsKey("category_id")) {
                    int categoryId = Integer.parseInt(values.get("category_id"));
                    if (categoryId > 0) {
                        splitBundle.putInt("category_id", categoryId);
                    }
                }

                if (values.containsKey("project_id")) {
                    int projectId = Integer.parseInt(values.get("project_id"));
                    if (projectId > 0) {
                        splitBundle.putInt("project_id", projectId);
                    }
                }

                splitBundle.putString("note", values.get("note"));

                int key = Integer.parseInt(parentId);
                List<Bundle> transactionSplits = mTransactionSplits.get(key, null);
                if (transactionSplits == null) {
                    transactionSplits = new ArrayList<>();
                    mTransactionSplits.put(key, transactionSplits);
                }
                transactionSplits.add(splitBundle);
            }

        } else {

            transactionBundle.putInt("id", id);

            if (toAccountId > 0) {
                transactionBundle.putInt("from_account_id", fromAccountId);
                transactionBundle.putLong("from_amount", 0 - fromAmount);
                transactionBundle.putInt("to_account_id", toAccountId);
                transactionBundle.putLong("to_amount", toAmount);
            } else {
                if (fromAmount > 0) {
                    transactionBundle.putInt("to_account_id", fromAccountId);
                    transactionBundle.putLong("to_amount", fromAmount);
                } else {
                    transactionBundle.putInt("from_account_id", fromAccountId);
                    transactionBundle.putLong("from_amount", 0 - fromAmount);
                }
            }

            if (values.containsKey("category_id")) {
                int categoryId = Integer.parseInt(values.get("category_id"));
                if (categoryId > 0) {
                    transactionBundle.putInt("category_id", categoryId);
                }
            }

            if (values.containsKey("payee_id")) {
                int payeeId = Integer.parseInt(values.get("payee_id"));
                if (payeeId > 0) {
                    transactionBundle.putInt("payee_id", payeeId);
                }
            }

            if (values.containsKey("project_id")) {
                int projectId = Integer.parseInt(values.get("project_id"));
                if (projectId > 0) {
                    transactionBundle.putInt("project_id", projectId);
                }
            }

            transactionBundle.putString("note", values.get("note"));

            String originalFromCurrencyId = values.get("original_currency_id");
            if (originalFromCurrencyId != null && !originalFromCurrencyId.isEmpty() && !originalFromCurrencyId.equals("0")) {
                transactionBundle.putString("original_currency", originalFromCurrencyId);
                long originalFromAmount = Long.parseLong(values.get("original_from_amount"));
                transactionBundle.putLong("original_amount", originalFromAmount);
            }

            long createdAt = Long.parseLong(values.get("datetime"));
            transactionBundle.putLong("occurred_at", createdAt);
            transactionBundle.putLong("cleared_at", createdAt);
            long updatedAt = Long.parseLong(values.get("updated_on"));
            if (updatedAt > 1) {
                transactionBundle.putLong("updated_at", updatedAt);
            }

            mTransactions.add(transactionBundle);
        }
    }

    private void processCategoryEntries() {

        List<Bundle> parentCategories = new ArrayList<>();
        List<Bundle> childCategories = new ArrayList<>();

        for (Bundle currentCategoryBundle : mCategories) {
            long id = currentCategoryBundle.getInt("id");
            if (id <= 0) {
                continue;
            }

            int parentId = 0;
            int left = currentCategoryBundle.getInt("left");
            int previousLeft = 0;
            int level = 0;

            for(Bundle categoryBundle : mCategories) {
                int currentId = categoryBundle.getInt("id");
                int currentLeft = categoryBundle.getInt("left");
                int currentRight = categoryBundle.getInt("right");

                if (currentLeft < left && left < currentRight) {
                    if (currentLeft > previousLeft) {
                        if (level <= 1) {
                            parentId = currentId;
                        }
                        previousLeft = currentLeft;
                        level++;
                    }
                }
            }

            if (level >= 2) {
                mMigrateCategories.put(currentCategoryBundle.getInt("id"), parentId);
            } else {
                currentCategoryBundle.putInt("parent_id", parentId);
                if (parentId == 0) {
                    parentCategories.add(currentCategoryBundle);
                } else {
                    childCategories.add(currentCategoryBundle);
                }
            }
        }

        // sort parent categories for best color distribution
        Collections.sort(parentCategories, new Comparator<Bundle>() {
            @Override
            public int compare(Bundle category2, Bundle category1) {
                String title2 = category2.getString("title");
                String title1 = category1.getString("title");

                if(title1 == null) {
                    if (title2 == null) {
                        return 0; //equal
                    } else {
                        return -1; // null is before other strings
                    }
                } else {
                    if (title2 == null) {
                        return 1;  // all other strings are after null
                    } else {
                        return title2.compareTo(title1);
                    }
                }
            }
        });

        realm.beginTransaction();
        for (Bundle values : parentCategories) {

            int id = values.getInt("id");
            Category category = realm.createObject(Category.class, id);
            category.setName(values.getString("name"));
        }
        realm.commitTransaction();

        // process child categories
        realm.beginTransaction();
        for (Bundle values : childCategories) {

            Category category = realm.createObject(Category.class, values.getInt("id"));
            category.setName(values.getString("name"));

            int parentId = values.getInt("parent_id");
            if (parentId > 0) {
                category.setParentCategory(realm.where(Category.class).equalTo("id", parentId).findFirst());
            }
        }
        realm.commitTransaction();
    }

    private void processAccountEntries() {
        realm.beginTransaction();
        for (Bundle accountBundle: mAccounts) {
            String currencyId = accountBundle.getString("currency");
            String currencyCode;
            if (mCurrencies.containsKey(currencyId)) {
                currencyCode = mCurrencies.get(currencyId);
            } else {
                currencyCode = "EUR";
            }

            Account account = realm.createObject(Account.class, accountBundle.getInt("id"));

            account.setTitle(accountBundle.getString("title"));
            account.setCurrency(currencyCode);
            account.setBalance(accountBundle.getLong("balance"));
            account.setType(accountBundle.getString("type"));
            account.setCardType(accountBundle.getString("card_type"));
            account.setOnlineAccountType(accountBundle.getString("online_account_type"));
            account.setActive(accountBundle.getBoolean("is_active"));
            account.setIncludeIntoTotals(accountBundle.getBoolean("include_into_totals"));
            account.setSortOrder(accountBundle.getInt("sort_order"));
            account.setNote(accountBundle.getString("note"));
            account.setCreatedAt(new Date(accountBundle.getLong("created_at")));
        }
        realm.commitTransaction();
    }

    private void processPayeeEntries() {
        realm.beginTransaction();
        for (Bundle payeeBundle: mPayees) {

            Payee payee = realm.createObject(Payee.class, payeeBundle.getInt("id"));
            payee.setName(payeeBundle.getString("name"));

            int lastCategoryId = payeeBundle.getInt("last_category_id");
            if (lastCategoryId > 0) {
                if (mMigrateCategories.indexOfKey(lastCategoryId) > 0) {
                    lastCategoryId = mMigrateCategories.get(lastCategoryId);
                }
                payee.setLastCategory(realm.where(Category.class).equalTo("id", lastCategoryId).findFirst());
            }
        }
        realm.commitTransaction();
    }

    private void processProjectEntries() {
        realm.beginTransaction();
        for (Bundle projectValues: mProjects) {
            Project project = realm.createObject(Project.class, projectValues.getInt("id"));
            project.setTitle(projectValues.getString("title"));
            project.setActive(projectValues.getBoolean("is_active"));
            long updatedAt = projectValues.getLong("updated_at");
            if (updatedAt > 0) {
                project.setUpdatedAt(new Date(updatedAt));
            }
        }
        realm.commitTransaction();
    }

    private void processTransactionEntries() {

        realm.beginTransaction();
        for (Bundle values : mTransactions) {

            int transactionId = values.getInt("id");

            Transaction transaction = realm.createObject(Transaction.class, transactionId);

            int fromAccountId = values.getInt("from_account_id");
            if (fromAccountId > 0) {
                transaction.setFromAccount(realm.where(Account.class).equalTo("id", fromAccountId).findFirst());
                transaction.setFromAmount(values.getLong("from_amount"));
            }

            int toAccountId = values.getInt("to_account_id");
            if (toAccountId > 0) {
                transaction.setToAccount(realm.where(Account.class).equalTo("id", toAccountId).findFirst());
                transaction.setToAmount(values.getLong("to_amount"));
            }

            int categoryId = values.getInt("category_id");
            if (categoryId > 0) {
                if (mMigrateCategories.indexOfKey(categoryId) > 0) {
                    categoryId = mMigrateCategories.get(categoryId);
                }
                transaction.setCategory(realm.where(Category.class).equalTo("id", categoryId).findFirst());
            }

            int payeeId = values.getInt("payee_id");
            if (payeeId > 0) {
                transaction.setPayee(realm.where(Payee.class).equalTo("id", payeeId).findFirst());
            }

            int projectId = values.getInt("project_id");
            if (projectId > 0) {
                transaction.setProject(realm.where(Project.class).equalTo("id", projectId).findFirst());
            }

            String originalCurrencyId = values.getString("original_currency");
            if (originalCurrencyId != null && !originalCurrencyId.isEmpty() && !originalCurrencyId.equals("0") && !originalCurrencyId.equals("-1")) {
                String currencyCode;
                if (mCurrencies.containsKey(originalCurrencyId)) {
                    currencyCode = mCurrencies.get(originalCurrencyId);
                    transaction.setOriginalCurrency(currencyCode);
                    transaction.setOriginalAmount(values.getLong("original_amount"));
                } else {
                    Log.w(TAG, "skipping unknown original currency ID " + originalCurrencyId);
                }
            }

            transaction.setNote(values.getString("note"));

            transaction.setOccurredAt(new Date(values.getLong("occurred_at")));
            transaction.setClearedAt(new Date(values.getLong("cleared_at")));

            long updatedAt = values.getLong("updated_at");
            if (updatedAt > 0) {
                transaction.setUpdatedAt(new Date(updatedAt));
            }

            List<Bundle> transactionSplits = mTransactionSplits.get(transactionId, null);
            if (transactionSplits != null) {

                long transactionAmount = 0;

                RealmList<TransactionSplit> splits = new RealmList<>();
                for (Bundle split : transactionSplits) {

                    TransactionSplit transactionSplit = realm.createObject(TransactionSplit.class, split.getInt("id"));

                    long splitAmount = split.getLong("amount");
                    transactionSplit.setAmount(splitAmount);
                    transactionAmount += splitAmount;

                    int splitCategoryId = split.getInt("category_id");
                    if (splitCategoryId > 0) {
                        transactionSplit.setCategory(realm.where(Category.class).equalTo("id", splitCategoryId).findFirst());
                    }

                    int splitProjectId = split.getInt("project_id");
                    if (splitProjectId > 0) {
                        transactionSplit.setProject(realm.where(Project.class).equalTo("id", splitProjectId).findFirst());
                    }

                    transactionSplit.setNote(split.getString("note"));

                    splits.add(transactionSplit);
                }
                transaction.setSplits(splits);

                transaction.setFromAmount(transactionAmount);
            }
        }
        realm.commitTransaction();
    }
}
