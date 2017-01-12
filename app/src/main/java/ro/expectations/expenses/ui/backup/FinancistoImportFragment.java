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

package ro.expectations.expenses.ui.backup;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import ro.expectations.expenses.R;
import ro.expectations.expenses.restore.AbstractRestoreIntentService;
import ro.expectations.expenses.restore.FinancistoImportIntentService;
import ro.expectations.expenses.ui.dialog.AlertDialogFragment;
import ro.expectations.expenses.ui.dialog.ConfirmationDialogFragment;
import ro.expectations.expenses.ui.dialog.ProgressDialogFragment;
import ro.expectations.expenses.ui.overview.OverviewActivity;
import ro.expectations.expenses.ui.recyclerview.ItemClickHelper;
import ro.expectations.expenses.utils.FileUtils;

public class FinancistoImportFragment extends Fragment
        implements ConfirmationDialogFragment.Listener, DialogInterface.OnClickListener {

    private static final String TAG = FinancistoImportFragment.class.getSimpleName();

    private static final int CONFIRMATION_DIALOG_REQUEST_CODE = 0;
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 0;
    private static final String KEY_SELECTED_FILE = "selected_file";

    private LinearLayout mRequestPermissionRationale;
    private TextView mPermissionRationale;
    private Button mRequestPermission;
    private TextView mEmptyView;

    private FinancistoImportAdapter mAdapter;

    private File mSelectedFile;

    private boolean mDismissProgressBar = false;
    private boolean mLaunchAlertDialog = false;

    private ActionMode mActionMode;
    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.cab_import_financisto, menu);

            menu.findItem(R.id.action_financisto_import)
                    .getIcon()
                    .setColorFilter(ContextCompat.getColor(getContext(), R.color.colorWhite),
                            PorterDuff.Mode.SRC_IN);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            mode.setTitle(getString(R.string.selected_single_selection));

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            switch(id) {
                case R.id.action_financisto_import:
                    mSelectedFile = mAdapter.getItem(mAdapter.getSelectedItemPosition());

                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newInstance(
                                activity.getString(R.string.financisto_import_confirmation_title),
                                activity.getString(R.string.financisto_import_confirmation_message),
                                activity.getString(R.string.button_import),
                                activity.getString(R.string.button_cancel), false);
                        confirmationDialogFragment.setTargetFragment(FinancistoImportFragment.this, CONFIRMATION_DIALOG_REQUEST_CODE);
                        confirmationDialogFragment.show(activity.getSupportFragmentManager(), "ConfirmationDialogFragment");
                    }
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
        }
    };

    private BroadcastReceiver mSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hideProgressDialog();
            showAlertDialog();
            if (mActionMode != null) {
                mActionMode.finish();
            }
        }
    };

    private BroadcastReceiver mFailureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Throwable failureException = (Throwable) intent.getSerializableExtra("exception");
            Log.e(TAG, "An error occurred while importing Financisto backup: "
                    + failureException.getMessage(), failureException);
        }
    };

    public FinancistoImportFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_backup, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRequestPermissionRationale = (LinearLayout) view.findViewById(R.id.request_permission_rationale);
        mPermissionRationale = (TextView) view.findViewById(R.id.permission_rationale);
        mRequestPermission = (Button) view.findViewById(R.id.request_permission);
        mEmptyView = (TextView) view.findViewById(R.id.list_backup_empty);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list_backup);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        mAdapter = new FinancistoImportAdapter(getActivity(), new File[0]);
        recyclerView.setAdapter(mAdapter);

        ItemClickHelper itemClickHelper = new ItemClickHelper(recyclerView);
        itemClickHelper.setOnItemClickListener(new ItemClickHelper.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position) {
                boolean isItemSelected = mAdapter.isItemSelected(position);
                if (isItemSelected) {
                    mAdapter.setItemSelected(position, false);
                    if (mActionMode != null) {
                        mActionMode.finish();
                    }
                } else if (mAdapter.hasItemSelected()) {
                    mAdapter.setItemSelected(position, true);
                    if (mActionMode == null) {
                        mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                    } else {
                        mActionMode.invalidate();
                    }
                }
            }
        });
        itemClickHelper.setOnItemLongClickListener(new ItemClickHelper.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(RecyclerView parent, View view, int position) {
                boolean isItemSelected = mAdapter.isItemSelected(position);
                if (isItemSelected) {
                    mAdapter.setItemSelected(position, false);
                    if (mActionMode != null) {
                        mActionMode.finish();
                    }
                } else {
                    mAdapter.setItemSelected(position, true);
                    if (mActionMode == null) {
                        mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                    } else {
                        mActionMode.invalidate();
                    }
                }
                return true;
            }
        });

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            setupRecyclerView();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showRequestPermissionRationale();
            mRequestPermission.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
                }
            });
            mRequestPermission.setVisibility(View.VISIBLE);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mAdapter.onRestoreInstanceState(savedInstanceState);
            mSelectedFile = (File) savedInstanceState.getSerializable(KEY_SELECTED_FILE);
            if (mAdapter.hasItemSelected() && mActionMode == null) {
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
            }
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        localBroadcastManager.registerReceiver(mSuccessReceiver,
                new IntentFilter(FinancistoImportIntentService.ACTION_SUCCESS));
        localBroadcastManager.registerReceiver(mFailureReceiver,
                new IntentFilter(FinancistoImportIntentService.ACTION_FAILURE));

        if (mDismissProgressBar) {
            hideProgressDialog();
            mDismissProgressBar = false;
        }

        if (mLaunchAlertDialog) {
            showAlertDialog();
            mLaunchAlertDialog = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        localBroadcastManager.unregisterReceiver(mSuccessReceiver);
        localBroadcastManager.unregisterReceiver(mFailureReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mAdapter.onSaveInstanceState(outState);
        outState.putSerializable(KEY_SELECTED_FILE, mSelectedFile);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupRecyclerView();
            } else {
                showRequestPermissionRationale();
                mRequestPermission.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onConfirmed(int targetRequestCode) {
        if (targetRequestCode == CONFIRMATION_DIALOG_REQUEST_CODE) {
            Intent financistoImportIntent = new Intent(getActivity(), FinancistoImportIntentService.class);
            financistoImportIntent.putExtra(AbstractRestoreIntentService.ARG_FILE_URI, Uri.fromFile(mSelectedFile).getPath());
            getActivity().startService(financistoImportIntent);
            showProgressDialog();
        }
    }

    @Override
    public void onDenied(int targetRequestCode) {
        // nothing to do
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        Intent intent = new Intent(getActivity(), OverviewActivity.class);
        startActivity(intent);
    }

    private void setupRecyclerView() {
        File financistoBackupFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "financisto");
        File[] files = FileUtils.listFilesWithExtension(financistoBackupFolder, ".backup");
        mAdapter.setFiles(files);
        mRequestPermissionRationale.setVisibility(View.GONE);
        mEmptyView.setText(getString(R.string.no_financisto_backup_found));
        mEmptyView.setVisibility(files.length > 0 ? View.GONE : View.VISIBLE);
    }

    private void showRequestPermissionRationale() {
        String app = getString(R.string.app_name);
        String permissionRationale = String.format(getString(R.string.read_storage_rationale_financisto_import), app, app);
        Spanned text;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            text = Html.fromHtml(permissionRationale,Html.FROM_HTML_MODE_LEGACY);
        } else {
            text = Html.fromHtml(permissionRationale);
        }
        mPermissionRationale.setText(text);
        mRequestPermissionRationale.setVisibility(View.VISIBLE);
    }

    private void showProgressDialog() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            ProgressDialogFragment.newInstance(getString(R.string.financisto_import_progress), false)
                    .show(activity.getSupportFragmentManager(), "ProgressDialogFragment");
        }
    }

    private void showAlertDialog() {
        FragmentActivity activity = getActivity();
        if (activity != null && isResumed()) {
            AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(
                    getString(R.string.success),
                    getString(R.string.financisto_import_successful),
                    false
            );
            alertDialogFragment.setTargetFragment(this, 0);
            alertDialogFragment.show(activity.getSupportFragmentManager(), "AlertDialogFragment");
        } else {
            mLaunchAlertDialog = true;
        }
    }

    private void hideProgressDialog() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            ProgressDialogFragment progressDialogFragment = (ProgressDialogFragment) activity
                    .getSupportFragmentManager().findFragmentByTag("ProgressDialogFragment");
            if (isResumed() && progressDialogFragment != null && progressDialogFragment.getDialog().isShowing()) {
                progressDialogFragment.dismiss();
            } else {
                mDismissProgressBar = true;
            }
        }
    }
}
