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

package ro.expectations.expenses.ui.accounts;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.FrameLayout;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import ro.expectations.expenses.R;
import ro.expectations.expenses.model.Account;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.ui.provider.AppBarLayoutProvider;
import ro.expectations.expenses.ui.recyclerview.ItemClickHelper;
import ro.expectations.expenses.ui.transactions.TransactionsActivity;
import ro.expectations.expenses.utils.ColorUtils;

public class AccountsFragment extends Fragment {

    private Realm mRealm;

    private AccountsAdapter mAdapter;
    private TextView mEmptyView;
    private FrameLayout mVerticalCenterWrapper;

    private int mStatusBarColor;

    private AppBarLayoutProvider mAppBarLayoutProvider;

    private ActionMode mActionMode;
    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu_accounts, menu);

            ((DrawerActivity) getActivity()).lockNavigationDrawer();

            menu.findItem(R.id.action_edit_account)
                    .getIcon()
                    .setColorFilter(ContextCompat.getColor(getContext(), R.color.colorWhite),
                            PorterDuff.Mode.SRC_IN);
            menu.findItem(R.id.action_close_account)
                    .getIcon()
                    .setColorFilter(ContextCompat.getColor(getContext(), R.color.colorWhite),
                            PorterDuff.Mode.SRC_IN);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int primaryColorDark = ColorUtils.getColorFromTheme(getActivity(), R.attr.colorPrimaryDark);
                getActivity().getWindow().setStatusBarColor(0XFF000000 | primaryColorDark);
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            int selectedAccounts = mAdapter.getSelectedItemCount();
            mode.setTitle(getResources().getQuantityString(
                    R.plurals.selected_accounts,
                    selectedAccounts,
                    selectedAccounts
            ));
            if (mAdapter.getSelectedItemCount() == 1) {
                menu.findItem(R.id.action_edit_account).setVisible(true);
            } else {
                menu.findItem(R.id.action_edit_account).setVisible(false);
            }

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            switch(id) {
                case R.id.action_edit_account:
                    if (mAdapter.getSelectedItemCount() == 1) {
                        int position = mAdapter.getSelectedItemPositions().get(0);
                        long accountId = mAdapter.getItemId(position);
                        Intent editAccountIntent = new Intent(getActivity(), ManageAccountActivity.class);
                        editAccountIntent.putExtra(ManageAccountActivity.ARG_ACCOUNT_ID, accountId);
                        startActivity(editAccountIntent);
                    }
                    mode.finish();
                    return true;
                case R.id.action_close_account:
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

            // reset the status bar color to default
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActivity().getWindow().setStatusBarColor(mStatusBarColor);
            }
        }
    };

    public static AccountsFragment newInstance() {
        return new AccountsFragment();
    }

    public AccountsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof AppBarLayoutProvider) {
            mAppBarLayoutProvider = (AppBarLayoutProvider) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AppBarLayoutProvider");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accounts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mStatusBarColor = getActivity().getWindow().getStatusBarColor();
        }

        mEmptyView = (TextView) view.findViewById(R.id.list_accounts_empty);

        mVerticalCenterWrapper = (FrameLayout) view.findViewById(R.id.vertical_center_wrapper);
        mAppBarLayoutProvider.getAppBarLayout().addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int paddingBottom = appBarLayout.getTotalScrollRange() - Math.abs(verticalOffset);
                mVerticalCenterWrapper.setPaddingRelative(0, 0, 0, paddingBottom);
            }
        });

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list_accounts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);

        RealmResults<Account> accounts = mRealm.where(Account.class)
                .equalTo("isActive", true)
                .findAllSortedAsync("sortOrder", Sort.ASCENDING);
        accounts.addChangeListener(new RealmChangeListener<RealmResults<Account>>() {
            @Override
            public void onChange(RealmResults<Account> accounts) {
                mEmptyView.setVisibility(accounts.size() > 0 ? View.GONE : View.VISIBLE);
            }
        });
        mAdapter = new AccountsAdapter(getActivity(), accounts, true);
        recyclerView.setAdapter(mAdapter);

        ItemClickHelper itemClickHelper = new ItemClickHelper(recyclerView);
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
                } else {
                    long id = parent.getAdapter().getItemId(position);
                    Intent transactionsListingIntent = new Intent(getActivity(), TransactionsActivity.class);
                    transactionsListingIntent.putExtra(TransactionsActivity.ARG_ACCOUNT_ID, id);
                    startActivity(transactionsListingIntent);
                }
            }
        });
        itemClickHelper.setOnItemLongClickListener(new ItemClickHelper.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView parent, View view, int position) {
                mAdapter.setItemSelected(position, !mAdapter.isItemSelected(position));
                if (mAdapter.hasItemSelected()) {
                    if (mActionMode == null) {
                        mActionMode = ((AccountsActivity) getActivity()).startSupportActionMode(mActionModeCallback);
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
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mAdapter.onRestoreInstanceState(savedInstanceState);
            if (mAdapter.hasItemSelected() && mActionMode == null) {
                mActionMode = ((AccountsActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            }
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAppBarLayoutProvider = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}
