package com.realsil.wificonfigprofile.entity;

/**
 * Created by neil_zhou on 2018/10/10.
 */

public class WifiConfigInfo {
    private byte[] mac;
    private String macString;
    private String ssid;
    private byte encryptType;
    private byte channel;
    private byte rssi;
    private boolean isConnected;

    public byte getChannel() {
        return channel;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
    }

    public byte[] getMac() {
        return mac;
    }

    public void setMac(byte[] mac) {
        this.mac = mac;
    }

    public String getMacString() {
        return macString;
    }

    public void setMacString(String macString) {
        this.macString = macString;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public byte getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(byte encryptType) {
        this.encryptType = encryptType;
    }

    public byte getRssi() {
        return rssi;
    }

    public void setRssi(byte rssi) {
        this.rssi = rssi;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WifiConfigInfo) {
            WifiConfigInfo var2 = (WifiConfigInfo)obj;
            return this.macString.equals(var2.macString);
        } else {
            return super.equals(obj);
        }
    }

    public static class AddressComparator {
        public String address;

        public AddressComparator() {
        }

        public boolean equals(Object obj) {
            if(obj instanceof WifiConfigInfo) {
                WifiConfigInfo var2 = (WifiConfigInfo)obj;
                return this.address.equals(var2.macString);
            } else {
                return super.equals(obj);
            }
        }
    }
}
