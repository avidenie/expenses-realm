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

package ro.expectations.expenses.ui.payees;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewStub;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.ui.provider.AppBarLayoutProvider;

public class PayeesActivity extends DrawerActivity implements AppBarLayoutProvider {

    private AppBarLayout mAppBarLayout;

    @Override
    protected void setMainContentView(@Nullable Bundle savedInstanceState) {
        ViewStub mainContent = (ViewStub) findViewById(R.id.main_content_stub);
        mainContent.setLayoutResource(R.layout.content_fragment);
        mainContent.setVisibility(View.VISIBLE);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newPayeeIntent = new Intent(PayeesActivity.this, ManagePayeeActivity.class);
                startActivity(newPayeeIntent);
            }
        });
        fab.setVisibility(View.VISIBLE);

        if (savedInstanceState == null) {
            PayeesFragment fragment =  PayeesFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.main_content, fragment);
            transaction.commit();
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_payees;
    }

    @Override
    public AppBarLayout getAppBarLayout() {
        return mAppBarLayout;
    }
}
