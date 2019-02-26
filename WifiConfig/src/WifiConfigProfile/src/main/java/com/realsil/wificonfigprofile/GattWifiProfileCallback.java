package com.realsil.wificonfigprofile;

/**
 * Created by neil_zhou on 2018/10/11.
 */

public interface GattWifiProfileCallback {
    void getData(byte[] data);
    void onDataSend(boolean status);
}
