package com.realsil.WifiConfig.base;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.realsil.WifiConfig.R;
import com.realsil.WifiConfig.utility.SPWristbandConfigInfo;
import com.realsil.sdk.core.BuildConfig;
import com.realsil.sdk.core.logger.ZLogger;


/**
 * Created by bingshanguxue on 19/06/2017.
 */

public class BaseFragment extends Fragment {
    private Toast mToast;
    private ProgressDialog mProgressDialog = null;
    private static final int REQUEST_CODE_APPLICATION_SETTINGS = 0x11;
    private Handler mProgressBarSuperHandler = new Handler();
    private static final long PROGRESS_BAR_TIMEOUT = 30 * 1000;
    public void showProgressBar(final int messageResId) {
        showProgressBar(getResources().getString(messageResId));
    }

    public void showProgressBar(final String message) {
        ZLogger.w("showProgressBar");
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                ZLogger.w("ProgressBar is showing");
                return;
            }
        }
        mProgressDialog = ProgressDialog.show(getActivity()
                , null
                , message
                , true);
        mProgressDialog.setCancelable(false);

        mProgressBarSuperHandler.postDelayed(mProgressBarSuperTask, PROGRESS_BAR_TIMEOUT);
    }

    public void cancelProgressBar() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }
        ZLogger.w("removeCallbacks");
        mProgressBarSuperHandler.removeCallbacks(mProgressBarSuperTask);
    }

    public void onProgressBarTimeout() {
        ZLogger.w("Wait Progress Timeout");
        showToast(R.string.progress_bar_timeout);
        cancelProgressBar();
    }

    private Runnable mProgressBarSuperTask = new Runnable() {
        @Override
        public void run() {
            onProgressBarTimeout();
        }
    };

    protected void showToast(final String message) {
        if (mToast == null) {
            mToast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    protected void showToast(final int message) {
        if (mToast == null) {
            mToast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    public boolean isFirstLoad() {
        return SPWristbandConfigInfo.getFirstAppStartFlag(getActivity());
    }

    protected void showPermissionRationaleDialog() {
        AlertDialog.Builder aa = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert);
        aa.setTitle(R.string.permission_failure_title);
        aa.setMessage(R.string.permission_failure_text);
        aa.setPositiveButton(R.string.permission_failure_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                route2ReqPermission();
            }
        });
        aa.setNegativeButton(R.string.permission_failure_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onRationaleCancel();
            }
        });
        aa.setCancelable(false);
        aa.create();
        aa.show();
    }

    public void onRationaleCancel(){
        ZLogger.w("onRationaleCancel");
    }

    private void route2ReqPermission() {
        //HUAWEI
        ZLogger.d("Build.MANUFACTURER: " + Build.MANUFACTURER);
        ZLogger.d("Build.BRAND: " + Build.BRAND);
        ZLogger.d("Build.MODEL: " + Build.MODEL);
        ZLogger.d("Build.BOOTLOADER: " + Build.BOOTLOADER);
        Intent intent = new Intent();
        if ("HUAWEI".equals(Build.MANUFACTURER)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
            intent.setComponent(comp);
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.applications.InstalledAppDetails"));
            intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
        }

        try {
            ResolveInfo res = getActivity().getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (res != null) {
                startActivityForResult(intent, REQUEST_CODE_APPLICATION_SETTINGS);
            } else {
                ZLogger.d("cannot resolve permission activity");
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }

    }
}
