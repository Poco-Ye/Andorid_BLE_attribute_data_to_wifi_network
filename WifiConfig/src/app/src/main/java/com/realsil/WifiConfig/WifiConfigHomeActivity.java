package com.realsil.WifiConfig;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;

import com.realsil.WifiConfig.backgroundscan.BackgroundScanAutoConnected;
import com.realsil.WifiConfig.base.BaseActivity;
import com.realsil.WifiConfig.utility.ActivityUtils;
import com.realsil.WifiConfig.utility.DeviceListAdapter;
import com.realsil.WifiConfig.utility.RefreshableScanView;
import com.realsil.WifiConfig.utility.SPWristbandConfigInfo;
import com.realsil.WifiConfig.view.LoadingDialogFragment;
import com.realsil.WifiConfig.view.SwipeMenu;
import com.realsil.WifiConfig.view.SwipeMenuCreator;
import com.realsil.WifiConfig.view.SwipeMenuItem;
import com.realsil.WifiConfig.view.SwipeMenuListView;
import com.realsil.sdk.core.bluetooth.GlobalGatt;
import com.realsil.sdk.core.bluetooth.scanner.ExtendedBluetoothDevice;
import com.realsil.sdk.core.bluetooth.scanner.SpecScanRecord;
import com.realsil.sdk.core.logger.ZLogger;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.realsil.sdk.core.RtkCore.isBluetoothSupported;

public class WifiConfigHomeActivity extends BaseActivity implements DeviceListAdapter.DeviceListCallback {
    // Log
    private final static String TAG = "WifiConfigHomeActivity";
    private final static boolean D = true;

    private static final int REQUEST_ENABLE_BT = 1;

    @BindView(R.id.lvWristbandDevice)
    SwipeMenuListView mList;
    @BindView(R.id.refreshable_view)
    RefreshableScanView refreshableView;

    private BluetoothAdapter mBluetoothAdapter;

    // Device Scan adapter
    private DeviceListAdapter mAdapter;

    private BluetoothDevice mBluetoothDevice;
    private final LoadingDialogFragment dialogFragment = new LoadingDialogFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_unprovision_devices);

        ButterKnife.bind(this);
        ActivityUtils.getInstance().addActivity(this);
        // Check whether support BLE
        if (!ensureBLEExists()) {
            finish();
        }

        if (!isBluetoothSupported()) {
            ZLogger.w("this device does not support bluetooth");
            Dialog alertDialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).
                    setMessage(R.string.not_support_bluetooth).
                    create();
            alertDialog.show();
            finish();
        }
        mBluetoothAdapter = GlobalGatt.getInstance().getBluetoothAdapter();

        // set UI
        setUI();
        initialUI();
    }

    @Override
    public void removeBond(int position) {
        // stop le scan
        BackgroundScanAutoConnected.getInstance().stopAutoConnect();

        removeBondAlert(position);

    }

    private void removeBondAlert(final int position) {
        new AlertDialog.Builder(WifiConfigHomeActivity.this, AlertDialog.THEME_HOLO_LIGHT)
                .setTitle(R.string.settings_remove_bond)
                .setMessage(R.string.settings_remove_bond_sync_end_tip)
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        forceRemoveBond(position);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void initialUI() {
        if (D) ZLogger.d(D, "initialUI");
//        dialogFragment.setCancelable(false);
        BackgroundScanAutoConnected.getInstance().registerCallback(mBackgroundScanCallback);
        BackgroundScanAutoConnected.getInstance().registerBluetoothOnOffAutoStartBroadcast();
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RxPermissions rxPermissions = new RxPermissions(this);
            rxPermissions.requestEach(Manifest.permission.ACCESS_COARSE_LOCATION)
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Permission>() {
                        @Override
                        public void call(Permission permission) {
                            ZLogger.i(D, permission.toString());
                            if (permission.granted) {
                            } else if (!permission.shouldShowRequestPermissionRationale) {
//                                mcbCallRemind.setChecked(false);
                                showPermissionRationaleDialog();
                            } else {
                                showToast(R.string.permission_disallowed);
                            }
                        }
                    });
        }
    }

    AdapterView.OnItemClickListener mListItemClickListener = new AdapterView.OnItemClickListener() {

        public static final int MIN_CLICK_DELAY_TIME = 500;
        private long lastClickTime = 0;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mAdapter.getConnectState(position)) {
                startNextActivity();
                return;
            }

            long currentTime = Calendar.getInstance().getTimeInMillis();

            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                lastClickTime = currentTime;

                String curBondedDeviceName = SPWristbandConfigInfo.getBondedDevice(WifiConfigHomeActivity.this);

                final BluetoothDevice device = mAdapter.getDevice(position);
                mBluetoothDevice = device;

                if (device == null) return;
                //停止刷新
                refreshableView.finishRefreshing();

                if (curBondedDeviceName != null) {
                    if (mBluetoothDevice.getAddress().equals(curBondedDeviceName)) {
                        ZLogger.d(D, "Reconnect");
                        showToast(R.string.reconnecting);
                    } else {
                        showToast("请先断开当前连接");
                        return;
                    }
                } else {
                    ZLogger.i(D, "select device to connect, name=" + device.getName() + ", addr=" + device.getAddress());

                    BackgroundScanAutoConnected.getInstance().connectWristbandDevice(device);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialogFragment.show(getFragmentManager(), "loading");
                        }
                    });
                }

            }
        }
    };

    private void startNextActivity() {
        Intent intent = new Intent(this, WifiInfoActivity.class);
        startActivity(intent);
    }

    /**
     * if scanned device already in the list then update it otherwise add as a new device
     */
    private void addScannedDevice(final BluetoothDevice device, final int rssi) {
        mAdapter.addOrUpdateDevice(new ExtendedBluetoothDevice(device, device.getName(), rssi, ExtendedBluetoothDevice.DEVICE_NOT_BONDED, false));
    }

    private void addScannedDevice(final BluetoothDevice device, final String name, final int rssi) {
        mAdapter.addOrUpdateDevice(new ExtendedBluetoothDevice(device, name, rssi, ExtendedBluetoothDevice.DEVICE_NOT_BONDED, false));
    }

    private void setUI() {
        refreshableView.setOnRefreshListener(new RefreshableScanView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mAdapter != null) {
                            mAdapter.clearShowedDevices();
                        }
                        if (SPWristbandConfigInfo.getBondedDevice(WifiConfigHomeActivity.this) != null) {
                            showToast("请先断开当前连接");
                            refreshableView.finishRefreshing();
                            return;
                        }

                        accessCoarseLocation();
                    }
                });
            }
        }, 0);

        // Initializes list view adapter.
        mList.setMenuCreator(mSwipMenuCreator);
        mList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                ZLogger.d(D, "setOnMenuItemClickListener, index: " + index);
                switch (index) {
                    case 0:
                        // remove bond
                        removeBond(position);
                        break;
                }
            }
        });
        mAdapter = new DeviceListAdapter(this, this);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(mListItemClickListener);
    }

    BackgroundScanAutoConnected.BackgroundScanCallback mBackgroundScanCallback
            = new BackgroundScanAutoConnected.BackgroundScanCallback() {
        private long lastClickTime = 0;

        @Override
        public void onWristbandDeviceFind(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    ZLogger.d(D, "onLeScan() - Device name is: " + device.getName() +
                            " - address is: " + device.getAddress());
                    SpecScanRecord record = SpecScanRecord.parseFromBytes(scanRecord);
                    if (!TextUtils.isEmpty(record.getDeviceName())) {
                        SPWristbandConfigInfo.setInfoKeyValue(getApplication(), device.getAddress(), record.getDeviceName());
                        addScannedDevice(device, record.getDeviceName(), rssi);
                    } else {
                        // add device to adapter
                        SPWristbandConfigInfo.setInfoKeyValue(getApplication(), device.getAddress(), device.getName());
                        addScannedDevice(device, rssi);
                    }
                }
            });
        }

        public void onLeScanEnable(boolean enable) {
            ZLogger.d(D, "enable: " + enable);
            if (enable) {

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshableView.finishRefreshing();
                    }
                });
            }
        }

        public void onWristbandState(final boolean connected, final int state) {
            ZLogger.w(D, "onWristbandState is be called,connect:" + connected + ",state:" + state);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!connected) {
                        mAdapter.changeBondedDevicesState(false);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (dialogFragment.isVisible()){
                                    dialogFragment.dismiss();
                                }
                            }
                        });
                        addConnectedDevice(SPWristbandConfigInfo.getBondedDevice(WifiConfigHomeActivity.this));
                        mAdapter.clearShowedDevices();
                    }
                    accessCoarseLocation();
                }
            });
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (currentTime - lastClickTime > 2000) {
                lastClickTime = currentTime;
                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
        }
    };

    @Override
    public void onProgressBarTimeout() {
        ZLogger.w(D, "Wait Progress Timeout");
        showToast(R.string.progress_bar_timeout);
        BackgroundScanAutoConnected.getInstance().getGattLayerInstance().close();
        super.onProgressBarTimeout();
    }

    @Override
    protected void onResume() {
        ZLogger.d(D, "onResume()");
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            String address = SPWristbandConfigInfo.getBondedDevice(WifiConfigHomeActivity.this);
            if (address == null) {
                accessCoarseLocation();
            } else {
                addConnectedDevice(address);
            }
        }

    }

    private void addConnectedDevice(String address) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        String useName;
        String name = SPWristbandConfigInfo.getInfoKeyValue(this, device.getAddress());

        if (!TextUtils.isEmpty(name)) {
            useName = name;
        } else {
            useName = device.getName();
        }
        if (BackgroundScanAutoConnected.getInstance().isConnect()) {
            mAdapter.addBondedDevice(new ExtendedBluetoothDevice(device, useName,
                    ExtendedBluetoothDevice.NO_RSSI, ExtendedBluetoothDevice.DEVICE_IS_BONDED, true));
        } else{
            mAdapter.addBondedDevice(new ExtendedBluetoothDevice(device, useName,
                    ExtendedBluetoothDevice.NO_RSSI, ExtendedBluetoothDevice.DEVICE_IS_BONDED, false));
        }
    }

    @Override
    protected void onDestroy() {
        ZLogger.d(D, "onDestroy()");
        ActivityUtils.getInstance().removeActivity(this);
        super.onDestroy();

        mAdapter.clearShowedDevices();
        BackgroundScanAutoConnected.getInstance().closeConnect();
        BackgroundScanAutoConnected.getInstance().unregisterBluetoothOnOffAutoStartBroadcast();
        BackgroundScanAutoConnected.getInstance().unregisterCallback(mBackgroundScanCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ZLogger.d(D, "resultCode= " + resultCode);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    //BackgroundScanAutoConnected.getInstance().forceLeScan();
                    showToast(R.string.bluetooth_enabled);
                } else {
                    // User did not enable Bluetooth or an error occured
                    ZLogger.d(D, "BT not enabled");
                    showToast(R.string.bluetooth_not_enabled);
                    finish();
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder aa = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        aa.setTitle(R.string.toast_message);
        aa.setMessage(R.string.sure_to_exit);
        aa.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                BackgroundScanAutoConnected.getInstance().closeConnect();
                BackgroundScanAutoConnected.getInstance().unregisterBluetoothOnOffAutoStartBroadcast();
                BackgroundScanAutoConnected.getInstance().unregisterCallback(mBackgroundScanCallback);
                ((RealsilDemoApplication) (getApplicationContext())).closeAllActivity();
            }
        });
        aa.setNegativeButton(R.string.cancel, null);
        aa.create();
        aa.show();
    }


    SwipeMenuCreator mSwipMenuCreator = new SwipeMenuCreator() {

        @Override
        public void create(SwipeMenu menu) {
            // Create different menus depending on the view type
            switch (menu.getViewType()) {
                case DeviceListAdapter.TYPE_BONDED_ITEM:
                    createMenu1(menu);
                    break;
                default:
                    // do nothing
                    break;
            }
        }

        private void createMenu1(SwipeMenu menu) {
            SwipeMenuItem removeBondItem = new SwipeMenuItem(
                    getApplicationContext());
            // set item background
            removeBondItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                    0x3F, 0x25)));
            // set item width
            removeBondItem.setWidth(dp2px(90));
            // set item title
            removeBondItem.setTitle(getString(R.string.remove_bond));
            // set item title fontsize
            removeBondItem.setTitleSize(18);
            // set item title font color
            removeBondItem.setTitleColor(Color.WHITE);
            // add to menu
            menu.addMenuItem(removeBondItem);
        }
    };

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }


    private void forceRemoveBond(int position) {
        ZLogger.d(D, "remove bond");

        if (BackgroundScanAutoConnected.getInstance().isConnect()) {
            BackgroundScanAutoConnected.getInstance().getGattLayerInstance().close();
        } else {
            accessCoarseLocation();
        }
        mAdapter.clearDevices();

        SPWristbandConfigInfo.setBondedDevice(WifiConfigHomeActivity.this, null);
    }

    private void accessCoarseLocation() {
        int permissionCheck = ContextCompat.checkSelfPermission(WifiConfigHomeActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ZLogger.w(D, "permission not grated, " + Manifest.permission.ACCESS_COARSE_LOCATION);
            requestPermissions();
        } else {
            BackgroundScanAutoConnected.getInstance().startAutoConnect();
        }
    }
}
