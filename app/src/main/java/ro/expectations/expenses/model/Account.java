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

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Account extends RealmObject {

    @PrimaryKey
    private int id;
    @Required
    private String title;
    @Required
    private String currency;
    private long balance = 0;
    @Required
    private String type;
    private String cardType;
    private String onlineAccountType;
    @Index
    private boolean isActive = true;
    private boolean includeIntoTotals = true;
    @Index
    private int sortOrder = 0;
    private String note;
    private Date createdAt;
    private Date lastTransactionAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setType(AccountType accountType) {
        this.type = accountType.toString();
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType.toString();
    }

    public String getOnlineAccountType() {
        return onlineAccountType;
    }

    public void setOnlineAccountType(String onlineAccountType) {
        this.onlineAccountType = onlineAccountType;
    }

    public void setOnlineAccountType(OnlineAccountType onlineAccountType) {
        this.onlineAccountType = onlineAccountType.toString();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isIncludeIntoTotals() {
        return includeIntoTotals;
    }

    public void setIncludeIntoTotals(boolean includeIntoTotals) {
        this.includeIntoTotals = includeIntoTotals;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastTransactionAt() {
        return lastTransactionAt;
    }

    public void setLastTransactionAt(Date lastTransactionAt) {
        this.lastTransactionAt = lastTransactionAt;
    }
}
