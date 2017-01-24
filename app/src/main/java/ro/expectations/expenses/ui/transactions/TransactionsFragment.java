/*
 * Copyright (c) 2017 Adrian Videnie
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

package ro.expectations.expenses.ui.transactions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class TransactionsFragment extends Fragment {

    protected static final String ARG_ACCOUNT_ID = "TransactionsFragment.ARG_ACCOUNT_ID";
    protected static final String ARG_HANDLE_CLICKS = "TransactionsFragment.ARG_HANDLE_CLICKS";

    private long mSelectedAccountId;
    private boolean mHandleClicks = false;

    public static TransactionsFragment newInstance(long accountId, boolean handleClicks) {
        TransactionsFragment fragment = new TransactionsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ACCOUNT_ID, accountId);
        args.putBoolean(ARG_HANDLE_CLICKS, handleClicks);
        fragment.setArguments(args);
        return fragment;
    }

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSelectedAccountId = getArguments().getLong(ARG_ACCOUNT_ID);
            mHandleClicks = getArguments().getBoolean(ARG_HANDLE_CLICKS);
        }
    }
}
