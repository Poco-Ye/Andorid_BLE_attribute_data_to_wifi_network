package com.realsil.WifiConfig.base;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.realsil.WifiConfig.R;
import com.realsil.WifiConfig.utility.SPWristbandConfigInfo;
import com.realsil.sdk.core.BuildConfig;
import com.realsil.sdk.core.logger.ZLogger;

/**
 * Created by bingshanguxue on 19/06/2017.
 */

public class BaseActivity extends FragmentActivity {

    private static final int REQUEST_CODE_APPLICATION_SETTINGS = 0x11;

    private Toast mToast;
    private static final long PROGRESS_BAR_TIMEOUT = 30 * 1000;
    private ProgressDialog mProgressDialog = null;
    private Handler mProgressBarSuperHandler = new Handler();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            //window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    protected void showToast(final String message) {
        if (mToast == null) {
            mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    protected void showToast(final int message) {
        if (mToast == null) {
            mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    protected void showLongToast(final String message) {
        if (mToast == null) {
            mToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    public void showProgressBar(final int messageResId) {
        showProgressBar(getResources().getString(messageResId));
    }

    public void showProgressBar(final String message) {
        mProgressDialog = ProgressDialog.show(this
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

        mProgressBarSuperHandler.removeCallbacks(mProgressBarSuperTask);
    }

    public void onProgressBarTimeout() {
        ZLogger.w("Wait Progress Timeout");
        showToast(R.string.progress_bar_timeout);
        cancelProgressBar();
    }

    // Alarm timer
    private Runnable mProgressBarSuperTask = new Runnable() {
        @Override
        public void run() {
            onProgressBarTimeout();
        }
    };

    public boolean isFirstLoad() {
        return SPWristbandConfigInfo.getFirstAppStartFlag(this);
    }

    protected boolean ensureBLEExists() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast(R.string.bluetooth_not_support_ble);
            return false;
        }
        return true;
    }

    protected void showPermissionRationaleDialog() {
        AlertDialog.Builder aa = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
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

    /**
     * <tr>
     *     <td>MANUFACTURER</td><td>BRAND</td><td>MODEL</td><td>BOOTLOADER</td>
     * </tr>
     * <tr><td>HUAWEI</td><td>Huawei</td><td>HUAWEI ALE-CL00</td><td>unknown</td></tr>
     * */
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
            intent.setData(Uri.parse("package:" + getPackageName()));
        }

        try {
            ResolveInfo res = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (res != null) {
                startActivityForResult(intent, REQUEST_CODE_APPLICATION_SETTINGS);
            } else {
                ZLogger.d("cannot resolve permission activity");
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ZLogger.i("requestCode=" + requestCode + ", resultCode=" + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_APPLICATION_SETTINGS: {
                onPermissionSettingsBack();
                break;
            }
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onPermissionSettingsBack(){
        ZLogger.w("onPermissionSettingsBack");
    }

    public void onRationaleCancel(){
        ZLogger.w("onRationaleCancel");
    }

}
