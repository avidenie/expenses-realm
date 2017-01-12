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

package ro.expectations.expenses.ui.drawer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.accounts.AccountsActivity;
import ro.expectations.expenses.ui.backup.BackupActivity;
import ro.expectations.expenses.ui.overview.OverviewActivity;
import ro.expectations.expenses.ui.transactions.TransactionsActivity;

public abstract class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDrawerContentView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(getSelfNavDrawerItem());

        setMainContentView(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_overview && !(this instanceof OverviewActivity)) {
            Intent overviewIntent = new Intent(this, OverviewActivity.class);
            startActivityOnCloseDrawer(overviewIntent);
            return true;
        } else if (id == R.id.nav_accounts && !(this instanceof AccountsActivity)) {
            Intent accountsIntent = new Intent(this, AccountsActivity.class);
            startActivityOnCloseDrawer(accountsIntent);
            return true;
        } else if (id == R.id.nav_transactions && !(this instanceof TransactionsActivity)) {
            Intent transactionsIntent = new Intent(this, TransactionsActivity.class);
            startActivityOnCloseDrawer(transactionsIntent);
            return true;
        } else if (id == R.id.nav_backup && !(this instanceof BackupActivity)) {
            Intent backupIntent = new Intent(this, BackupActivity.class);
            startActivityOnCloseDrawer(backupIntent);
            return true;
        } else if (id == R.id.nav_categories) {
            Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_payees) {
            Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_exchange_rates) {
            Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    /**
     * Close the navigation drawer and disable all interactions with it.
     */
    public void lockNavigationDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    /**
     * Enable interactions with navigation drawer.
     */
    public void unlockNavigationDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    /**
     * Set the content view for the navigation drawer.
     *
     * Subclasses of DrawerActivity can override this to load a different content view.
     */
    protected void setDrawerContentView() {
        setContentView(R.layout.activity_drawer);
        findViewById(R.id.app_bar_stub).setVisibility(View.VISIBLE);
    }

    /**
     * Set the main content to load.
     *
     * Subclasses of DrawerActivity implement this to load main content layout on demand.
     */
    protected abstract void setMainContentView(@Nullable Bundle savedInstanceState);

    /**
     * Returns the navigation drawer item that corresponds to this Activity.
     *
     * Subclasses of DrawerActivity implement this to indicate what nav drawer item corresponds to
     * them.
     */
    protected abstract int getSelfNavDrawerItem();

    // Close the navigation drawer and start a new activity after the drawer is closed.
    private void startActivityOnCloseDrawer(final Intent intent) {
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                startActivity(intent);
            }
        });
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }
}
