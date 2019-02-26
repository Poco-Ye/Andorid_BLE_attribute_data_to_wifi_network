package com.realsil.wificonfigprofile.entity;

/**
 * Created by neil_zhou on 2018/10/10.
 */

public class ConnectWifiData {
    private static final int size = 109;
    private byte head;
    private byte groupCmd;
    private byte entryCmd;
    private short length;
    private byte freqType;
    private byte encryptType;
    private byte[] ssid;
    private byte[] mac;
    private byte[] psw;

    public byte getHead() {
        return head;
    }

    public void setHead(byte head) {
        this.head = head;
    }

    public byte getGroupCmd() {
        return groupCmd;
    }

    public void setGroupCmd(byte groupCmd) {
        this.groupCmd = groupCmd;
    }

    public byte getEntryCmd() {
        return entryCmd;
    }

    public void setEntryCmd(byte entryCmd) {
        this.entryCmd = entryCmd;
    }

    public short getLength() {
        return length;
    }

    public void setLength(short length) {
        this.length = length;
    }

    public byte getFreqType() {
        return freqType;
    }

    public void setFreqType(byte freqType) {
        this.freqType = freqType;
    }

    public byte getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(byte encryptType) {
        this.encryptType = encryptType;
    }

    public byte[] getSsid() {
        return ssid;
    }

    public void setSsid(byte[] ssid) {
        this.ssid = ssid;
    }

    public byte[] getMac() {
        return mac;
    }

    public void setMac(byte[] mac) {
        this.mac = mac;
    }

    public byte[] getPsw() {
        return psw;
    }

    public void setPsw(byte[] psw) {
        this.psw = psw;
    }

    public byte[] toByteArray(){
        byte[] buf = new byte[size];
        buf[0] = head;
        buf[1] = groupCmd;
        buf[2] = entryCmd;
        buf[3] = (byte) (length & 0xff);
        buf[4] = (byte) ((length >> 8) & 0xff);
        buf[5] = freqType;
        buf[6] = encryptType;
        for (int i = 7,j = 0;j<32;i++,j++){
            buf[i] = ssid[j];
        }
        for (int i = 39,j = 0;j<6;i++,j++){
            buf[i] = mac[j];
        }
        int len = psw.length>64?64:psw.length;
        for (int i = 45,j = 0;j<len;i++,j++){
            buf[i] = psw[j];
        }

        return buf;
    }
}
