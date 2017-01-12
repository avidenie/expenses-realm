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

package ro.expectations.expenses.ui.recyclerview;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.List;

public class MultipleSelectionHelper implements MultipleSelection {

    private static final String STATE_KEY_SELECTED_ITEMS = "MultipleSelectionHelper::SelectedItems";
    private static final String STATE_KEY_SELECTED_COUNT = "MultipleSelectionHelper::SelectedCount";

    private SelectedItems mSelectedItems = new SelectedItems();
    private int mSelectedItemsCount;

    private RecyclerView.Adapter mAdapter;

    public MultipleSelectionHelper() {
    }

    public MultipleSelectionHelper(RecyclerView.Adapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        this.mAdapter = adapter;
    }

    @Override
    public boolean hasItemSelected() {
        return mSelectedItemsCount > 0;
    }

    @Override
    public int getSelectedItemCount() {
        return mSelectedItemsCount;
    }

    @Override
    public List<Integer> getSelectedItemPositions() {
        List<Integer> items = new ArrayList<>();
        for (int i = 0; i < mSelectedItems.size(); ++i) {
            if (mSelectedItems.valueAt(i)) {
                items.add(mSelectedItems.keyAt(i));
            }
        }
        return items;
    }

    @Override
    public boolean isItemSelected(int position) {
        return mSelectedItems.get(position);
    }

    @Override
    public void setItemSelected(int position, boolean selected) {
        boolean oldValue = mSelectedItems.get(position);

        if (selected) {
            mSelectedItems.put(position, true);
        } else {
            mSelectedItems.delete(position);
        }

        if (oldValue != selected) {
            if (selected) {
                mSelectedItemsCount++;
            } else {
                mSelectedItemsCount--;
            }
            if (mAdapter != null) {
                mAdapter.notifyItemChanged(position);
            }
        }
    }

    @Override
    public void clearSelection() {
        List<Integer> selection = getSelectedItemPositions();
        mSelectedItems.clear();
        mSelectedItemsCount = 0;
        for (Integer i: selection) {
            if (mAdapter != null) {
                mAdapter.notifyItemChanged(i);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putParcelable(STATE_KEY_SELECTED_ITEMS, mSelectedItems);
        state.putInt(STATE_KEY_SELECTED_COUNT, mSelectedItemsCount);
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        mSelectedItems = state.getParcelable(STATE_KEY_SELECTED_ITEMS);
        if (mSelectedItems == null) {
            mSelectedItems = new SelectedItems();
        }
        mSelectedItemsCount = state.getInt(STATE_KEY_SELECTED_COUNT, 0);
    }

    private static class SelectedItems extends SparseBooleanArray implements Parcelable {

        private static final int FALSE = 0;
        private static final int TRUE = 1;

        public static final Parcelable.Creator<SelectedItems> CREATOR =
                new Parcelable.Creator<SelectedItems>() {

                    @Override
                    public SelectedItems createFromParcel(Parcel in) {
                        return new SelectedItems(in);
                    }

                    @Override
                    public SelectedItems[] newArray(int size) {
                        return new SelectedItems[size];
                    }
                };


        public SelectedItems() {
            super();
        }

        private SelectedItems(Parcel in) {
            final int size = in.readInt();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    final int key = in.readInt();
                    final boolean value = (in.readInt() == TRUE);
                    if (value) {
                        put(key, true);
                    }
                }
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            final int size = size();
            parcel.writeInt(size);

            for (int i = 0; i < size; i++) {
                parcel.writeInt(keyAt(i));
                parcel.writeInt(valueAt(i) ? TRUE : FALSE);
            }
        }
    }
}
