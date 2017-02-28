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
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import ro.expectations.expenses.R;
import ro.expectations.expenses.model.Payee;
import ro.expectations.expenses.ui.drawer.DrawerActivity;
import ro.expectations.expenses.ui.provider.AppBarLayoutProvider;
import ro.expectations.expenses.ui.recyclerview.ItemClickHelper;
import ro.expectations.expenses.utils.ColorUtils;

public class PayeesFragment extends Fragment {

    private Realm mRealm;

    private PayeesAdapter mAdapter;
    private TextView mEmptyView;
    private FrameLayout mVerticalCenterWrapper;

    private int mStatusBarColor;

    private AppBarLayoutProvider mAppBarLayoutProvider;

    private ActionMode mActionMode;
    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu_payees, menu);

            ((DrawerActivity) getActivity()).lockNavigationDrawer();

            menu.findItem(R.id.action_edit_payee)
                    .getIcon()
                    .setColorFilter(ContextCompat.getColor(getContext(), R.color.colorWhite),
                            PorterDuff.Mode.SRC_IN);
            menu.findItem(R.id.action_delete_payee)
                    .getIcon()
                    .setColorFilter(ContextCompat.getColor(getContext(), R.color.colorWhite),
                            PorterDuff.Mode.SRC_IN);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int primaryColorDark = ColorUtils.getColorFromTheme(getActivity(), R.attr.colorPrimaryDark);
                getActivity().getWindow().setStatusBarColor(0XFF000000 | primaryColorDark);
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            int selectedPayees = mAdapter.getSelectedItemCount();
            mode.setTitle(getResources().getQuantityString(
                    R.plurals.selected_payees,
                    selectedPayees,
                    selectedPayees
            ));
            if (mAdapter.getSelectedItemCount() == 1) {
                menu.findItem(R.id.action_edit_payee).setVisible(true);
            } else {
                menu.findItem(R.id.action_edit_payee).setVisible(false);
            }

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            switch(id) {
                case R.id.action_edit_payee:
                    if (mAdapter.getSelectedItemCount() == 1) {
                        int position = mAdapter.getSelectedItemPositions().get(0);
                        long payeeId = mAdapter.getItemId(position);
                        Intent editPayeeIntent = new Intent(getActivity(), ManagePayeeActivity.class);
                        editPayeeIntent.putExtra(ManagePayeeActivity.ARG_PAYEE_ID, payeeId);
                        startActivity(editPayeeIntent);
                    }
                    mode.finish();
                    return true;
                case R.id.action_delete_payee:
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

            mActionMode = null;
            if (mAdapter.hasItemSelected()) {
                mAdapter.clearSelection();
            }

            ((DrawerActivity) getActivity()).unlockNavigationDrawer();

            // reset the status bar color to default
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActivity().getWindow().setStatusBarColor(mStatusBarColor);
            }
        }
    };

    public static PayeesFragment newInstance() {
        return new PayeesFragment();
    }

    public PayeesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof AppBarLayoutProvider) {
            mAppBarLayoutProvider = (AppBarLayoutProvider) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AppBarLayoutProvider");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payees, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mStatusBarColor = getActivity().getWindow().getStatusBarColor();
        }

        mEmptyView = (TextView) view.findViewById(R.id.list_payees_empty);

        mVerticalCenterWrapper = (FrameLayout) view.findViewById(R.id.vertical_center_wrapper);
        mAppBarLayoutProvider.getAppBarLayout().addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int paddingBottom = appBarLayout.getTotalScrollRange() - Math.abs(verticalOffset);
                mVerticalCenterWrapper.setPaddingRelative(0, 0, 0, paddingBottom);
            }
        });

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list_payees);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);

        RealmResults<Payee> payees = mRealm.where(Payee.class)
                .findAllSortedAsync(Payee.NAME, Sort.ASCENDING);
        payees.addChangeListener(new RealmChangeListener<RealmResults<Payee>>() {
            @Override
            public void onChange(RealmResults<Payee> payees) {
                mEmptyView.setVisibility(payees.size() > 0 ? View.GONE : View.VISIBLE);
            }
        });
        mAdapter = new PayeesAdapter(getActivity(), payees, true);
        recyclerView.setAdapter(mAdapter);

        ItemClickHelper itemClickHelper = new ItemClickHelper(recyclerView);
        itemClickHelper.setOnItemClickListener(new ItemClickHelper.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position) {
                boolean isItemSelected = mAdapter.isItemSelected(position);
                if (isItemSelected) {
                    mAdapter.setItemSelected(position, false);
                    if (!mAdapter.hasItemSelected()) {
                        mActionMode.finish();
                    } else {
                        mActionMode.invalidate();
                    }
                } else if (mAdapter.hasItemSelected()) {
                    mAdapter.setItemSelected(position, true);
                    mActionMode.invalidate();
                }
            }
        });
        itemClickHelper.setOnItemLongClickListener(new ItemClickHelper.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView parent, View view, int position) {
                mAdapter.setItemSelected(position, !mAdapter.isItemSelected(position));
                if (mAdapter.hasItemSelected()) {
                    if (mActionMode == null) {
                        mActionMode = ((PayeesActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                    } else {
                        mActionMode.invalidate();
                    }
                } else {
                    if (mActionMode != null) {
                        mActionMode.finish();
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mAdapter.onRestoreInstanceState(savedInstanceState);
            if (mAdapter.hasItemSelected() && mActionMode == null) {
                mActionMode = ((PayeesActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            }
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAppBarLayoutProvider = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}
