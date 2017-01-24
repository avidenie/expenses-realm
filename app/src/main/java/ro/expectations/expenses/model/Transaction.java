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

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Transaction extends RealmObject {

    public static final String ID = "id";
    public static final String FROM_ACCOUNT = "fromAccount";
    public static final String FROM_AMOUNT = "fromAmount";
    public static final String FROM_RUNNING_BALANCE = "fromRunningBalance";
    public static final String TO_ACCOUNT = "toAccount";
    public static final String TO_AMOUNT = "toAmount";
    public static final String TO_RUNNING_BALANCE = "toRunningBalance";
    public static final String PAYEE = "payee";
    public static final String CATEGORY = "category";
    public static final String PROJECT = "project";
    public static final String NOTE = "note";
    public static final String ORIGINAL_CURRENCY = "originalCurrency";
    public static final String ORIGINAL_AMOUNT = "originalAmount";
    public static final String OCCURRED_AT = "occurredAt";
    public static final String CLEARED_AT = "clearedAt";
    public static final String UPDATED_AT = "updatedAt";
    public static final String SPLITS = "splits";

    @PrimaryKey
    private int id;

    private Account fromAccount;
    private long fromAmount;
    private long fromRunningBalance;

    private Account toAccount;
    private long toAmount;
    private long toRunningBalance;

    private Payee payee;

    private Category category;
    private Project project;
    private String note;

    private String originalCurrency;
    private long originalAmount;

    private Date occurredAt;
    private Date clearedAt;
    private Date updatedAt;

    private RealmList<TransactionSplit> splits;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public long getFromAmount() {
        return fromAmount;
    }

    public void setFromAmount(long fromAmount) {
        this.fromAmount = fromAmount;
    }

    public long getFromRunningBalance() {
        return fromRunningBalance;
    }

    public void setFromRunningBalance(long fromRunningBalance) {
        this.fromRunningBalance = fromRunningBalance;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    public long getToAmount() {
        return toAmount;
    }

    public void setToAmount(long toAmount) {
        this.toAmount = toAmount;
    }

    public long getToRunningBalance() {
        return toRunningBalance;
    }

    public void setToRunningBalance(long toRunningBalance) {
        this.toRunningBalance = toRunningBalance;
    }

    public Payee getPayee() {
        return payee;
    }

    public void setPayee(Payee payee) {
        this.payee = payee;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getOriginalCurrency() {
        return originalCurrency;
    }

    public void setOriginalCurrency(String originalCurrency) {
        this.originalCurrency = originalCurrency;
    }

    public long getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(long originalAmount) {
        this.originalAmount = originalAmount;
    }

    public Date getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Date occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Date getClearedAt() {
        return clearedAt;
    }

    public void setClearedAt(Date clearedAt) {
        this.clearedAt = clearedAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public RealmList<TransactionSplit> getSplits() {
        return splits;
    }

    public void setSplits(RealmList<TransactionSplit> splits) {
        this.splits = splits;
    }

}
