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
        categoryValues.putInt(Category.ID, Integer.parseInt(values.get("_id")));
        categoryValues.putString(Category.NAME, values.get("title"));
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
        accountValues.putInt(Account.ID, Integer.parseInt(values.get("_id")));
        accountValues.putString(Account.TITLE, values.get("title"));
        accountValues.putString(Account.CURRENCY, values.get("currency_id"));
        accountValues.putLong(Account.BALANCE, Long.parseLong(values.get("total_amount")));
        accountValues.putString(Account.TYPE, type.name());
        if (cardType != null) {
            accountValues.putString(Account.CARD_TYPE, cardType.name());
        }
        if (onlineAccountType != null) {
            accountValues.putString(Account.ONLINE_ACCOUNT_TYPE, onlineAccountType.name());
        }
        accountValues.putBoolean(Account.IS_ACTIVE, Integer.parseInt(values.get("is_active")) == 1);
        accountValues.putBoolean(Account.INCLUDE_INTO_TOTALS, Integer.parseInt(values.get("is_include_into_totals")) == 1);
        accountValues.putInt(Account.SORT_ORDER, Integer.parseInt(values.get("sort_order")));
        accountValues.putString(Account.NOTE, values.get("note"));
        accountValues.putLong(Account.CREATED_AT, Long.parseLong(values.get("creation_date")));

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
        payeeValues.putInt(Payee.ID, Integer.parseInt(values.get("_id")));
        payeeValues.putString(Payee.NAME, values.get("title"));
        payeeValues.putInt("last_category_id", Integer.parseInt(values.get("last_category_id")));

        mPayees.add(payeeValues);
    }

    private void processProjectEntry(Map<String, String> values) {

        Bundle projectValues = new Bundle();
        int projectId = Integer.parseInt(values.get("_id"));
        if (projectId > 0) {
            projectValues.putInt(Project.ID, projectId);
            projectValues.putString(Project.TITLE, values.get("title"));
            projectValues.putBoolean(Project.IS_ACTIVE, Integer.parseInt(values.get("is_active")) == 1);
            projectValues.putLong(Project.UPDATED_AT, Long.parseLong(values.get("updated_on")));

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
                splitBundle.putInt(TransactionSplit.ID, id);
                splitBundle.putLong(TransactionSplit.AMOUNT, 0 - fromAmount);

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

                splitBundle.putString(TransactionSplit.NOTE, values.get("note"));

                int key = Integer.parseInt(parentId);
                List<Bundle> transactionSplits = mTransactionSplits.get(key, null);
                if (transactionSplits == null) {
                    transactionSplits = new ArrayList<>();
                    mTransactionSplits.put(key, transactionSplits);
                }
                transactionSplits.add(splitBundle);
            }

        } else {

            transactionBundle.putInt(Transaction.ID, id);

            if (toAccountId > 0) {
                transactionBundle.putInt("from_account_id", fromAccountId);
                transactionBundle.putLong(Transaction.FROM_AMOUNT, 0 - fromAmount);
                transactionBundle.putInt("to_account_id", toAccountId);
                transactionBundle.putLong(Transaction.TO_AMOUNT, toAmount);
            } else {
                if (fromAmount > 0) {
                    transactionBundle.putInt("to_account_id", fromAccountId);
                    transactionBundle.putLong(Transaction.TO_AMOUNT, fromAmount);
                } else {
                    transactionBundle.putInt("from_account_id", fromAccountId);
                    transactionBundle.putLong(Transaction.FROM_AMOUNT, 0 - fromAmount);
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

            transactionBundle.putString(Transaction.NOTE, values.get("note"));

            String originalFromCurrencyId = values.get("original_currency_id");
            if (originalFromCurrencyId != null && !originalFromCurrencyId.isEmpty() && !originalFromCurrencyId.equals("0")) {
                transactionBundle.putString(Transaction.ORIGINAL_CURRENCY, originalFromCurrencyId);
                long originalFromAmount = Long.parseLong(values.get("original_from_amount"));
                transactionBundle.putLong(Transaction.ORIGINAL_AMOUNT, originalFromAmount);
            }

            long createdAt = Long.parseLong(values.get("datetime"));
            transactionBundle.putLong(Transaction.OCCURRED_AT, createdAt);
            transactionBundle.putLong(Transaction.CLEARED_AT, createdAt);
            long updatedAt = Long.parseLong(values.get("updated_on"));
            if (updatedAt > 1) {
                transactionBundle.putLong(Transaction.UPDATED_AT, updatedAt);
            }

            mTransactions.add(transactionBundle);
        }
    }

    private void processCategoryEntries() {

        List<Bundle> parentCategories = new ArrayList<>();
        List<Bundle> childCategories = new ArrayList<>();

        for (Bundle currentCategoryBundle : mCategories) {
            long id = currentCategoryBundle.getInt(Category.ID);
            if (id <= 0) {
                continue;
            }

            int parentId = 0;
            int left = currentCategoryBundle.getInt("left");
            int previousLeft = 0;
            int level = 0;

            for(Bundle categoryBundle : mCategories) {
                int currentId = categoryBundle.getInt(Category.ID);
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
                mMigrateCategories.put(currentCategoryBundle.getInt(Category.ID), parentId);
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
                String title2 = category2.getString(Category.NAME);
                String title1 = category1.getString(Category.NAME);

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

            int id = values.getInt(Category.ID);
            Category category = realm.createObject(Category.class, id);
            category.setName(values.getString(Category.NAME));
        }
        realm.commitTransaction();

        // process child categories
        realm.beginTransaction();
        for (Bundle values : childCategories) {

            Category category = realm.createObject(Category.class, values.getInt(Category.ID));
            category.setName(values.getString(Category.NAME));

            int parentId = values.getInt("parent_id");
            if (parentId > 0) {
                category.setParentCategory(realm.where(Category.class).equalTo(Category.ID, parentId).findFirst());
            }
        }
        realm.commitTransaction();
    }

    private void processAccountEntries() {
        realm.beginTransaction();
        for (Bundle accountBundle: mAccounts) {
            String currencyId = accountBundle.getString(Account.CURRENCY);
            String currencyCode;
            if (mCurrencies.containsKey(currencyId)) {
                currencyCode = mCurrencies.get(currencyId);
            } else {
                currencyCode = "EUR";
            }

            Account account = realm.createObject(Account.class, accountBundle.getInt(Account.ID));

            account.setTitle(accountBundle.getString(Account.TITLE));
            account.setCurrency(currencyCode);
            account.setBalance(accountBundle.getLong(Account.BALANCE));
            account.setType(accountBundle.getString(Account.TYPE));
            account.setCardType(accountBundle.getString(Account.CARD_TYPE));
            account.setOnlineAccountType(accountBundle.getString(Account.ONLINE_ACCOUNT_TYPE));
            account.setActive(accountBundle.getBoolean(Account.IS_ACTIVE));
            account.setIncludeIntoTotals(accountBundle.getBoolean(Account.INCLUDE_INTO_TOTALS));
            account.setSortOrder(accountBundle.getInt(Account.SORT_ORDER));
            account.setNote(accountBundle.getString(Account.NOTE));
            account.setCreatedAt(new Date(accountBundle.getLong(Account.CREATED_AT)));
        }
        realm.commitTransaction();
    }

    private void processPayeeEntries() {
        realm.beginTransaction();
        for (Bundle payeeBundle: mPayees) {

            Payee payee = realm.createObject(Payee.class, payeeBundle.getInt(Payee.ID));
            payee.setName(payeeBundle.getString(Payee.NAME));

            int lastCategoryId = payeeBundle.getInt("last_category_id");
            if (lastCategoryId > 0) {
                if (mMigrateCategories.indexOfKey(lastCategoryId) > 0) {
                    lastCategoryId = mMigrateCategories.get(lastCategoryId);
                }
                payee.setLastCategory(realm.where(Category.class).equalTo(Category.ID, lastCategoryId).findFirst());
            }
        }
        realm.commitTransaction();
    }

    private void processProjectEntries() {
        realm.beginTransaction();
        for (Bundle projectValues: mProjects) {
            Project project = realm.createObject(Project.class, projectValues.getInt(Project.ID));
            project.setTitle(projectValues.getString(Project.TITLE));
            project.setActive(projectValues.getBoolean(Project.IS_ACTIVE));
            long updatedAt = projectValues.getLong(Project.UPDATED_AT);
            if (updatedAt > 0) {
                project.setUpdatedAt(new Date(updatedAt));
            }
        }
        realm.commitTransaction();
    }

    private void processTransactionEntries() {

        realm.beginTransaction();
        for (Bundle values : mTransactions) {

            int transactionId = values.getInt(Transaction.ID);

            Transaction transaction = realm.createObject(Transaction.class, transactionId);

            int fromAccountId = values.getInt("from_account_id");
            if (fromAccountId > 0) {
                transaction.setFromAccount(realm.where(Account.class).equalTo(Account.ID, fromAccountId).findFirst());
                transaction.setFromAmount(values.getLong(Transaction.FROM_AMOUNT));
            }

            int toAccountId = values.getInt("to_account_id");
            if (toAccountId > 0) {
                transaction.setToAccount(realm.where(Account.class).equalTo(Account.ID, toAccountId).findFirst());
                transaction.setToAmount(values.getLong(Transaction.TO_AMOUNT));
            }

            int categoryId = values.getInt("category_id");
            if (categoryId > 0) {
                if (mMigrateCategories.indexOfKey(categoryId) > 0) {
                    categoryId = mMigrateCategories.get(categoryId);
                }
                transaction.setCategory(realm.where(Category.class).equalTo(Category.ID, categoryId).findFirst());
            }

            int payeeId = values.getInt("payee_id");
            if (payeeId > 0) {
                transaction.setPayee(realm.where(Payee.class).equalTo(Payee.ID, payeeId).findFirst());
            }

            int projectId = values.getInt("project_id");
            if (projectId > 0) {
                transaction.setProject(realm.where(Project.class).equalTo(Project.ID, projectId).findFirst());
            }

            String originalCurrencyId = values.getString(Transaction.ORIGINAL_CURRENCY);
            if (originalCurrencyId != null && !originalCurrencyId.isEmpty() && !originalCurrencyId.equals("0") && !originalCurrencyId.equals("-1")) {
                String currencyCode;
                if (mCurrencies.containsKey(originalCurrencyId)) {
                    currencyCode = mCurrencies.get(originalCurrencyId);
                    transaction.setOriginalCurrency(currencyCode);
                    transaction.setOriginalAmount(values.getLong(Transaction.ORIGINAL_AMOUNT));
                } else {
                    Log.w(TAG, "skipping unknown original currency ID " + originalCurrencyId);
                }
            }

            transaction.setNote(values.getString(Transaction.NOTE));

            transaction.setOccurredAt(new Date(values.getLong(Transaction.OCCURRED_AT)));
            transaction.setClearedAt(new Date(values.getLong(Transaction.CLEARED_AT)));

            long updatedAt = values.getLong(Transaction.CLEARED_AT);
            if (updatedAt > 0) {
                transaction.setUpdatedAt(new Date(updatedAt));
            }

            List<Bundle> transactionSplits = mTransactionSplits.get(transactionId, null);
            if (transactionSplits != null) {

                long transactionAmount = 0;

                RealmList<TransactionSplit> splits = new RealmList<>();
                for (Bundle split : transactionSplits) {

                    TransactionSplit transactionSplit = realm.createObject(TransactionSplit.class, split.getInt(TransactionSplit.ID));

                    long splitAmount = split.getLong(TransactionSplit.AMOUNT);
                    transactionSplit.setAmount(splitAmount);
                    transactionAmount += splitAmount;

                    int splitCategoryId = split.getInt("category_id");
                    if (splitCategoryId > 0) {
                        transactionSplit.setCategory(realm.where(Category.class).equalTo(Category.ID, splitCategoryId).findFirst());
                    }

                    int splitProjectId = split.getInt("project_id");
                    if (splitProjectId > 0) {
                        transactionSplit.setProject(realm.where(Project.class).equalTo(Project.ID, splitProjectId).findFirst());
                    }

                    transactionSplit.setNote(split.getString(TransactionSplit.NOTE));

                    splits.add(transactionSplit);
                }
                transaction.setSplits(splits);

                transaction.setFromAmount(transactionAmount);
            }
        }
        realm.commitTransaction();
    }
}
