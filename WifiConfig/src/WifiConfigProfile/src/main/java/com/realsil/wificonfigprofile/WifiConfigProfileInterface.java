package com.realsil.wificonfigprofile;

/**
 * Created by neil_zhou on 2018/10/22.
 */

public interface WifiConfigProfileInterface {
    void send(byte[] buf);
    void setGattWifiProfileCallback(GattWifiProfileCallback gattWifiProfileCallback);
    void log(String log);
}
