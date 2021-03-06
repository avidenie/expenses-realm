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

package ro.expectations.expenses.model;

import ro.expectations.expenses.R;

public enum CardType {

    VISA(R.string.card_issuer_visa, R.drawable.ic_card_visa_black_24dp),
    VISA_ELECTRON(R.string.card_issuer_visa_electron, R.drawable.ic_card_visa_black_24dp),
    MASTERCARD(R.string.card_issuer_mastercard, R.drawable.ic_card_mastercard_black_24dp),
    MAESTRO(R.string.card_issuer_maestro, R.drawable.ic_card_maestro_black_24dp),
    AMERICAN_EXPRESS(R.string.card_issuer_american_express, R.drawable.ic_card_amex_black_24dp),
    DISCOVER(R.string.card_issuer_discover, R.drawable.ic_card_discover_black_24dp),
    CIRRUS(R.string.card_issuer_cirrus, R.drawable.ic_card_cirrus_black_24dp),
    JCB(R.string.card_issuer_jcb, R.drawable.ic_card_jcb_black_24dp),
    DINERS(R.string.card_issuer_diners, R.drawable.ic_card_dinners_black_24dp),
    UNIONPAY(R.string.card_issuer_unionpay, R.drawable.ic_card_unionpay_black_24dp),
    EPS(R.string.card_issuer_eps, R.drawable.ic_card_eps_black_24dp),
    OTHER(R.string.card_issuer_other, R.drawable.ic_credit_card_black_24dp);

    private static final CardType[] copyOfValues = values();

    public final int titleId;
    public final int iconId;

    CardType(int titleId, int iconId) {
        this.titleId = titleId;
        this.iconId = iconId;
    }


    public static CardType fromString(String value, CardType defaultCardType) {
        if (value != null) {
            for (CardType val : copyOfValues) {
                if (value.equalsIgnoreCase(val.toString())) {
                    return val;
                }
            }
        }
        return defaultCardType;
    }

    public static CardType fromString(String value) {
        return fromString(value, OTHER);
    }
}
