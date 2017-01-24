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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Spinner;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import ro.expectations.expenses.R;
import ro.expectations.expenses.model.Account;
import ro.expectations.expenses.ui.accounts.ManageAccountActivity;
import ro.expectations.expenses.ui.adapter.RealmSpinnerAdapter;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.ui.provider.AppBarLayoutProvider;

public class TransactionsActivity extends DrawerActivity implements AppBarLayoutProvider {

    public static final String ARG_ACCOUNT_ID = "TransactionsActivity.ARG_ACCOUNT_ID";

    private Realm mRealm;
    private AppBarLayout mAppBarLayout;
    private long mSelectedAccountId;

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

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);

            // Setup spinner.
            RealmResults<Account> accounts = mRealm.where(Account.class)
                    .equalTo(Account.IS_ACTIVE, true)
                    .findAllSortedAsync(Account.TITLE, Sort.ASCENDING);
            RealmSpinnerAdapter adapter = new RealmSpinnerAdapter<Account>(actionBar.getThemedContext(), accounts) {
                @Override
                public String getItemData(int position) {
                    Account account = (Account) getItem(position);
                    if (account != null) {
                        return account.getTitle();
                    }
                    return null;
                }
            };

            Spinner mAccountsSpinner = (Spinner) findViewById(R.id.spinner);
            mAccountsSpinner.setAdapter(adapter);

            mAccountsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (id != mSelectedAccountId) {
                        mSelectedAccountId = id;
                        //todo: load the transaction fragment
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
}
