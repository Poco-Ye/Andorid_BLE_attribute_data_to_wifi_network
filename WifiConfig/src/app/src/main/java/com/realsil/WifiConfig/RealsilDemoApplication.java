package com.realsil.WifiConfig;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import com.realsil.WifiConfig.backgroundscan.BackgroundScanAutoConnected;
import com.realsil.WifiConfig.utility.ActivityUtils;
import com.realsil.WifiConfig.utility.MyNotificationManager;
import com.realsil.WifiConfig.utility.ToastUtils;
import com.realsil.sdk.core.logger.WriteLog;
import com.realsil.sdk.core.logger.ZLogger;
import com.realsil.sdk.dfu.RtkDfu;
import com.realsil.wificonfigprofile.WifiConfigProfile;


import java.util.List;



public class RealsilDemoApplication extends Application{
    // Log
    private final static String TAG = "RealsilDemoApplication";
    private final static boolean D = true;

    public int count = 0;
    private static boolean isInForeground = false;

    public static boolean isInForeground() {
        return isInForeground;
    }

    private String mLastActivity = null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (D) {
            ZLogger.d(D, "APPLICATION_ID=" + BuildConfig.APPLICATION_ID);
            ZLogger.d(D, "VERSION_NAME=" + BuildConfig.VERSION_NAME);
            ZLogger.d(D, "VERSION_CODE=" + BuildConfig.VERSION_CODE);
            ZLogger.d(D, "FLAVOR=" + BuildConfig.FLAVOR);
        }

        // initial RealtekSDK
        ZLogger.initialize("Mesh", true);
        WriteLog.install(this, "OTA", 2);
        RtkDfu.initialize(this);
        RtkDfu.DEBUG_ENABLE = true;
        // initial BackgroundScanAutoConnected
        BackgroundScanAutoConnected.initial(this);
        WifiConfigProfile.initial(BackgroundScanAutoConnected.getInstance().getGattLayerInstance());
        WifiConfigProfile.getInstance().setGattWifiProfileCallback();
        // initial MyNotificationManager
        MyNotificationManager.initial(this);

        // initial ToastUtils
        ToastUtils.initial(this);

        //register callback for judge that if application is in foreground
        registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);

        int pid = android.os.Process.myPid();
        String processAppName = getProcessName(this, pid);
        ZLogger.d("process: " + processAppName);

    }

    private Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            ZLogger.d(D, "localClassName=" + activity.getLocalClassName());
            if (count == 0) {
                BackgroundScanAutoConnected.getInstance().setStopAutoReconnect(false);
                BackgroundScanAutoConnected.getInstance().startAutoConnect();
                ZLogger.w(D, "app is in foreground");
                isInForeground = true;
            }
            count++;
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            ZLogger.d(D, activity.toString());
            mLastActivity = activity.getLocalClassName();
            count--;
            if (count == 0) {
                ZLogger.d(D, "app is in background");
                isInForeground = false;
                BackgroundScanAutoConnected.getInstance().stopAutoConnect();
                BackgroundScanAutoConnected.getInstance().setStopAutoReconnect(true);
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };

    public void closeAllActivity() {
        List<Activity> list = ActivityUtils.getInstance().getAliveActivity();
        Activity currentActivity = null;
        for (int i = 0; i < list.size(); i++) {
            Activity activity = list.get(i);
            if (i == list.size() - 1) {
                currentActivity = activity;
            } else {
                activity.finish();
            }

        }
        if (currentActivity != null) {
            currentActivity.finish();
        }
    }

    public void startHomeActivity() {
        List<Activity> list = ActivityUtils.getInstance().getAliveActivity();
        Activity currentActivity = null;
        for (int i = 0; i < list.size(); i++) {
            Activity activity = list.get(i);
            if (i == list.size() - 1) {
                currentActivity = activity;
            } else {
                activity.finish();
            }

        }
        if (currentActivity != null) {
            currentActivity.finish();
            Intent intent = new Intent(this, WifiConfigHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    /**
     * 获取进程名称
     *
     * @return null may be returned if the specified process not found
     */
    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

}
