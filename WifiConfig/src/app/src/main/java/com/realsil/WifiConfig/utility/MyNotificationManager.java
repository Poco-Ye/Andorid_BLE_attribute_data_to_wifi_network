package com.realsil.WifiConfig.utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import com.realsil.sdk.core.logger.ZLogger;

/**
 * Created by rain1_wen on 2016/10/18.
 */

public class MyNotificationManager {
    // Log
    private final static String TAG = "MyNotificationManager";
    private final static boolean D = true;
    // object
    private static MyNotificationManager mInstance;
    private static Context mContext;

    NotificationManager mNotificationManager;
    Notification mNotification;

    public static void initial(Context context) {
        ZLogger.d(D, "initial()");
        mInstance = new MyNotificationManager();
        mContext = context;

        mInstance.mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static MyNotificationManager getInstance() {
        return mInstance;
    }

    private int counter;

}
