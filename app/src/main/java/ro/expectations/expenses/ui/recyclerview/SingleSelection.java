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

/**
 * A {@link android.support.v7.widget.RecyclerView.Adapter} that allows selecting a single
 * item from the list.
 */
public interface SingleSelection {

    int INVALID_POSITION = -1;

    /**
     * Check if the adapter has an item selected or not.
     *
     * @return Returns true if the adapter has an item selected, false otherwise
     */
    boolean hasItemSelected();

    /**
     * Return the position of the currently selected item.
     *
     * @return The position of the currently selected item or {@link #INVALID_POSITION} if nothing is
     * selected
     */
    int getSelectedItemPosition();

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
     * Clear current selected item.
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
