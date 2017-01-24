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

package ro.expectations.expenses.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmResults;

public abstract class RealmSpinnerAdapter<T extends RealmModel> extends BaseAdapter implements ThemedSpinnerAdapter {

    @Nullable
    private RealmResults<T> adapterData;
    private RealmChangeListener<RealmResults<T>> listener;

    private final LayoutInflater inflater;
    private final ThemedSpinnerAdapter.Helper mDropDownHelper;

    public RealmSpinnerAdapter(@NonNull Context context, @Nullable RealmResults<T> data) {
        adapterData = data;
        listener = new RealmChangeListener<RealmResults<T>>() {
            @Override
            public void onChange(RealmResults<T> element) {
                notifyDataSetChanged();
            }
        };
        if (adapterData != null) {
            adapterData.addChangeListener(listener);
        }
        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void setDropDownViewTheme(@Nullable Resources.Theme theme) {
        mDropDownHelper.setDropDownViewTheme(theme);
    }

    @Nullable
    @Override
    public Resources.Theme getDropDownViewTheme() {
        return mDropDownHelper.getDropDownViewTheme();
    }

    @Override
    public int getCount() {
        if (adapterData == null) {
            return 0;
        }
        return adapterData.size();
    }

    @Override
    public Object getItem(int position) {
        if (adapterData == null) {
            return null;
        }
        return adapterData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int selectedItemPosition = position;
        if (parent instanceof AdapterView) {
            selectedItemPosition = ((AdapterView) parent).getSelectedItemPosition();
        }

        TextView tv;
        if (convertView != null) {
            tv = (TextView) convertView;
        } else {
            tv = (TextView) inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        if (adapterData != null) {
            String item = getItemData(selectedItemPosition);
            tv.setText(item);
        }

        return tv;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView tv;
        if (convertView != null) {
            tv = (TextView) convertView;
        } else {
            LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
            tv = (TextView) inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        if (adapterData != null) {
            String item = getItemData(position);
            tv.setText(item);
        }

        return tv;
    }

    public void updateData(@Nullable RealmResults<T> data) {

        if (listener != null) {
            if (adapterData != null) {
                adapterData.removeChangeListener(listener);
            }
            if (data != null) {
                data.addChangeListener(listener);
            }
        }

        adapterData = data;
        notifyDataSetChanged();
    }

    public abstract String getItemData(int position);
}