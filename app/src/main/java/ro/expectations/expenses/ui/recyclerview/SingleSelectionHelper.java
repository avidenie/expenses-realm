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

package ro.expectations.expenses.ui.recyclerview;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

public final class SingleSelectionHelper implements SingleSelection {

    private static final String STATE_KEY_SELECTED_POSITION = "SingleSelectionHelper::SelectedPosition";

    private int mSelectedItemPosition = INVALID_POSITION;

    private RecyclerView.Adapter mAdapter;

    public SingleSelectionHelper() {
    }

    public SingleSelectionHelper(RecyclerView.Adapter adapter) {
        this.mAdapter = adapter;
    }

    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(RecyclerView.Adapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    @Override
    public boolean isItemSelected(int position) {
        return position == mSelectedItemPosition;
    }

    @Override
    public void setItemSelected(int position, boolean selected) {

        if (selected) {
            // First, check if another item was previously selected
            if (mSelectedItemPosition != position) {
                clearSelection();
            }
            mSelectedItemPosition = position;
            if (mAdapter != null) {
                mAdapter.notifyItemChanged(position);
            }
        } else {
            // When asked to deselect, only do it if the item was previously selected
            if (mSelectedItemPosition == position) {
                mSelectedItemPosition = INVALID_POSITION;
                if (mAdapter != null) {
                    mAdapter.notifyItemChanged(position);
                }
            }
        }
    }

    @Override
    public boolean hasItemSelected() {
        return mSelectedItemPosition != INVALID_POSITION;
    }

    @Override
    public int getSelectedItemPosition() {
        return mSelectedItemPosition;
    }

    @Override
    public void clearSelection() {
        int oldPosition = mSelectedItemPosition;
        if (oldPosition != INVALID_POSITION) {
            mSelectedItemPosition = INVALID_POSITION;
            if (mAdapter != null) {
                mAdapter.notifyItemChanged(oldPosition);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putInt(STATE_KEY_SELECTED_POSITION, mSelectedItemPosition);
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        mSelectedItemPosition = state.getInt(STATE_KEY_SELECTED_POSITION, INVALID_POSITION);
    }
}
