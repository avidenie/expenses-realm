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
import ro.expectations.expenses.model.Payee;
import ro.expectations.expenses.model.AccountType;
import ro.expectations.expenses.model.CardType;
import ro.expectations.expenses.model.OnlineAccountType;
import ro.expectations.expenses.ui.recyclerview.MultipleSelection;
import ro.expectations.expenses.ui.recyclerview.MultipleSelectionHelper;
import ro.expectations.expenses.ui.utils.ListUtils;
import ro.expectations.expenses.utils.NumberUtils;

public class PayeesAdapter extends RealmRecyclerViewAdapter<Payee, PayeesAdapter.ViewHolder> implements MultipleSelection {

    private final MultipleSelectionHelper mMultipleSelectionHelper;

    public PayeesAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Payee> data, boolean autoUpdate) {
        super(context, data, autoUpdate);
        mMultipleSelectionHelper = new MultipleSelectionHelper(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_payees, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Payee payee = getData().get(position);

        // Set the row background
        ListUtils.setItemBackground(context, holder.itemView, isItemSelected(position));

        // Set the payee name
        holder.mPayeeName.setText(payee.getName());
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

        final TextView mPayeeName;

        ViewHolder(View itemView) {
            super(itemView);

            mPayeeName = (TextView) itemView.findViewById(R.id.payee_name);
        }
    }
}
