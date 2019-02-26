package com.realsil.WifiConfig.backgroundscan;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanRecord;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.text.TextUtils;

import com.realsil.WifiConfig.R;
import com.realsil.WifiConfig.RealsilDemoApplication;
import com.realsil.WifiConfig.gattlayer.GattLayer;
import com.realsil.WifiConfig.gattlayer.GattLayerCallback;
import com.realsil.WifiConfig.utility.SPWristbandConfigInfo;
import com.realsil.WifiConfig.utility.ToastUtils;
import com.realsil.sdk.core.bluetooth.scanner.SpecScanRecord;
import com.realsil.sdk.core.logger.ZLogger;
import com.realsil.sdk.core.utility.PermissionUtil;

import java.util.ArrayList;
import java.util.UUID;


/**
 * Created by Administrator on 2016/5/23.
 * <p>
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BackgroundScanAutoConnected {
    // Log
    private final static String TAG = "BackgroundScanAutoConnected";
    private final static boolean D = true;

    // State
    public static final int STATE_IDLE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_LOGGING = 2;
    public static final int STATE_SYNCING = 3;
    public static final int STATE_DISCONNECTED = 4;
    public static final int STATE_ERROR = 5;

    private int mState = STATE_IDLE;
    private String mStateString = "";

    // Message
    public static final int MSG_STATE_CONNECTED = 0;
    public static final int MSG_STATE_DISCONNECTED = 1;
    public static final int MSG_WRIST_STATE_CHANGED = 2;
    public static final int MSG_RECEIVE_SPORT_INFO = 3;//characteristic read
    public static final int MSG_RECEIVE_SLEEP_INFO = 4;
    public static final int MSG_BOND_STATE_ERROR = 5;
    public static final int MSG_BOND_STATE_SUCCESS = 6;

    public static final int MSG_FIND_BONDED_DEVICE = 7;
    public static final int MSG_MESH_PROVISION = 8;
    public static final int MSG_MESH_ADD_APPKEY = 9;
    public static final int MSG_ERROR = 10;
    public static final int MSG_MESH_ADD_BIND_LIGHT = 11;
    public static final int MSG_MESH_ADD_BIND_COLOR = 12;
    public static final int MSG_MESH_OK = 13;

    private static Context mContext;

    //    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;

    //private BackgroundScanCallback mCallback;

    ArrayList<BackgroundScanCallback> mCallbacks;

    private boolean isInLogin = false;

    private boolean isStopAutoReconnect = false;  //用于在后台的时候停止自动扫描设备和重连设备

    public void setStopAutoReconnect(boolean stopAutoReconnect) {
        ZLogger.d(D, "setStopAutoReconnect():" + stopAutoReconnect);
        isStopAutoReconnect = stopAutoReconnect;
    }

    public boolean isStopAutoReconnect() {
        return isStopAutoReconnect;
    }

    // instance
    private static BackgroundScanAutoConnected mInstance;

    private BluetoothOnOffStateReceiver mBluetoothOnOffStateReceiver;

    private boolean mScanning;

    // Use this count to count current receive adv count, may be something error can not scan.
    private int mReceiveAdvCount = 0;

    private boolean isConnected = false;

    private String mDeviceAddress;

    public String getBluetoothAddress() {
        return mDeviceAddress;
    }

    // Gatt Layer
    private GattLayer mGattLayer;

    public GattLayer getGattLayerInstance() {
        return mGattLayer;
    }

    public boolean isConnect() {
        ZLogger.d(D, "isConnected: " + isConnected);
        return isConnected;
    }

    private BackgroundScanAutoConnected(Context context) {
        mContext = context.getApplicationContext();
        mCallbacks = new ArrayList<>();
    }

    public static void initial(Context context) {
        if (mInstance == null) {
            synchronized (BackgroundScanAutoConnected.class) {
                if (mInstance == null) {
                    mInstance = new BackgroundScanAutoConnected(context.getApplicationContext());
                }
            }
        }

//        mContext = context.getApplicationContext();
        if (mInstance.mGattLayer == null) {
            mInstance.mGattLayer = new GattLayer(mContext);
        }

        if (mInstance.mBluetoothManager == null) {
            mInstance.mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        if (mInstance.mBluetoothManager == null) {
            ZLogger.w("Unable to initialize BluetoothManager.");
        }

        if (mInstance.mBluetoothAdapter == null && mInstance.mBluetoothManager != null) {
            mInstance.mBluetoothAdapter = mInstance.mBluetoothManager.getAdapter();
        }
        if (mInstance.mBluetoothAdapter == null) {
            ZLogger.w("Unable to obtain a BluetoothAdapter.");
        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                mInstance.mBluetoothLeScanner = mInstance.mBluetoothAdapter.getBluetoothLeScanner();
//            }
        }
    }

    public static BackgroundScanAutoConnected getInstance() {
        return mInstance;
    }


    public void registerCallback(BackgroundScanCallback callback) {
        if (!mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
        }
    }

    public void unregisterCallback(BackgroundScanCallback callback) {
        if (mCallbacks.contains(callback)) {
            mCallbacks.remove(callback);
        }
    }

    public void closeConnect() {
        isInLogin = false;
        mGattLayer.close();
    }

    public void startAutoConnect() {

//        checkAndResumeProgressBar();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            ZLogger.w(D, "please ensure bluetooth is enabled.");
            for (BackgroundScanCallback callback : mCallbacks) {
                callback.onLeScanEnable(false);
            }
            return;
        }

        if (!isConnected) {
            ZLogger.d(D, "startAutoConnect()");

            String bondedDeviceAddr = SPWristbandConfigInfo.getBondedDevice(mContext);
            if (!TextUtils.isEmpty(bondedDeviceAddr)) {
                connectWristbandDevice(mBluetoothAdapter.getRemoteDevice(bondedDeviceAddr));
            } else {
                scanLeDevice(true);
            }

        } else {
            stopAutoConnect();
        }
    }

    public void forceLeScan() {
        ZLogger.d(D, "forceLeScan");

//        checkAndResumeProgressBar();
        scanLeDevice(true);
    }

    public void connectWristbandDevice(BluetoothDevice device) {
        if (isInLogin) {
            ZLogger.w(D, "is in login, do nothing.");
            return;
        }
        stopAutoConnect();

        ZLogger.d(D, "connect to " + device.getAddress());
        SendMessage(MSG_FIND_BONDED_DEVICE, device, -1, -1);
    }

    public void stopAutoConnect() {
        ZLogger.d(D, "stopAutoConnect()");
        scanLeDevice(false);

        // 强制删除progress bar
//        cancelProgressBar();
    }

    public void checkAndResumeProgressBar() {
        ZLogger.d(D, "mState: " + mState + ", mStateString: " + mStateString);
        if (mState != STATE_IDLE) {
            if (!ToastUtils.getInstance().isProgressBarShowing()) {
                showProgressBar(mStateString);
            }
        }
    }

    public void registerBluetoothOnOffAutoStartBroadcast() {
        ZLogger.d(D, "registerBluetoothOnOffAutoStartBroadcast");
        mBluetoothOnOffStateReceiver = new BluetoothOnOffStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mBluetoothOnOffStateReceiver, filter);
    }

    public void unregisterBluetoothOnOffAutoStartBroadcast() {
        ZLogger.d(D, "unregisterBluetoothOnOffAutoStartBroadcast");
        if (mBluetoothOnOffStateReceiver != null) {
            mContext.unregisterReceiver(mBluetoothOnOffStateReceiver);
        }
        mBluetoothOnOffStateReceiver = null;
    }

    // Stops scanning after 120 seconds. This is too long
    // Stops scanning after 30 seconds. 30 seconds is enough
    private static final long SCAN_PERIOD = 30000;
    private static final long RE_SCAN_PERIOD = 10000;
    private final static UUID WIFICONFIG_SERVICE_UUID = UUID.fromString("0000a00a-0000-1000-8000-00805f9b34fb");
    private Handler mScanHandler = new Handler();

    // Control le scan

    /**
     * java.lang.SecurityException: Need ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get scan results
     */
    private void scanLeDevice(boolean enable) {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            ZLogger.w(D, "please ensure bluetooth is enabled.");
            for (BackgroundScanCallback callback : mCallbacks) {
                callback.onLeScanEnable(false);
            }
            return;
        }

        // control the process bar and le scan
        if (enable) {
            if (!PermissionUtil.checkSelfPermissions(mContext,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION})) {
//                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mContext,
//                        Manifest.permission.ACCESS_COARSE_LOCATION)
//                        && PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mContext,
//                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                ZLogger.w("Need ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get scan results");
                for (BackgroundScanCallback callback : mCallbacks) {
                    callback.onLeScanEnable(false);
                }
                return;
            }

            // avoid repetition operator
            if (mScanning == enable) {
                ZLogger.w(D, "the le scan is already on");
                if (mReceiveAdvCount == 0) {
                    ZLogger.w(D, "May be something wrong, le scan may be not real start, try restart it.");

                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    mBluetoothAdapter.startLeScan(mLeScanCallback);

                }
                // restart timer
                mScanHandler.removeCallbacks(mStopLeScan);
                mScanHandler.postDelayed(mStopLeScan, SCAN_PERIOD);
                for (BackgroundScanCallback callback : mCallbacks) {
                    callback.onLeScanEnable(true);
                }
                return;
            }
            // Stops scanning after a pre-defined scan period.
            mScanHandler.postDelayed(mStopLeScan, SCAN_PERIOD);
            ZLogger.d(D, "start the le scan, on time is " + SCAN_PERIOD + "ms");
            mReceiveAdvCount = 0;

            mBluetoothAdapter.startLeScan(mLeScanCallback);

        } else {
            // avoid repetition operator
            if (mScanning == enable) {
                ZLogger.w(D, "the le scan is already off");
                for (BackgroundScanCallback callback : mCallbacks) {
                    callback.onLeScanEnable(false);
                }
                return;
            }
            //remove the stop le scan runnable
            mScanHandler.removeCallbacks(mStopLeScan);

            ZLogger.d(D, "stop the le scan");
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mBluetoothLeScanner != null) {
//                mBluetoothLeScanner.stopScan(mScanCallback);
//            } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
//            }
        }
        // update le scan status
        mScanning = enable;
        for (BackgroundScanCallback callback : mCallbacks) {
            callback.onLeScanEnable(mScanning);
        }
    }


    // Stops scanning after a pre-defined scan period.
    private Runnable mStopLeScan = new Runnable() {
        @Override
        public void run() {
            ZLogger.d(D, "le delay time reached");
            mScanHandler.removeCallbacks(mRestartLeScan);
            mScanHandler.postDelayed(mRestartLeScan, RE_SCAN_PERIOD);
            // Stop le scan, delay SCAN_PERIOD ms
            scanLeDevice(false);
        }
    };

    // Restart scanning after a pre-defined scan period.
    private Runnable mRestartLeScan = new Runnable() {
        @Override
        public void run() {
            ZLogger.d(D, "restart scan delay time reached");
            // restart le scan, delay SCAN_PERIOD ms
            //SendMessage(MSG_STATE_DISCONNECTED, null, -1, -1);
            startAutoConnect();
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            if (!mScanning) {
                ZLogger.w(D, "is stop le scan, return");
                return;
            }
            mReceiveAdvCount++;

            SpecScanRecord record = SpecScanRecord.parseFromBytes(scanRecord);
            ZLogger.d(D, record.toString());

            if ((record.getServiceUuids() == null) || (!record.getServiceUuids().contains(new ParcelUuid(WIFICONFIG_SERVICE_UUID)))) {
                return;
            }

            final String addr = SPWristbandConfigInfo.getBondedDevice(mContext);

            for (BackgroundScanCallback callback : mCallbacks) {
                callback.onWristbandDeviceFind(device, rssi, scanRecord);
            }

            if (addr == null || !addr.equals(device.getAddress())) {
                return;
            }

            scanLeDevice(false);
            if (isInLogin) {
                ZLogger.w(D, "in Login, return.");
                return;
            }
            ZLogger.d(D, "Device name is: " + device.getName() +
                    " - address is: " + device.getAddress());

            SendMessage(MSG_FIND_BONDED_DEVICE, device, -1, -1);
        }
    };

    // Application Layer callback
    private GattLayerCallback mWristbandManagerCallback = new GattLayerCallback() {
        @Override
        public void onConnectionStateChange(final boolean status, final boolean newState) {
            ZLogger.d(D, "status: " + status + ", newState: " + newState);
            // if already connect to the remote device, we can do more things here.
            if (status && newState) {
                SendMessage(MSG_STATE_CONNECTED, null, -1, -1);
            } else {
                SendMessage(MSG_STATE_DISCONNECTED, null, -1, -1);
            }
        }

    };

    private void showProgressBar(final int message) {
        showProgressBar(mContext.getResources().getString(message));
    }

    private void showProgressBar(final String message) {
        //if(!JudgeActivityFront.isAppOnForeground(mContext)) {
        if (!RealsilDemoApplication.isInForeground()) {
            ZLogger.w(D, "showProgressBar, running in background.");
            return;
        }
        ToastUtils.getInstance().showProgressBar(message, mProgressDialogSupper);
    }

    private void cancelProgressBar() {
        // update state info
        mState = STATE_IDLE;
        mStateString = "";

        ToastUtils.getInstance().cancelProgressBar();
    }

    private ToastUtils.ProgressBarSupperCallback mProgressDialogSupper = new ToastUtils.ProgressBarSupperCallback() {
        public void onSupperTimeout() {
            mGattLayer.close();
            isInLogin = false;

            mState = STATE_IDLE;
            mStateString = "";

            startAutoConnect();

            ToastUtils.getInstance().showToast(R.string.progress_bar_timeout);
        }
    };


    /**
     * send message
     *
     * @param msgType Type message type
     * @param obj     object sent with the message set to null if not used
     * @param arg1    parameter sent with the message, set to -1 if not used
     * @param arg2    parameter sent with the message, set to -1 if not used
     **/
    private void SendMessage(int msgType, Object obj, int arg1, int arg2) {
        if (mHandler != null) {
            //	Message msg = new Message();
            Message msg = Message.obtain();
            msg.what = msgType;
            if (arg1 != -1) {
                msg.arg1 = arg1;
            }
            if (arg2 != -1) {
                msg.arg2 = arg2;
            }
            if (null != obj) {
                msg.obj = obj;
            }
            mHandler.sendMessage(msg);
        } else {
            ZLogger.w(D, "handler is null, can't send message");
        }
    }

    // Broadcast to receive BT on/off broadcast
    public class BluetoothOnOffStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                ZLogger.d(D, "ACTION_STATE_CHANGED: state: " + state);
                if (state == BluetoothAdapter.STATE_ON) {
                    // Need wait a while
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isInLogin = false;
                    startAutoConnect();
                } else if (state == BluetoothAdapter.STATE_TURNING_OFF) {
                    if (mScanning) {
                        mScanning = false;

//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mBluetoothLeScanner != null) {
//                            mBluetoothLeScanner.stopScan(mScanCallback);
//                        } else {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                        }
                        mScanHandler.removeCallbacks(mStopLeScan);
                        for (BackgroundScanCallback callback : mCallbacks) {
                            callback.onLeScanEnable(false);
                        }
                    }
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    if (isConnected) {
                        ZLogger.w(D, "May be close bluetooth, but not disconnect, something may be error!");
                        for (BackgroundScanCallback callback : mCallbacks) {
                            callback.onWristbandState(false,STATE_DISCONNECTED);
                        }
                    }
                    isInLogin = false;
                    mGattLayer.close();
                }
            }
        }
    }

    public boolean isInLogin() {
        return isInLogin;
    }

    // The Handler that gets information back from test thread
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_STATE_CONNECTED:
                    mState = STATE_IDLE;
                    ZLogger.d(D, "MSG_STATE_CONNECTED, connect");
                    SPWristbandConfigInfo.setBondedDevice(mContext, mDeviceAddress);
                    isConnected = true;
                    isInLogin = false;
                    for (BackgroundScanCallback callback : mCallbacks) {
                        callback.onWristbandState(true, mState);
                    }
                    break;
                case MSG_STATE_DISCONNECTED:
                    // do something
                    isConnected = false;
                    ZLogger.d(D, "MSG_STATE_DISCONNECTED, something is error");
//                    ToastUtils.getInstance().showToast(R.string.connect_disconnect);  //
//                    cancelProgressBar();
                    isInLogin = false;
                    // Need start scan
                    mState = STATE_DISCONNECTED;
                    if (!isStopAutoReconnect) {
                        startAutoConnect();
                    }
                    for (BackgroundScanCallback callback : mCallbacks) {
                        callback.onWristbandState(false, mState);
                    }
                    //mWristbandManager.close();----for test
                    break;
                case MSG_ERROR:
                    isConnected = false;
                    int er = msg.arg1;

                    ToastUtils.getInstance().showToast(R.string.something_error);

//                    cancelProgressBar();
                    isInLogin = false;
                    mGattLayer.close();
                    // Need start scan
                    mState = STATE_ERROR;
                    if (!isStopAutoReconnect) {
                        startAutoConnect();
                    }
                    for (BackgroundScanCallback callback : mCallbacks) {
                        callback.onWristbandState(false, mState);
                    }
                    break;
                case MSG_FIND_BONDED_DEVICE:
                    isConnected = false;
                    final BluetoothDevice device = (BluetoothDevice) msg.obj;
                    mDeviceAddress = device.getAddress();
                    ZLogger.d(D, "MSG_FIND_BONDED_DEVICE. name= " + device.getName() + ", addr=" + mDeviceAddress);

                    isInLogin = true;
                    // update state info
                    mState = STATE_CONNECTING;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mGattLayer.connect(device.getAddress(), mWristbandManagerCallback);
                        }
                    }).start();
                    break;
                default:
                    break;
            }
        }
    };

    public static class BackgroundScanCallback {
        public void onWristbandDeviceFind(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {

        }

        public void onWristbandDeviceFind(BluetoothDevice device, int rssi, ScanRecord scanRecord) {

        }

        public void onLeScanEnable(boolean enable) {

        }

        public void onWristbandLoginStateChange(boolean connected) {

        }

        public void onWristbandState(boolean connected, final int state) {

        }
    }
}
