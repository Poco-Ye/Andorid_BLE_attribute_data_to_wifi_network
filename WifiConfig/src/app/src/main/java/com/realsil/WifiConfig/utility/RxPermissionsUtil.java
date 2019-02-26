package com.realsil.WifiConfig.utility;

import android.Manifest;
import android.app.Activity;

import com.tbruyelle.rxpermissions.RxPermissions;

import rx.functions.Action1;

/**
 * 6.0以上动态权限申请
 */
public class RxPermissionsUtil {


    public static void CheckPerssion(final Activity activity, final IperssionCallBack callBack) {

        RxPermissions rxPermissions = new RxPermissions(activity);
        rxPermissions.request(Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                Manifest.permission.CALL_PHONE
        )
                .subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if (aBoolean) {
                    callBack.isSuccess();
                } else {
                    callBack.isFalure();
                }
            }
        });
    }

//    String... permissions

    public static void CheckPerssion(final Activity activity, String permission, final IperssionCallBack callBack) {
        RxPermissions rxPermissions = new RxPermissions(activity);

        rxPermissions.request(permission)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            callBack.isSuccess();
                        } else {
                            callBack.isFalure();
                        }
                    }
                });
    }


    public interface IperssionCallBack {
        void isSuccess();

        void isFalure();

    }

}