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

package ro.expectations.expenses.ui.backup;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.drawer.DrawerActivity;

public class BackupActivity extends DrawerActivity {

    @Override
    protected void setMainContentView(@Nullable Bundle savedInstanceState) {
        ViewStub mainContent = (ViewStub) findViewById(R.id.main_content_stub);
        mainContent.setLayoutResource(R.layout.content_fragment);
        mainContent.setVisibility(View.VISIBLE);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_backup;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (isFinancistoInstalled()) {
            menu.add(Menu.NONE, R.id.action_financisto_import, Menu.NONE, R.string.action_financisto_import);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_financisto_import) {
            Intent financistoImportIntent = new Intent(this, FinancistoImportActivity.class);
            startActivity(financistoImportIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isFinancistoInstalled() {
        boolean installed;
        try {
            getPackageManager().getPackageInfo("ru.orangesoftware.financisto", PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }
}
