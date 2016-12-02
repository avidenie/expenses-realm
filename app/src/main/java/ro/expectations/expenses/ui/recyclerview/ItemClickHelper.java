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

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Helper class that allows attaching different type of click listeners on
 * {@link RecyclerView} items.
 */
public class ItemClickHelper {

    private final RecyclerView mRecyclerView;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mOnItemClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
                mOnItemClickListener.onItemClick(mRecyclerView, view, holder.getAdapterPosition());
            }
        }
    };

    private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if (mOnItemLongClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
                return mOnItemLongClickListener.onItemLongClick(mRecyclerView, view, holder.getAdapterPosition());
            }
            return false;
        }
    };

    private final RecyclerView.OnChildAttachStateChangeListener mAttachListener
            = new RecyclerView.OnChildAttachStateChangeListener() {
        @Override
        public void onChildViewAttachedToWindow(View view) {
            if (mOnItemClickListener != null) {
                view.setOnClickListener(mOnClickListener);
            }
            if (mOnItemLongClickListener != null) {
                view.setOnLongClickListener(mOnLongClickListener);
            }
        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {

        }
    };

    public ItemClickHelper(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener);
    }

    /**
     * Register a callback to be invoked when an item has been clicked.
     *
     * @param listener The callback that will run
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * Register a callback to be invoked when an item has been clicked and held.
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    /**
     * Detach the helper from the {@link RecyclerView}.
     */
    public void detach() {
        mRecyclerView.removeOnChildAttachStateChangeListener(mAttachListener);
    }

    /**
     * Interface definition for a callback to be invoked when the
     * {@link RecyclerView} item is clicked.
     */
    public interface OnItemClickListener {
        void onItemClick(RecyclerView parent, View view, int position);
    }

    /**
     * Interface definition for a callback to be invoked when the
     * {@link RecyclerView} has been clicked and held.
     */
    public interface OnItemLongClickListener {
        boolean onItemLongClick(RecyclerView parent, View view, int position);
    }
}
