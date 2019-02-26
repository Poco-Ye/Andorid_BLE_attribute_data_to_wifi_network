package com.realsil.WifiConfig.utility;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import com.realsil.WifiConfig.RealsilDemoApplication;
import com.realsil.sdk.core.logger.ZLogger;

/**
 * Created by rain1_wen on 2016/10/18.
 */

public class ToastUtils {
    // Log
    private final static String TAG = "ToastUtils";
    private final static boolean D = true;
    // object
    private static ToastUtils mInstance;
    private static Context mContext;

    private ProgressDialog mProgressDialog = null;

    private Toast mToast;

    private ProgressBarSupperCallback mProgressBarSupperCallback;

    private HomeWatcherReceiver mHomeWatcherReceiver;

    public static void initial(Context context) {
        ZLogger.d(D, "initial()");
        mInstance = new ToastUtils();
        mContext = context;

        mInstance.mHomeWatcherReceiver = new HomeWatcherReceiver();

        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mContext.registerReceiver(mInstance.mHomeWatcherReceiver, filter);
    }

    public void close() {
        mContext.unregisterReceiver(mHomeWatcherReceiver);
    }

    public static ToastUtils getInstance() {
        return mInstance;
    }

    public void showToast(final int message) {
        //if(!JudgeActivityFront.isAppOnForeground(mContext)) {
        if (!RealsilDemoApplication.isInForeground()) {
            ZLogger.e(D, "showToast, Is not in top.");
            return;
        }
        if (mToast == null) {
            mToast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    /**
     * 显示Toast通知
     *
     * @param msg 显示的内容
     */
    public void showToast(String msg) {
        //if(!JudgeActivityFront.isAppOnForeground(mContext)) {
        if (!RealsilDemoApplication.isInForeground()) {
            ZLogger.e(D, "showToast, Is not in top.");
            return;
        }
        if (mToast == null) {
            mToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msg);
        }
        mToast.show();
    }

    public void showProgressBar(final int message, ProgressBarSupperCallback callback) {
        showProgressBar(mContext.getResources().getString(message), callback);
    }

    public void showProgressBar(final int message) {
        showProgressBar(mContext.getResources().getString(message));
    }

    public void showProgressBar(final String message) {
        showProgressBar(message, null);
    }

    public void showProgressBar(final String message, ProgressBarSupperCallback callback) {
        //if(!JudgeActivityFront.isAppOnForeground(mContext)) {
        if (!RealsilDemoApplication.isInForeground()) {
            ZLogger.e(D, "Is not in top.");
            return;
        }

        try {
            cancelProgressBar();
//            mProgressBarSuperHandler.removeCallbacks(mProgressBarSuperTask);

            mProgressBarSupperCallback = callback;

            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(mContext, AlertDialog.THEME_HOLO_LIGHT);
                //mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                // Some device can not show dialog
                if( Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
                    mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                } else {
                    mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                }

                mProgressDialog.setTitle(null);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
            }
            mProgressDialog.setMessage(message);
            // android.view.WindowManager$BadTokenException: Unable to add window -- window android.view.ViewRootImpl$W@91bff12 has already been added
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
            mProgressBarSuperHandler.postDelayed(mProgressBarSuperTask, 30 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }

    public void cancelProgressBar() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();

                mProgressDialog = null;
            }
        }
        mProgressBarSuperHandler.removeCallbacks(mProgressBarSuperTask);
    }

    // Alarm timer
    private Handler mProgressBarSuperHandler = new Handler();
    private Runnable mProgressBarSuperTask = new Runnable() {
        @Override
        public void run() {
            ZLogger.w(D, "Wait Progress Timeout");
            // stop timer
            cancelProgressBar();

            if (mProgressBarSupperCallback != null) {
                mProgressBarSupperCallback.onSupperTimeout();
            }
        }
    };

    public boolean isProgressBarShowing() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                return true;
            }
        }

        return false;
    }


    public static class HomeWatcherReceiver extends BroadcastReceiver {
        private static final String LOG_TAG = "HomeReceiver";
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";// 长按Home键 或者 activity切换键
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";// 锁屏
        private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist"; // samsung 长按Home键

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ZLogger.i("action: " + action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                // android.intent.action.CLOSE_SYSTEM_DIALOGS
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                ZLogger.i("reason: " + reason);

                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)
                        || SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)
                        || SYSTEM_DIALOG_REASON_LOCK.equals(reason)
                        || SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                    ToastUtils.getInstance().cancelProgressBar();
                }
            }
        }
    }

    public static class ProgressBarSupperCallback {
        /**
         * Callback indicating when progressbar super timeout
         */
        public void onSupperTimeout() {
        }
    }
}
