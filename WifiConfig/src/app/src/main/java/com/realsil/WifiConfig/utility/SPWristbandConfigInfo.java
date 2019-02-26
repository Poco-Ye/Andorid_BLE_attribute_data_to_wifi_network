package com.realsil.WifiConfig.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class SPWristbandConfigInfo {
    private static String SP_KEY_BONDED_DEVICE = "SPKeyBondedDevice";

    private static String SP_KEY_FIRST_APP_START_FLAG = "SPKeyFirstAPPStartFlag";

    private static String SP_WRISTBAND_CONFIG_INFO = "SPWristbandConfigInfo";

    public static String getBondedDevice(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_WRISTBAND_CONFIG_INFO, Context.MODE_PRIVATE);
        String value = sp.getString(SP_KEY_BONDED_DEVICE, null);
        return value;
    }

    public static boolean getFirstAppStartFlag(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_WRISTBAND_CONFIG_INFO, Context.MODE_PRIVATE);
        boolean value = sp.getBoolean(SP_KEY_FIRST_APP_START_FLAG, true);
        return value;
    }


    public static String getInfoKeyValue(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(SP_WRISTBAND_CONFIG_INFO, Context.MODE_PRIVATE);
        String value = sp.getString(key, null);
        return value;
    }


    public static void setBondedDevice(Context context, String v) {
        SharedPreferences sp = context.getSharedPreferences(SP_WRISTBAND_CONFIG_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(SP_KEY_BONDED_DEVICE, v);
        ed.apply();
    }


    public static void setInfoKeyValue(Context context, String key, String v) {
        SharedPreferences sp = context.getSharedPreferences(SP_WRISTBAND_CONFIG_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(key, v);
        ed.apply();
    }
}
