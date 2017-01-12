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

package ro.expectations.expenses.ui.overview;

import android.os.Bundle;
import android.support.annotation.Nullable;

import ro.expectations.expenses.R;
import ro.expectations.expenses.ui.drawer.DrawerActivity;

public class OverviewActivity extends DrawerActivity {

    @Override
    protected void setMainContentView(@Nullable Bundle savedInstanceState) {

    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_overview;
    }
}
