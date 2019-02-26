package com.realsil.wificonfigprofile;


import com.realsil.wificonfigprofile.entity.WifiConfigInfo;

/**
 * Created by neil_zhou on 2018/10/10.
 */

public interface WifiConfigProfileCallBack {
    void phoneCmdResut(byte status);
    void didScanAp(WifiConfigInfo wifiConfigInfo);
    void didConnectComplete(byte status, String mac, byte rssi);
    void didDisConnectToDevice(String mac);
    void didScanComplete();
}
