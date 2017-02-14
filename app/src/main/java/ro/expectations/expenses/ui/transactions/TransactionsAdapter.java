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

package ro.expectations.expenses.ui.transactions;

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
import ro.expectations.expenses.model.Category;
import ro.expectations.expenses.model.Payee;
import ro.expectations.expenses.model.Transaction;
import ro.expectations.expenses.ui.recyclerview.MultipleSelection;
import ro.expectations.expenses.ui.recyclerview.MultipleSelectionHelper;
import ro.expectations.expenses.ui.utils.ListUtils;
import ro.expectations.expenses.utils.ColorUtils;
import ro.expectations.expenses.utils.DrawableUtils;
import ro.expectations.expenses.utils.NumberUtils;

public class TransactionsAdapter
        extends RealmRecyclerViewAdapter<Transaction, TransactionsAdapter.ViewHolder>
        implements MultipleSelection {

    final private long mSelectedAccountId;

    private final MultipleSelectionHelper mMultipleSelectionHelper;

    public TransactionsAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Transaction> data,
                               long selectedAccountId, boolean autoUpdate) {
        super(context, data, autoUpdate);
        mSelectedAccountId = selectedAccountId;
        mMultipleSelectionHelper = new MultipleSelectionHelper(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_transactions, parent, false);
        return new TransactionsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Transaction transaction = getData().get(position);

        Account fromAccount = transaction.getFromAccount();
        Account toAccount = transaction.getToAccount();

        if (fromAccount != null && toAccount != null) {
            processTransfer(holder, position);
        } else {
            if (fromAccount != null) {
                processDebit(holder, position);
            } else {
                processCredit(holder, position);
            }
        }

        // Set the row background
        ListUtils.setItemBackground(context, holder.itemView, isItemSelected(position),
                holder.mTransactionIconBackground, holder.mSelectedIconBackground);

        Category category = transaction.getCategory();

        // Set the description
        StringBuilder description = new StringBuilder();
        if (transaction.getSplits().size() > 0) {
            description.append(context.getString(R.string.multiple_categories));
        } else if (category != null) {
            description.append(category.getName());
            Category parentCategory = category.getParentCategory();
            if (parentCategory != null) {
                description.insert(0, " » ");
                description.insert(0, parentCategory.getName());
            }
        }

        StringBuilder additionalDescription = new StringBuilder();
        Payee payee = transaction.getPayee();
        if (payee != null) {
            additionalDescription.append(payee.getName());
        }
        String note = transaction.getNote();
        if (note != null && !note.isEmpty()) {
            if (additionalDescription.length() > 0) {
                additionalDescription.append(": ");
            }
            additionalDescription.append(note);
        }
        if (description.length() > 0 && additionalDescription.length() > 0) {
            additionalDescription.insert(0, " (");
            additionalDescription.append(")");
        }
        if (additionalDescription.length() > 0) {
            description.append(additionalDescription.toString());
        }
        if (description.length() == 0) {
            if (fromAccount != null && toAccount != null) {
                description.append(context.getString(R.string.default_transfer_description));
            } else {
                if (fromAccount != null) {
                    description.append(context.getString(R.string.default_debit_description));
                } else {
                    description.append(context.getString(R.string.default_credit_description));
                }
            }
        }
        holder.mDescription.setText(description.toString());

        // Set the transaction date
        Date transactionDate = transaction.getOccurredAt();
        if (transactionDate != null) {
            transactionDate = transaction.getClearedAt();
        }
        if (transactionDate != null) {
            holder.mDate.setText(DateUtils.getRelativeTimeSpanString(transactionDate.getTime(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS));
        }

        // Set the icon
        if (fromAccount != null && toAccount != null) {
            holder.mTransactionIcon.setImageResource(R.drawable.ic_transfer_black_24dp);
        } else {
            int iconId;
            String iconName = "";
            if (category != null) {
                iconName = category.getIcon();
            }
            if (iconName == null || iconName.isEmpty()) {
                iconId = R.drawable.ic_question_mark_black_24dp;
            } else {
                iconId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                if (iconId == 0) {
                    iconId = R.drawable.ic_question_mark_black_24dp;
                }
            }
            holder.mTransactionIcon.setImageResource(iconId);
        }

        // Set the icon background color
        int color;
        int defaultColor = ContextCompat.getColor(context, R.color.colorPrimary);
        if (category != null) {
            color = ColorUtils.fromRGB(category.getColor(), defaultColor);
        } else {
            color = defaultColor;
        }
        holder.mTransactionIconBackground
                .getBackground()
                .setColorFilter(color, PorterDuff.Mode.SRC_IN);

        holder.mSelectedIconBackground
                .getBackground()
                .setColorFilter(ContextCompat.getColor(context, R.color.colorGrey600),
                        PorterDuff.Mode.SRC_IN);
    }

    private void processTransfer(ViewHolder holder, int position) {

        Transaction transaction = getData().get(position);

        // Set the account
        Account fromAccount = transaction.getFromAccount();
        Account toAccount = transaction.getToAccount();
        holder.mAccount.setText(context.getResources().getString(R.string.breadcrumbs, fromAccount.getTitle(), toAccount.getTitle()));

        // Set the amount
        NumberFormat format = NumberFormat.getCurrencyInstance();
        String fromCurrencyCode = fromAccount.getCurrency();
        String toCurrencyCode = toAccount.getCurrency();
        if (fromCurrencyCode.equals(toCurrencyCode)) {
            double amount = NumberUtils.roundToTwoPlaces(transaction.getFromAmount() / 100.0);
            Currency currency = Currency.getInstance(fromCurrencyCode);
            format.setCurrency(currency);
            format.setMaximumFractionDigits(currency.getDefaultFractionDigits());
            holder.mAmount.setText(format.format(amount));

            double fromBalance = NumberUtils.roundToTwoPlaces(transaction.getFromRunningBalance() / 100.0);
            String fromBalanceFormatted = format.format(fromBalance);
            double toBalance = NumberUtils.roundToTwoPlaces(transaction.getToRunningBalance() / 100.0);
            holder.mRunningBalance.setText(context.getResources().getString(R.string.breadcrumbs, fromBalanceFormatted, format.format(toBalance)));
        } else {
            Currency fromCurrency = Currency.getInstance(fromCurrencyCode);
            format.setCurrency(fromCurrency);
            format.setMaximumFractionDigits(fromCurrency.getDefaultFractionDigits());
            double fromAmount = NumberUtils.roundToTwoPlaces(transaction.getFromAmount() / 100.0);
            String fromAmountFormatted = format.format(fromAmount);
            double fromBalance = NumberUtils.roundToTwoPlaces(transaction.getFromRunningBalance() / 100.0);
            String fromBalanceFormatted = format.format(fromBalance);

            Currency toCurrency = Currency.getInstance(toCurrencyCode);
            format.setCurrency(toCurrency);
            format.setMaximumFractionDigits(toCurrency.getDefaultFractionDigits());
            double toAmount = NumberUtils.roundToTwoPlaces(transaction.getToAmount() / 100.0);
            double toBalance = NumberUtils.roundToTwoPlaces(transaction.getToRunningBalance() / 100.0);

            holder.mAmount.setText(context.getResources().getString(R.string.breadcrumbs, fromAmountFormatted, format.format(toAmount)));
            holder.mRunningBalance.setText(context.getResources().getString(R.string.breadcrumbs, fromBalanceFormatted, format.format(toBalance)));
        }

        // Set the color for the amount and the transaction type icon
        if (mSelectedAccountId == 0) {
            holder.mAmount.setTextColor(ContextCompat.getColor(context, R.color.colorOrange700));
            holder.mTypeIcon.setImageDrawable(DrawableUtils.tint(context, R.drawable.ic_swap_horiz_black_24dp, R.color.colorOrange700));
        } else {
            if (mSelectedAccountId == fromAccount.getId()) {
                holder.mAmount.setTextColor(ContextCompat.getColor(context, R.color.colorRed700));
                holder.mTypeIcon.setImageDrawable(DrawableUtils.tint(context, R.drawable.ic_call_made_black_24dp, R.color.colorRed700));
            } else if (mSelectedAccountId == toAccount.getId()) {
                holder.mAmount.setTextColor(ContextCompat.getColor(context, R.color.colorGreen700));
                holder.mTypeIcon.setImageDrawable(DrawableUtils.tint(context, R.drawable.ic_call_received_black_24dp, R.color.colorGreen700));
            }
        }
    }



    private void processDebit(ViewHolder holder, int position) {

        Transaction transaction = getData().get(position);

        // Set the account
        Account fromAccount = transaction.getFromAccount();
        holder.mAccount.setText(fromAccount.getTitle());

        // Set the amount
        double fromAmount = NumberUtils.roundToTwoPlaces(0 - transaction.getFromAmount() / 100.0);
        NumberFormat format = NumberFormat.getCurrencyInstance();
        String fromCurrencyCode = fromAccount.getCurrency();
        Currency fromCurrency = Currency.getInstance(fromCurrencyCode);
        format.setCurrency(fromCurrency);
        format.setMaximumFractionDigits(fromCurrency.getDefaultFractionDigits());
        holder.mAmount.setText(format.format(fromAmount));
        holder.mAmount.setTextColor(ContextCompat.getColor(context, R.color.colorRed700));

        double fromBalance = NumberUtils.roundToTwoPlaces(transaction.getFromRunningBalance() / 100.0);
        holder.mRunningBalance.setText(format.format(fromBalance));

        // Set the transaction type icon
        holder.mTypeIcon.setImageDrawable(DrawableUtils.tint(context, R.drawable.ic_call_made_black_24dp, R.color.colorRed700));
    }

    private void processCredit(ViewHolder holder, int position) {

        Transaction transaction = getData().get(position);

        // Set the account
        Account toAccount = transaction.getToAccount();
        holder.mAccount.setText(toAccount.getTitle());

        // Set the amount
        double toAmount = NumberUtils.roundToTwoPlaces(transaction.getToAmount() / 100.0);
        NumberFormat format = NumberFormat.getCurrencyInstance();
        String toCurrencyCode = toAccount.getCurrency();
        Currency toCurrency = Currency.getInstance(toCurrencyCode);
        format.setCurrency(toCurrency);
        format.setMaximumFractionDigits(toCurrency.getDefaultFractionDigits());
        holder.mAmount.setText(format.format(toAmount));
        holder.mAmount.setTextColor(ContextCompat.getColor(context, R.color.colorGreen700));

        double toBalance = NumberUtils.roundToTwoPlaces(transaction.getToRunningBalance() / 100.0);
        holder.mRunningBalance.setText(format.format(toBalance));

        // Set the transaction type icon
        holder.mTypeIcon.setImageDrawable(DrawableUtils.tint(context, R.drawable.ic_call_received_black_24dp, R.color.colorGreen700));
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

        final RelativeLayout mTransactionIconBackground;
        final ImageView mTransactionIcon;
        final RelativeLayout mSelectedIconBackground;
        final ImageView mSelectedIcon;
        final TextView mAccount;
        final TextView mDescription;
        final TextView mDate;
        final ImageView mTypeIcon;
        final TextView mAmount;
        final TextView mRunningBalance;


        ViewHolder(View itemView) {
            super(itemView);
            mTransactionIconBackground = (RelativeLayout) itemView.findViewById(R.id.transaction_icon_background);
            mTransactionIcon = (ImageView) itemView.findViewById(R.id.transaction_icon);
            mSelectedIconBackground = (RelativeLayout) itemView.findViewById(R.id.selected_icon_background);
            mSelectedIcon = (ImageView) itemView.findViewById(R.id.selected_icon);
            mAccount = (TextView) itemView.findViewById(R.id.transaction_account);
            mDescription = (TextView) itemView.findViewById(R.id.transaction_description);
            mDate = (TextView) itemView.findViewById(R.id.transaction_date);
            mTypeIcon = (ImageView) itemView.findViewById(R.id.transaction_type_icon);
            mAmount = (TextView) itemView.findViewById(R.id.transaction_amount);
            mRunningBalance = (TextView) itemView.findViewById(R.id.account_running_balance);
        }
    }
}
