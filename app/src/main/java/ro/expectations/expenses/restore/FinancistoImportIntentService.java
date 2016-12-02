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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class FinancistoImportIntentService extends AbstractRestoreIntentService {

    public static final String ACTION_SUCCESS = "FinancistoImportIntentService.ACTION_SUCCESS";
    public static final String ACTION_FAILURE = "FinancistoImportIntentService.ACTION_FAILURE";

    private static final String TAG = FinancistoImportIntentService.class.getSimpleName();

    @Override
    protected void parse(InputStream input) throws IOException {
        InputStreamReader reader = new InputStreamReader(input, "UTF-8");
        BufferedReader br = new BufferedReader(reader, 65535);
        try {
            boolean insideEntity = false;
            Map<String, String> values = new HashMap<>();
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
        } finally {
            br.close();
        }
        Log.i(TAG, "Finished parsing Financisto backup file");

        processAccountEntries();
        processCategoryEntries();
        processTransactionEntries();
        Log.i(TAG, "Finished processing Financisto backup file");
    }

    @Override
    protected void notifySuccess() {
        Log.e(TAG, "notifySuccess");
        Intent successIntent = new Intent(ACTION_SUCCESS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(successIntent);
    }

    @Override
    protected void notifyFailure(Exception e) {
        Log.e(TAG, "notifyFailure", e);
        Intent failureIntent = new Intent(ACTION_FAILURE);
        failureIntent.putExtra("exception", e);
        LocalBroadcastManager.getInstance(this).sendBroadcast(failureIntent);
    }

    private void processEntry(String tableName, Map<String, String> values) {
        switch(tableName) {
            case "account":
                processAccountEntry(values);
                break;
            case "currency":
                processCurrencyEntry(values);
                break;
            case "payee":
                processPayeeEntry(values);
                break;
            case "category":
                processCategoryEntry(values);
                break;
            case "transactions":
                if (!values.containsKey("is_template") || Integer.parseInt(values.get("is_template")) == 0) {
                    processTransactionEntry(values);
                }
                break;
        }
    }

    private void processAccountEntry(Map<String, String> values) {

    }

    private void processCurrencyEntry(Map<String, String> values) {

    }

    private void processPayeeEntry(Map<String, String> values) {

    }

    private void processCategoryEntry(Map<String, String> values) {

    }

    private void processTransactionEntry(Map<String, String> values) {

    }

    private void processAccountEntries() {

    }

    private void processCategoryEntries() {

    }

    private void processTransactionEntries() {

    }
}
