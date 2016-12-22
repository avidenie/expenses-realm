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

package ro.expectations.expenses.model;

import ro.expectations.expenses.R;

public enum AccountType {

    CASH(R.string.account_type_cash, R.drawable.ic_wallet_black_24dp, R.color.colorGreen500),
    DEBIT_CARD(R.string.account_type_debit_card, R.drawable.ic_credit_card_black_24dp, R.color.colorBlue500),
    CREDIT_CARD(R.string.account_type_credit_card, R.drawable.ic_credit_card_black_24dp, R.color.colorTeal500),
    BANK(R.string.account_type_bank, R.drawable.ic_account_balance_black_24dp, R.color.colorPurple500),
    SAVINGS(R.string.account_type_savings, R.drawable.ic_money_bag_black_24dp, R.color.colorDeepOrange500),
    LOAN(R.string.account_type_loan, R.drawable.ic_loan_account_black_24dp, R.color.colorPink500),
    ONLINE(R.string.account_type_online, R.drawable.ic_online_account_black_24dp, R.color.colorBlueGrey500),
    OTHER(R.string.account_type_other, R.drawable.ic_wallet_black_24dp, R.color.colorAmber500);

    private static final AccountType[] copyOfValues = values();

    public final int titleId;
    public final int iconId;
    public final int colorId;

    AccountType(int titleId, int iconId, int colorId) {
        this.titleId = titleId;
        this.iconId = iconId;
        this.colorId = colorId;
    }

    public static AccountType fromString(String value, AccountType defaultAccountType) {
        if (value != null) {
            for (AccountType val : copyOfValues) {
                if (value.equalsIgnoreCase(val.toString())) {
                    return val;
                }
            }
        }
        return defaultAccountType;
    }

    public static AccountType fromString(String value) {
        return fromString(value, OTHER);
    }
}
