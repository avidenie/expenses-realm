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

package ro.expectations.expenses.ui.transactions;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import ro.expectations.expenses.R;
import ro.expectations.expenses.model.Account;
import ro.expectations.expenses.ui.accounts.ManageAccountActivity;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.ui.provider.AppBarLayoutProvider;

public class TransactionsActivity extends DrawerActivity implements AppBarLayoutProvider {

    public static final String ARG_ACCOUNT_ID = "TransactionsActivity.ARG_ACCOUNT_ID";

    private Realm mRealm;
    private AppBarLayout mAppBarLayout;
    private long mSelectedAccountId;

    RealmResults<Account> results;

    @Override
    protected void setDrawerContentView() {
        setContentView(R.layout.activity_drawer);
        ViewStub appBar = (ViewStub) findViewById(R.id.app_bar_stub);
        appBar.setLayoutResource(R.layout.app_bar_spinner);
        appBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void setMainContentView(@Nullable Bundle savedInstanceState) {

        mRealm = Realm.getDefaultInstance();

        if (savedInstanceState == null) {
            mSelectedAccountId = getIntent().getLongExtra(ARG_ACCOUNT_ID, 0);
        } else {
            mSelectedAccountId = savedInstanceState.getLong(ARG_ACCOUNT_ID, 0);
        }

        ViewStub mainContent = (ViewStub) findViewById(R.id.main_content_stub);
        mainContent.setLayoutResource(R.layout.content_fragment);
        mainContent.setVisibility(View.VISIBLE);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);

            // Setup spinner.
            final Spinner mAccountsSpinner = (Spinner) findViewById(R.id.spinner);

            results = mRealm.where(Account.class)
                    .equalTo(Account.IS_ACTIVE, true)
                    .findAllSortedAsync(Account.SORT_ORDER, Sort.ASCENDING);

            results.addChangeListener(new RealmChangeListener<RealmResults<Account>>() {
                @Override
                public void onChange(RealmResults<Account> results) {
                    List<Account> accounts = mRealm.copyFromRealm(results);

                    Account allAccounts = new Account();
                    allAccounts.setId(0);
                    allAccounts.setTitle(getString(R.string.all_accounts));
                    accounts.add(0, allAccounts);

                    AccountsSpinnerAdapter adapter = new AccountsSpinnerAdapter(actionBar.getThemedContext(), accounts);
                    mAccountsSpinner.setAdapter(adapter);
                }
            });

            mAccountsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (id != mSelectedAccountId) {
                        mSelectedAccountId = id;
                        getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_content, TransactionsFragment.newInstance(mSelectedAccountId, true))
                            .commit();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newAccountIntent = new Intent(TransactionsActivity.this, ManageAccountActivity.class);
                startActivity(newAccountIntent);
            }
        });
        fab.setVisibility(View.VISIBLE);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content, TransactionsFragment.newInstance(0, true))
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(ARG_ACCOUNT_ID, mSelectedAccountId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_transactions;
    }

    @Override
    public AppBarLayout getAppBarLayout() {
        return mAppBarLayout;
    }

    public class AccountsSpinnerAdapter extends BaseAdapter implements ThemedSpinnerAdapter {

        private List<Account> accounts;
        private final Context context;
        private final LayoutInflater inflater;
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        public AccountsSpinnerAdapter(@NonNull Context context, @NonNull List<Account> accounts) {
            this.context = context;
            this.accounts = accounts;
            this.mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
            this.inflater = LayoutInflater.from(context);
        }


        @Override
        public void setDropDownViewTheme(@Nullable Resources.Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }

        @Nullable
        @Override
        public Resources.Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public int getCount() {
            return accounts.size();
        }

        @Override
        public Account getItem(int position) {
            return accounts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return accounts.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int selectedItemPosition = position;
            if (parent instanceof AdapterView) {
                selectedItemPosition = ((AdapterView) parent).getSelectedItemPosition();
            }

            TextView tv;
            if (convertView != null) {
                tv = (TextView) convertView;
            } else {
                tv = (TextView) inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            Account account = getItem(selectedItemPosition);
            tv.setText(account.getTitle());

            return tv;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView != null) {
                tv = (TextView) convertView;
            } else {
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                tv = (TextView) inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }

            Account account = getItem(position);
            tv.setText(account.getTitle());

            return tv;
        }
    }
}
