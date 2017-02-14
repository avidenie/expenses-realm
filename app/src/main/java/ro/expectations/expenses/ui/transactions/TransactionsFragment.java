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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import ro.expectations.expenses.R;
import ro.expectations.expenses.model.Account;
import ro.expectations.expenses.model.Transaction;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.ui.recyclerview.ItemClickHelper;
import ro.expectations.expenses.utils.DrawableUtils;

public class TransactionsFragment extends Fragment {

    protected static final String ARG_ACCOUNT_ID = "TransactionsFragment.ARG_ACCOUNT_ID";
    protected static final String ARG_HANDLE_CLICKS = "TransactionsFragment.ARG_HANDLE_CLICKS";

    private Realm mRealm;

    RecyclerView recyclerView;

    private long mSelectedAccountId;
    private boolean mHandleClicks = false;

    private TransactionsAdapter mAdapter;
    private TextView mEmptyView;

    private ActionMode mActionMode;
    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu_transactions, menu);
            ((DrawerActivity) getActivity()).lockNavigationDrawer();
            MenuItem actionEditTransaction = menu.findItem(R.id.action_edit_transaction);
            actionEditTransaction.setIcon(DrawableUtils.tint(getContext(), actionEditTransaction.getIcon(), R.color.colorWhite));
            MenuItem actionDeleteTransaction = menu.findItem(R.id.action_delete_transaction);
            actionDeleteTransaction.setIcon(DrawableUtils.tint(getContext(), actionDeleteTransaction.getIcon(), R.color.colorWhite));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int selectedTransactions = mAdapter.getSelectedItemCount();
            mode.setTitle(getResources().getQuantityString(
                    R.plurals.selected_transactions,
                    selectedTransactions,
                    selectedTransactions
            ));
            if (selectedTransactions == 1) {
                menu.findItem(R.id.action_edit_transaction).setVisible(true);
            } else {
                menu.findItem(R.id.action_edit_transaction).setVisible(false);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            switch(id) {
                case R.id.action_edit_transaction:
                    mode.finish();
                    return true;
                case R.id.action_delete_transaction:
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            if (mAdapter.hasItemSelected()) {
                mAdapter.clearSelection();
            }
            ((DrawerActivity) getActivity()).unlockNavigationDrawer();
        }
    };

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

        mRealm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


        mEmptyView = (TextView) view.findViewById(R.id.list_transactions_empty);

        recyclerView = (RecyclerView) view.findViewById(R.id.list_transactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);


        ItemClickHelper itemClickHelper = new ItemClickHelper(recyclerView);
        if (mHandleClicks) {
            itemClickHelper.setOnItemClickListener(new ItemClickHelper.OnItemClickListener() {
                @Override
                public void onItemClick(RecyclerView parent, View view, int position) {
                    boolean isItemSelected = mAdapter.isItemSelected(position);
                    if (isItemSelected) {
                        mAdapter.setItemSelected(position, false);
                        if (!mAdapter.hasItemSelected()) {
                            mActionMode.finish();
                        } else {
                            mActionMode.invalidate();
                        }
                    } else if (mAdapter.hasItemSelected()) {
                        mAdapter.setItemSelected(position, true);
                        mActionMode.invalidate();
                    }
                }
            });
            itemClickHelper.setOnItemLongClickListener(new ItemClickHelper.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(RecyclerView parent, View view, int position) {
                    mAdapter.setItemSelected(position, !mAdapter.isItemSelected(position));
                    if (mAdapter.hasItemSelected()) {
                        if (mActionMode == null) {
                            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                        } else {
                            mActionMode.invalidate();
                        }
                    } else {
                        if (mActionMode != null) {
                            mActionMode.finish();
                        }
                    }
                    return true;
                }
            });
        } else {
            itemClickHelper.setOnItemClickListener(new ItemClickHelper.OnItemClickListener() {
                @Override
                public void onItemClick(RecyclerView parent, View view, int position) {
                    // nothing to do, just the ripple effect
                }
            });
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (mHandleClicks && savedInstanceState != null) {
            mAdapter.onRestoreInstanceState(savedInstanceState);
            if (mAdapter.hasItemSelected() && mActionMode == null) {
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            }
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mHandleClicks) {
            mAdapter.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        RealmResults<Transaction> transactions;
        if (mSelectedAccountId > 0) {
            transactions = mRealm.where(Transaction.class)
                    .beginGroup()
                    .equalTo(Transaction.FROM_ACCOUNT + "." + Account.ID, mSelectedAccountId)
                    .or()
                    .equalTo(Transaction.TO_ACCOUNT + "." + Account.ID, mSelectedAccountId)
                    .endGroup()
                    .findAllSortedAsync(Transaction.OCCURRED_AT, Sort.DESCENDING);
        } else {
            transactions = mRealm.where(Transaction.class)
                    .findAllSortedAsync(Transaction.OCCURRED_AT, Sort.DESCENDING);
        }
        mAdapter = new TransactionsAdapter(getActivity(), transactions, mSelectedAccountId, true);
        recyclerView.setAdapter(mAdapter);
    }
}
