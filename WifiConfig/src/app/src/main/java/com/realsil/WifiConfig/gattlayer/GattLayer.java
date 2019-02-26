package com.realsil.WifiConfig.gattlayer;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;

import com.realsil.sdk.core.bluetooth.GlobalGatt;
import com.realsil.sdk.core.logger.ZLogger;
import com.realsil.sdk.core.utility.DataConverter;
import com.realsil.wificonfigprofile.GattWifiProfileCallback;
import com.realsil.wificonfigprofile.WifiConfigProfileInterface;

import java.util.UUID;


public class GattLayer implements WifiConfigProfileInterface{
    // Log
    private final static String TAG = "GattLayer";
    private final static boolean D = true;
    private int retry = 0;

    // Gatt Layer Call
    private GattLayerCallback mCallback;
    private GattWifiProfileCallback gattWifiProfileCallback;

    // Bluetooth Manager
    private BluetoothGatt mBluetoothGatt;

    // MTU size
    private static int MTU_SIZE_EXPECT = 240;

    // Device info
    private String mBluetoothDeviceAddress;

    // Context
    private Context mContext;

    // Global Gatt
    private GlobalGatt mGlobalGatt;

    // UUID
    public final static UUID SERVICE_WIFI_CONFIG_UUID = UUID.fromString("0000e0ff-3c17-d293-8e48-14fe2e4da212");
    private final static UUID CHAR_WIFI_CONFIG_DATA_RETURN_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private final static UUID CHAR_WIFI_CONFIG_DATA_WRITE_UUID = UUID.fromString("0000ffe9-0000-1000-8000-00805f9b34fb");

    private final static UUID CHAR_WIFI_CONFIG_NOT_USED_UUID = UUID.fromString("0000ffea-0000-1000-8000-00805f9b34fb");

    // Characteristic
    private BluetoothGattCharacteristic mReturnCharacteristic;
    private BluetoothGattCharacteristic mWriteCharacteristic;

    public GattLayer(Context context, GattLayerCallback callback) {
        ZLogger.d(D, "initial.");
        mContext = context;
        // register callback
        mCallback = callback;
        // Global Gatt
        mGlobalGatt = GlobalGatt.getInstance();
    }

    @Override
    public void log(String s) {
        ZLogger.d(s);
    }

    @Override
    public void setGattWifiProfileCallback(GattWifiProfileCallback gattWifiProfileCallback) {
        this.gattWifiProfileCallback = gattWifiProfileCallback;
    }

    public GattLayer(Context context) {
        ZLogger.d(D, "initial.");
        mContext = context;
        // Global Gatt
        mGlobalGatt = GlobalGatt.getInstance();
    }

    @Override
    public void send(byte[] data) {
        if (mWriteCharacteristic == null) {
            ZLogger.w(D, "CHAR_WIFI_CONFIG_DATA_WRITE_UUID not supported");
        }
        if (!mGlobalGatt.isConnected(mBluetoothDeviceAddress)) {
            ZLogger.w(D, "disconnected, addr=" + mBluetoothDeviceAddress);
        }
        ZLogger.d(D, "-->> " + DataConverter.bytes2HexWithSeparate(data));

        // Send the data
        mWriteCharacteristic.setValue(data);
        mGlobalGatt.writeCharacteristic(mBluetoothDeviceAddress, mWriteCharacteristic);
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        ZLogger.d(D, "address: " + address);
        mBluetoothDeviceAddress = address;
        return mGlobalGatt.connect(address, mGattCallback);
    }

    public boolean connect(final String address, GattLayerCallback callback) {
        ZLogger.d(D, "address: " + address);
        mBluetoothDeviceAddress = address;
        mCallback = callback;
        return mGlobalGatt.connect(address, mGattCallback);
    }

    /**
     * When the le services manager close, it must disconnect and close the gatt.
     */
    public void close() {
        ZLogger.d(D, "close()");
        try {
            mGlobalGatt.close(mBluetoothDeviceAddress);
            Thread.sleep(50);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnectGatt() {
        ZLogger.d(D, "disconnect()");
        mGlobalGatt.disconnectGatt(mBluetoothDeviceAddress);
    }


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            ZLogger.d(D, "mtu=" + mtu + ", status=" + status);
            // change the mtu real payloaf size
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mCallback.onDataLengthChanged(mtu);
            }

            // Attempts to discover services after successful connection.
            boolean sta = mBluetoothGatt.discoverServices();
            ZLogger.i(D, "Attempting to start service discovery: " + sta);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                retry = 0;
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    mBluetoothGatt = gatt;
                    ZLogger.i(D, "Connected to GATT server.");

                    // only android 5.0 add the requestMTU feature
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

                        // Attempts to discover services after successful connection.
                        boolean sta = mBluetoothGatt.discoverServices();
                        ZLogger.i(D, "Attempting to start service discovery: " +
                                sta);
                    } else {
                        ZLogger.i(D, "Attempting to request mtu size, expect mtu size is: " + String.valueOf(MTU_SIZE_EXPECT));
                        mBluetoothGatt.requestMtu(MTU_SIZE_EXPECT);
                    }

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    ZLogger.i(D, "Disconnected from GATT server.");
                    // try to close gatt
                    close();
                    // tell up stack the current connect state
                    mCallback.onConnectionStateChange(true, false);
                }
            } else {
                ZLogger.e(D, "error: status " + status + " newState: " + newState);
                // try to close gatt
                close();
                if (retry < 3) {
                    retry++;
                    connect(mBluetoothDeviceAddress);
                } else {
                    retry = 0;
                    // tell up stack the current connect state
                    mCallback.onConnectionStateChange(false, false);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            ZLogger.d(D, "status=" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // set the characteristic
                // initial the service and characteristic
                BluetoothGattService service = gatt.getService(SERVICE_WIFI_CONFIG_UUID);
                if (service == null) {
                    ZLogger.w(D, "SERVICE_WIFI_CONFIG_UUID not supported");

                    // try to disconnect gatt
                    disconnectGatt();
                    return;
                }
                mReturnCharacteristic = service.getCharacteristic(CHAR_WIFI_CONFIG_DATA_RETURN_UUID);
                if (mReturnCharacteristic == null) {
                    ZLogger.w(D, "CHAR_WIFI_CONFIG_DATA_RETURN_UUID not supported");

                    // try to disconnect gatt
                    disconnectGatt();
                    return;
                }
                mGlobalGatt.setCharacteristicNotification(mBluetoothDeviceAddress, mReturnCharacteristic, true);

                mWriteCharacteristic = service.getCharacteristic(CHAR_WIFI_CONFIG_DATA_WRITE_UUID);
                if (mWriteCharacteristic == null) {
                    ZLogger.w(D, "CHAR_WIFI_CONFIG_DATA_WRITE_UUID not supported");

                    // try to disconnect gatt
                    disconnectGatt();
                    return;
                }
            } else {
                // try to disconnect gatt
                disconnectGatt();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();

            ZLogger.d(D, "<<-- olength: " + characteristic.getValue().length
                    + ", data: " + DataConverter.bytes2HexWithSeparate(data));
            gattWifiProfileCallback.getData(data);
        }

        @Override
        public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            gattWifiProfileCallback.onDataSend(status == BluetoothGatt.GATT_SUCCESS);
        }

//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            ZLogger.d(D, "<<<--- status: " + status + " value: " + DataConverter.bytes2Hex(characteristic.getValue()));
//            String name = characteristic.getStringValue(0);
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                // tell up stack data send right
//                mCallback.onNameReceive(name);
//            }
//
//        }

        @Override
        public void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                boolean enabled = descriptor.getValue()[0] == 1;
                if (enabled) {
                    if (descriptor.getCharacteristic().getUuid().equals(mReturnCharacteristic.getUuid())) {
                        ZLogger.d(D, "WifiConfig Notification enabled");
                        mCallback.onConnectionStateChange(true, true);
                    }
                } else {
                    ZLogger.e(D, "Notification  not enabled!!!");
                    disconnectGatt();
                }

            } else {
                ZLogger.e(D, "Descriptor write error: " + status);
                disconnectGatt();
            }
        }

    };
}
