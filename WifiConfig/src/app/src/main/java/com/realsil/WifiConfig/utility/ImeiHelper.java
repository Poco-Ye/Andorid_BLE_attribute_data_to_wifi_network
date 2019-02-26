package com.realsil.WifiConfig.utility;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.realsil.sdk.core.logger.ZLogger;

import java.lang.reflect.Method;

/**
 * Created by rain1_wen on 2017/5/16.
 */

public class ImeiHelper {
    public static String getIMEI(Context context) {
        String imei = null;

        int permissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                imei = telephonyManager.getDeviceId();
            }
        } else {
            ZLogger.w("permission not grated, " + Manifest.permission.READ_PHONE_STATE);
        }

        if (TextUtils.isEmpty(imei)) {
            imei = android.os.Build.SERIAL;
        }

        if (TextUtils.isEmpty(imei)) {
            imei = android.os.Build.HARDWARE;
        }
        return imei == null ? "" : imei;
    }

    // The same to android.os.Build.SERIAL
    private static String getSerialNumber() {

        String serial = null;

        try {

            Class<?> c = Class.forName("android.os.SystemProperties");

            Method get = c.getMethod("get", String.class);

            serial = (String) get.invoke(c, "ro.serialno");

        } catch (Exception e) {

            e.printStackTrace();

        }

        return serial;

    }
}
