package com.realsil.WifiConfig.constant;

import android.os.Environment;

import com.realsil.sdk.core.BuildConfig;
import com.realsil.sdk.core.logger.ZLogger;

import java.io.File;


public class ConstantParam {

    public static final boolean WORK_TYPE_LOCAL = false;
    public static final boolean WORK_TYPE_INTERNET = true;
    public static final boolean APP_WORK_TYPE = true;

    public static final String APP_BUILD_TYPE = BuildConfig.BUILD_TYPE;

    /**
     * 基本的缓存的路径
     */
    public static final String BASE_CACHR_DIR = getBaseCacheDir();
    /**
     * 缓存的文件目录的名字
     */
    private static final String CACHR_DIR_NAME = "RtkBand";

    /**
     * 包名
     */
    public static final String PACKAGE_NAME = "com.realsil.android.wificonfig";
    /**
     * 图片的缓存路径
     */
    public static final String IMAGE_SAVE_CACHE = getBaseCacheDir() + "saveImage/";

    /**
     * 获取基本的缓存的路径
     *
     * @return
     */
    private static String getBaseCacheDir() {
        // TODO Auto-generated method stub
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + CACHR_DIR_NAME + "/";
            //return "sdcard" + "/" + CACHR_DIR_NAME + "/";
        } else {
            return "/data/data/" + PACKAGE_NAME + "/" + CACHR_DIR_NAME + "/";
        }
    }

    private static String getCameraImageCacheDir() {
        String imagePath = "sdcard/DCIM/";
        // initial the cash path
        File file = new File(imagePath);
        if (file.exists()) {
            imagePath = "sdcard/DCIM/Camera/";
            if (file.exists()) {
                return imagePath;
            } else {
                imagePath = "sdcard/DCIM/100MEDIA/";
                if (file.exists()) {
                    return imagePath;
                } else {
                    imagePath = "sdcard/DCIM/100Andro/";
                    if (file.exists()) {
                        return imagePath;
                    }
                }
            }
        }

        return IMAGE_SAVE_CACHE;
    }

    public static boolean isInDebugMode() {
        ZLogger.d("APP_BUILD_TYPE: " + APP_BUILD_TYPE);
        return APP_BUILD_TYPE.equals("debug");

    }
}
