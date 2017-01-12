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

import java.util.List;

/**
 * A {@link android.support.v7.widget.RecyclerView.Adapter} that allows selecting multiple
 * items from the list.
 */
public interface MultipleSelection {

    /**
     * Check if the adapter has at least 1 item selected or not.
     *
     * @return Returns true if the adapter has at least 1 item selected, false otherwise
     */
    boolean hasItemSelected();

    /**
     * Return the number of items currently selected.
     *
     * To determine the specific items that are currently selected, use
     * the {@link #getSelectedItemPositions} method.
     *
     * @return The number of items currently selected
     */
    int getSelectedItemCount();

    /**
     * Return the set of selected item positions in the list.
     *
     * @return All selected item positions in the list
     */
    List<Integer> getSelectedItemPositions();

    /**
     * Return the selected state of the specified position.
     *
     * @param position The item whose selected state to return
     * @return The item's selected state
     */
    boolean isItemSelected(int position);

    /**
     * Set the selected state of the specified position.
     *
     * @param position The item whose selected state is to be set
     * @param selected The new selected state for the item
     */
    void setItemSelected(int position, boolean selected);

    /**
     * Clear currently selected items.
     */
    void clearSelection();

    /**
     * Called to save the the state of the adapter.
     *
     * @param state Bundle in which to set the saved state.
     */
    void onSaveInstanceState(Bundle state);

    /**
     * Called to restore the state of the adapter from a previously saved bundle.
     *
     * @param state The data most recently supplied in {@link #onSaveInstanceState}
     */
    void onRestoreInstanceState(Bundle state);
}
