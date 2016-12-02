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
import android.support.v7.widget.RecyclerView;

/**
 * A {@link android.support.v7.widget.RecyclerView.Adapter} that allows selecting a single
 * item from the list.
 */
public abstract class SingleSelectionAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    public static final int INVALID_POSITION = -1;

    private static final String STATE_KEY_SELECTED_POSITION = "SingleSelectionSelectedPosition";

    private int mSelectedItemPosition = INVALID_POSITION;

    /**
     * Return the selected state of the specified position.
     *
     * @param position The item whose selected state to return
     * @return The item's selected state
     */
    public boolean isItemSelected(int position) {
        return position == mSelectedItemPosition;
    }

    /**
     * Return the position of the currently selected item.
     *
     * @return The position of the currently selected item or {@link #INVALID_POSITION} if nothing is
     * selected
     */
    public int getSelectedItemPosition() {
        return mSelectedItemPosition;
    }

    /**
     * Set the selected state of the specified position.
     *
     * @param position The item whose selected state is to be set
     * @param selected The new selected state for the item
     */
    public void setItemSelected(int position, boolean selected) {

        if (selected) {
            // First, check if another item was previously selected
            if (mSelectedItemPosition != position) {
                clearSelection();
            }
            mSelectedItemPosition = position;
            notifyItemChanged(position);
        } else {
            // When asked to deselect, only do it if the item was previously selected
            if (mSelectedItemPosition == position) {
                mSelectedItemPosition = INVALID_POSITION;
                notifyItemChanged(position);
            }
        }
    }

    /**
     * Clear current selected item.
     */
    public void clearSelection() {
        int oldPosition = mSelectedItemPosition;
        if (oldPosition != INVALID_POSITION) {
            mSelectedItemPosition = INVALID_POSITION;
            notifyItemChanged(oldPosition);
        }
    }

    /**
     * Check if the adapter has an item selected or not.
     *
     * @return Returns true if the adapter has at least 1 item selected, false otherwise
     */
    public boolean isActivated() {
        return mSelectedItemPosition != INVALID_POSITION;
    }

    /**
     * Called to save the the state of the adapter.
     *
     * @param state Bundle in which to set the saved state.
     */
    public void onSaveInstanceState(Bundle state) {
        state.putInt(STATE_KEY_SELECTED_POSITION, mSelectedItemPosition);
    }

    /**
     * Called to restore the state of the adapter from a previously saved bundle.
     *
     * @param state The data most recently supplied in {@link #onSaveInstanceState}
     */
    public void onRestoreInstanceState(Bundle state) {
        mSelectedItemPosition = state.getInt(STATE_KEY_SELECTED_POSITION, INVALID_POSITION);
    }
}
