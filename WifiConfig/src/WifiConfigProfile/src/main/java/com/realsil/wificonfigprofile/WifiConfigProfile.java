package com.realsil.wificonfigprofile;


import com.realsil.wificonfigprofile.entity.ConnectWifiData;
import com.realsil.wificonfigprofile.entity.WifiConfigInfo;

/**
 * Created by neil_zhou on 2018/10/10.
 */

public class WifiConfigProfile {
    private static WifiConfigProfile mInstance;
    private WifiConfigProfileInterface wifiConfigProfileInterface;
    private WifiConfigProfileCallBack wifiConfigProfileCallBack;

    private static final byte CMD_SCAN = 0x01;
    private static final byte CMD_CONNECT = 0x02;
    private static final byte CMD_GET_STATUS = 0x03;
    private static final byte CMD_CANCEL_CONNECT = 0x04;
    private static final byte CMD_GET_VERSION = 0x05;

    private static final byte EVENT_CMD_RESULT = 0x01;
    private static final byte EVENT_AP_INFO = 0x02;
    private static final byte EVENT_STATUS_RSP = 0x03;
    private static final byte EVENT_SCAN_COMPLETE = 0x04;
    private static final byte EVENT_CONFIG_VERSION_RSP = 0x05;

    public static final byte STATUS_SUCCESS = 0;
    public static final byte STATUS_FAILED = 1;
    private static final byte STATUS_CONNECTING = 2;
    public static final byte STATUS_PSW_ERROR = 3;
    public static final byte STATUS_DISCONNECT = 4;

    private static final byte SYNC_BYTE = (byte) 0xaa;
    private static final byte GROUP_CMD = 0x01;
    private static final byte GROUP_EVENT = 0x02;
    private static final byte FREQ_TYPE = 0;

    private static final byte SCAN = 0;
    private static final byte SEND_CONNECT = 1;
    private static final byte SEND_DISCONNECT = 2;

    private byte mStatus;

    private boolean needGetWifiStatus;
    private GattWifiProfileCallback gattWifiProfileCallback = new GattWifiProfileCallback() {
        @Override
        public void getData(byte[] data) {
            analyzeData(data);
        }

        @Override
        public void onDataSend(boolean status) {
            wifiConfigProfileInterface.log("onDataSend:" + String.valueOf(status));
        }
    };

    public static WifiConfigProfile getInstance() {
        return mInstance;
    }

    public static void initial(WifiConfigProfileInterface wifiConfigProfileInterface) {
        if (mInstance == null) {
            synchronized (WifiConfigProfile.class) {
                if (mInstance == null) {
                    mInstance = new WifiConfigProfile();
                }
            }
        }
        if (mInstance.wifiConfigProfileInterface == null) {
            mInstance.wifiConfigProfileInterface = wifiConfigProfileInterface;
        }
    }

    public void setGattWifiProfileCallback() {
        wifiConfigProfileInterface.setGattWifiProfileCallback(gattWifiProfileCallback);
    }

    public WifiConfigProfileCallBack getWifiConfigProfileCallBack() {
        return wifiConfigProfileCallBack;
    }

    public void setWifiConfigProfileCallBack(WifiConfigProfileCallBack wifiConfigProfileCallBack) {
        this.wifiConfigProfileCallBack = wifiConfigProfileCallBack;
    }

    public void searchWifiDevices() {
        wifiConfigProfileInterface.log("searchWifiDevices");
        mStatus = SCAN;
        byte[] buf = {(byte) 0xAA, 0x01, CMD_SCAN, 0x01, 0x00, 0x00};
        wifiConfigProfileInterface.send(buf);
    }

    public void getWifiStatus() {
        wifiConfigProfileInterface.log("getWifiStatus");
        byte[] buf = {(byte) 0xAA, 0x01, CMD_GET_STATUS, 0x01, 0x00, 0x00};
        wifiConfigProfileInterface.send(buf);
    }

    public void cancelConnectWifiDevice() {
        wifiConfigProfileInterface.log("cancelConnectWifiDevice");
        mStatus = SEND_DISCONNECT;
        needGetWifiStatus = true;
        byte[] buf = {(byte) 0xAA, 0x01, CMD_CANCEL_CONNECT, 0x01, 0x00, 0x00};
        wifiConfigProfileInterface.send(buf);
    }

    public void connectWifiDevice(WifiConfigInfo wifiConfigInfo, String password) {
        wifiConfigProfileInterface.log("connectWifiDevice");
        mStatus = SEND_CONNECT;
        needGetWifiStatus = true;
        ConnectWifiData connectWifiData = new ConnectWifiData();
        connectWifiData.setHead(SYNC_BYTE);
        connectWifiData.setGroupCmd(GROUP_CMD);
        connectWifiData.setEntryCmd(CMD_CONNECT);
        connectWifiData.setLength((short) 104);
        connectWifiData.setFreqType(FREQ_TYPE);
        connectWifiData.setEncryptType(wifiConfigInfo.getEncryptType());
        connectWifiData.setSsid(wifiConfigInfo.getSsid().getBytes());
        connectWifiData.setMac(wifiConfigInfo.getMac());
        connectWifiData.setPsw(password.getBytes());
        wifiConfigProfileInterface.send(connectWifiData.toByteArray());
    }

    public void analyzeData(byte[] data) {
        if (data.length < 5) {
            return;
        }
        if (data[0] == SYNC_BYTE) {

            if (data[1] == GROUP_EVENT) {
                byte entryType = data[2];
                WifiConfigInfo wifiConfigInfo = null;
                switch (entryType) {
                    case EVENT_CMD_RESULT:
                        if (data.length == 6) {
                            byte status = data[5];
                            wifiConfigProfileInterface.log("status = " + status);
                        }
                        if (needGetWifiStatus) {
                            needGetWifiStatus = false;
                            getWifiStatus();
                        }
//                        wifiConfigProfileCallBack.phoneCmdResut(status);
                        break;
                    case EVENT_AP_INFO:
                        int length = (data[4] << 8) | data[3];
                        if (length == 41) {
                            wifiConfigInfo = new WifiConfigInfo();
                            wifiConfigInfo.setEncryptType(data[5]);
                            byte[] mac = new byte[6];
                            for (int i = 6, j = 0; j < 6; i++, j++) {
                                mac[j] = data[i];
                            }
                            wifiConfigInfo.setMac(mac);
                            wifiConfigInfo.setMacString(convertMacByteToMacString(mac));
                            byte[] ssid = new byte[32];
                            for (int i = 12, j = 0; j < 32; i++, j++) {
                                ssid[j] = data[i];
                            }
                            wifiConfigInfo.setSsid(new String(ssid));
                            wifiConfigInfo.setChannel(data[44]);
                            wifiConfigInfo.setRssi(data[45]);
                            wifiConfigProfileCallBack.didScanAp(wifiConfigInfo);
                        }
                        break;
                    case EVENT_STATUS_RSP:
                        byte wifiStatus = data[5];
                        byte[] mac = new byte[6];
                        for (int i = 7, j = 0; j < 6; i++, j++) {
                            mac[j] = data[i];
                        }
                        String macString = convertMacByteToMacString(mac);
                        byte rssi = data[45];
                        if (mStatus == SCAN) {
                            if (wifiStatus == STATUS_SUCCESS) {
                                wifiConfigProfileCallBack.didConnectComplete(wifiStatus, macString, rssi);
                            } else {
                                wifiConfigProfileCallBack.didDisConnectToDevice(macString);
                            }
                        } else if (mStatus == SEND_DISCONNECT) {
                            if (wifiStatus != STATUS_SUCCESS) {
                                wifiConfigProfileCallBack.didDisConnectToDevice(macString);
                            }
                        } else {
                            if (wifiStatus == STATUS_SUCCESS || wifiStatus == STATUS_FAILED || wifiStatus == STATUS_PSW_ERROR) {
                                wifiConfigProfileCallBack.didConnectComplete(wifiStatus, macString, rssi);
                            } else {
                                wifiConfigProfileCallBack.didDisConnectToDevice(macString);
                            }
                        }

                        wifiConfigProfileInterface.log("EVENT_STATUS_RSP: wifiStatus " + wifiStatus + ",rssi " + rssi);
                        break;
                    case EVENT_SCAN_COMPLETE:
                        wifiConfigProfileInterface.log("EVENT_SCAN_COMPLETE");
                        wifiConfigProfileCallBack.didScanComplete();
                        break;
                    case EVENT_CONFIG_VERSION_RSP:
                        wifiConfigProfileInterface.log("EVENT_CONFIG_VERSION_RSP");
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private String convertMacByteToMacString(byte[] mac) {
        return String.format("%02x:%02x:%02x:%02x:%02x:%02x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
    }

}
