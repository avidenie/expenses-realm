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

package ro.expectations.expenses.data;

import android.content.Context;
import android.util.Log;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import ro.expectations.expenses.model.Account;
import ro.expectations.expenses.model.Transaction;

/**
 * Helper to check the database for integrity and fix any issues.
 */
public class DataIntegrityFixer {

    private static final String TAG = DataIntegrityFixer.class.getSimpleName();

    private final Context mContext;

    public DataIntegrityFixer(Context context) {
        mContext = context;
    }

    public void fix() {
        long t0 = System.currentTimeMillis();

        Realm realm = Realm.getDefaultInstance();

        final RealmResults<Account> accounts = realm.where(Account.class).findAll();
        for(Account account: accounts) {

            long balance = 0;
            Date lastTransactionAt = null;

            final RealmResults<Transaction> transactions = realm.where(Transaction.class)
                    .beginGroup()
                        .equalTo("fromAccount.id", account.getId())
                        .or()
                        .equalTo("toAccount.id", account.getId())
                    .endGroup()
                    .findAll();
            transactions.sort("occurredAt", Sort.ASCENDING);

            realm.beginTransaction();

            for (Transaction transaction: transactions) {
                Account fromAccount = transaction.getFromAccount();
                Account toAccount = transaction.getToAccount();
                if (fromAccount != null && fromAccount.getId() == account.getId()) {
                    balance -= transaction.getFromAmount();
                    transaction.setFromRunningBalance(balance);
                } else if (toAccount != null && toAccount.getId() == account.getId()) {
                    balance += transaction.getToAmount();
                    transaction.setToRunningBalance(balance);
                }
                Date occurredAt = transaction.getOccurredAt();
                if (lastTransactionAt == null) {
                    lastTransactionAt = occurredAt;
                } else if (occurredAt.after(lastTransactionAt)) {
                    lastTransactionAt = occurredAt;
                }
            }

            account.setBalance(balance);
            account.setLastTransactionAt(lastTransactionAt);

            realm.commitTransaction();
        }

        long t1 = System.currentTimeMillis();
        Log.i(TAG, "Data integrity fixer took " + TimeUnit.MILLISECONDS.toSeconds(t1 - t0) + "s");
        realm.close();
    }
}
