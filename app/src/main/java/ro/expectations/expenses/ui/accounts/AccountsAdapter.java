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

package ro.expectations.expenses.ui.accounts;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import ro.expectations.expenses.R;
import ro.expectations.expenses.model.Account;
import ro.expectations.expenses.model.AccountType;
import ro.expectations.expenses.model.CardType;
import ro.expectations.expenses.model.OnlineAccountType;
import ro.expectations.expenses.ui.recyclerview.MultipleSelection;
import ro.expectations.expenses.ui.recyclerview.MultipleSelectionHelper;
import ro.expectations.expenses.ui.utils.ListUtils;
import ro.expectations.expenses.utils.NumberUtils;

public class AccountsAdapter extends RealmRecyclerViewAdapter<Account, AccountsAdapter.ViewHolder> implements MultipleSelection {

    private final MultipleSelectionHelper mMultipleSelectionHelper;

    public AccountsAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Account> data, boolean autoUpdate) {
        super(context, data, autoUpdate);
        mMultipleSelectionHelper = new MultipleSelectionHelper(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_accounts, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Account account = getData().get(position);

        // Set the row background
        ListUtils.setItemBackground(context, holder.itemView, isItemSelected(position),
                holder.mAccountIconBackground, holder.mSelectedIconBackground);

        // Set the icon
        AccountType accountType = AccountType.fromString(account.getType(), AccountType.OTHER);
        if (accountType == AccountType.CREDIT_CARD || accountType == AccountType.DEBIT_CARD) {
            CardType cardType = CardType.fromString(account.getCardType(), CardType.OTHER);
            holder.mAccountIcon.setImageResource(cardType.iconId);
        } else if (accountType == AccountType.ONLINE) {
            OnlineAccountType onlineAccountType = OnlineAccountType.fromString(account.getOnlineAccountType(), OnlineAccountType.OTHER);
            holder.mAccountIcon.setImageResource(onlineAccountType.iconId);
        } else {
            holder.mAccountIcon.setImageResource(accountType.iconId);
        }

        // Set the icon background color
        holder.mAccountIconBackground
                .getBackground()
                .setColorFilter(ContextCompat.getColor(context, accountType.colorId),
                        PorterDuff.Mode.SRC_IN);
        holder.mSelectedIconBackground
                .getBackground()
                .setColorFilter(ContextCompat.getColor(context, R.color.colorGrey600),
                        PorterDuff.Mode.SRC_IN);

        // Set the description
        holder.mAccountDescription.setText(accountType.titleId);

        // Set the title
        holder.mAccountTitle.setText(account.getTitle());

        // Set the date
        long now = System.currentTimeMillis();
        Date lastTransactionAt = account.getLastTransactionAt();
        if (lastTransactionAt == null) {
            lastTransactionAt = account.getCreatedAt();
        }
        holder.mAccountLastTransactionAt.setText(DateUtils.getRelativeTimeSpanString(lastTransactionAt.getTime(), now, DateUtils.DAY_IN_MILLIS));

        // Set the account balance
        double balance = NumberUtils.roundToTwoPlaces(account.getBalance() / 100.0);
        String currencyCode = account.getCurrency();
        Currency currency = Currency.getInstance(currencyCode);
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setCurrency(currency);
        format.setMaximumFractionDigits(currency.getDefaultFractionDigits());
        holder.mAccountBalance.setText(format.format(balance));
        if (balance > 0) {
            holder.mAccountBalance.setTextColor(ContextCompat.getColor(context, R.color.colorGreen700));
        } else if (balance < 0) {
            holder.mAccountBalance.setTextColor(ContextCompat.getColor(context, R.color.colorRed700));
        }
    }

    @Override
    public boolean hasItemSelected() {
        return mMultipleSelectionHelper.hasItemSelected();
    }

    @Override
    public int getSelectedItemCount() {
        return mMultipleSelectionHelper.getSelectedItemCount();
    }

    @Override
    public List<Integer> getSelectedItemPositions() {
        return mMultipleSelectionHelper.getSelectedItemPositions();
    }

    @Override
    public boolean isItemSelected(int position) {
        return mMultipleSelectionHelper.isItemSelected(position);
    }

    @Override
    public void setItemSelected(int position, boolean selected) {
        mMultipleSelectionHelper.setItemSelected(position, selected);
    }

    @Override
    public void clearSelection() {
        mMultipleSelectionHelper.clearSelection();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        mMultipleSelectionHelper.onSaveInstanceState(state);
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        mMultipleSelectionHelper.onRestoreInstanceState(state);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final RelativeLayout mAccountIconBackground;
        final ImageView mAccountIcon;
        final RelativeLayout mSelectedIconBackground;
        final TextView mAccountTitle;
        final TextView mAccountDescription;
        final TextView mAccountLastTransactionAt;
        final TextView mAccountBalance;

        ViewHolder(View itemView) {
            super(itemView);

            mAccountIconBackground = (RelativeLayout) itemView.findViewById(R.id.account_icon_background);
            mSelectedIconBackground = (RelativeLayout) itemView.findViewById(R.id.selected_icon_background);
            mAccountIcon = (ImageView) itemView.findViewById(R.id.account_icon);
            mAccountTitle = (TextView) itemView.findViewById(R.id.account_title);
            mAccountDescription = (TextView) itemView.findViewById(R.id.account_description);
            mAccountLastTransactionAt = (TextView) itemView.findViewById(R.id.account_last_transaction_at);
            mAccountBalance = (TextView) itemView.findViewById(R.id.account_balance);
        }
    }
}
