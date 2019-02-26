package com.realsil.WifiConfig;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;

import com.realsil.WifiConfig.backgroundscan.BackgroundScanAutoConnected;
import com.realsil.WifiConfig.utility.ActivityUtils;
import com.realsil.WifiConfig.utility.DeviceListAdapter;
import com.realsil.WifiConfig.utility.RefreshableScanView;
import com.realsil.WifiConfig.utility.WifiInfoListAdapter;
import com.realsil.WifiConfig.view.LoadingDialogFragment;
import com.realsil.WifiConfig.view.SwipeBackActivity;
import com.realsil.WifiConfig.view.SwipeMenu;
import com.realsil.WifiConfig.view.SwipeMenuCreator;
import com.realsil.WifiConfig.view.SwipeMenuItem;
import com.realsil.WifiConfig.view.SwipeMenuListView;
import com.realsil.sdk.core.logger.ZLogger;
import com.realsil.wificonfigprofile.WifiConfigProfile;
import com.realsil.wificonfigprofile.WifiConfigProfileCallBack;
import com.realsil.wificonfigprofile.entity.WifiConfigInfo;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.realsil.sdk.core.RtkCore.isBluetoothSupported;
import static com.realsil.wificonfigprofile.WifiConfigProfile.STATUS_FAILED;
import static com.realsil.wificonfigprofile.WifiConfigProfile.STATUS_PSW_ERROR;
import static com.realsil.wificonfigprofile.WifiConfigProfile.STATUS_SUCCESS;

public class WifiInfoActivity extends SwipeBackActivity implements DeviceListAdapter.DeviceListCallback {
    // Log
    private final static String TAG = "WifiInfoActivity";
    private final static boolean D = true;

    private static final int REQUEST_ENABLE_BT = 1;

    @BindView(R.id.lvWristbandDevice)
    SwipeMenuListView mList;
    @BindView(R.id.refreshable_view)
    RefreshableScanView refreshableView;
    @BindView(R.id.ivScanBack)
    ImageView mivScanBack;

    private WifiInfoListAdapter mAdapter;
    private WifiConfigProfile wifiConfigProfile;
    private final LoadingDialogFragment dialogFragment = new LoadingDialogFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_wifi_aps);

        ButterKnife.bind(this);
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

        // set UI
        setUI();
        initialUI();
    }

    @Override
    public void removeBond(int position) {
        removeBondAlert(position);
    }

    private void removeBondAlert(final int position) {
        new AlertDialog.Builder(WifiInfoActivity.this, AlertDialog.THEME_HOLO_LIGHT)
                .setTitle("Remove Aps")
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
        wifiConfigProfile = WifiConfigProfile.getInstance();
        wifiConfigProfile.setWifiConfigProfileCallBack(wifiConfigProfileCallBack);
        dialogFragment.show(getFragmentManager(), "loading");
        dialogFragment.dismiss();
    }

    WifiConfigProfileCallBack wifiConfigProfileCallBack = new WifiConfigProfileCallBack() {
        @Override
        public void phoneCmdResut(byte status) {

        }

        @Override
        public void didScanAp(WifiConfigInfo wifiConfigInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addScannedDevice(wifiConfigInfo);
                }
            });
        }

        @Override
        public void didConnectComplete(byte status, String mac, byte rssi) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogFragment.dismiss();
                }
            });

            if (status == STATUS_SUCCESS){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addConnectedDevice(mac);
                        showToast("Connect succcess");
                    }
                });
            }else if (status == STATUS_PSW_ERROR){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("Password error");
                    }
                });
            }else if (status == STATUS_FAILED){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("Connect fail");
                    }
                });
            }
        }

        @Override
        public void didDisConnectToDevice(String mac) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogFragment.dismiss();
                    mAdapter.clearConnectedDevice();
                    showToast("Disonnect to wifi");
                }
            });
        }

        @Override
        public void didScanComplete() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshableView.finishRefreshing();
                }
            });
        }
    };

    AdapterView.OnItemClickListener mListItemClickListener = new AdapterView.OnItemClickListener() {

        public static final int MIN_CLICK_DELAY_TIME = 500;
        private long lastClickTime = 0;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            long currentTime = Calendar.getInstance().getTimeInMillis();

            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                lastClickTime = currentTime;

                WifiConfigInfo wifiConfigInfo = null;
                if (mAdapter.getItem(position) instanceof WifiConfigInfo){
                    wifiConfigInfo = (WifiConfigInfo) mAdapter.getItem(position);
                } else {
                    return;
                }
                if (wifiConfigInfo.isConnected()) {
                    showToast("This wifi ap is connected");
                    return;
                }
                if (wifiConfigInfo.getEncryptType() == 0){
                    wifiConfigProfile.connectWifiDevice(wifiConfigInfo,"");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialogFragment.show(getFragmentManager(), "loading");
                        }
                    });
                    return;
                }

                final EditText et = new EditText(WifiInfoActivity.this);
                et.setSingleLine(true);
                et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                final AlertDialog builder = new AlertDialog.Builder(WifiInfoActivity.this).
                        setTitle("Set Password").setView(et).
                        setPositiveButton(R.string.sure, null).
                        setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                builder.show();

                WifiConfigInfo finalWifiConfigInfo = wifiConfigInfo;
                builder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String input = et.getText().toString();
                        if (input.length() < 8) {
                            showToast("The password should be at least 8 characters in length.");
                            return;
                        }
                        builder.dismiss();
                        ZLogger.d("password: " + input);
                        wifiConfigProfile.connectWifiDevice(finalWifiConfigInfo,input);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialogFragment.show(getFragmentManager(), "loading");
                            }
                        });
                    }
                });

            }
        }
    };

    /**
     * if scanned device already in the list then update it otherwise add as a new device
     */
    private void addScannedDevice(WifiConfigInfo wifiConfigInfo) {
        mAdapter.addOrUpdateDevice(wifiConfigInfo);
    }

    private void setUI() {
        mivScanBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        refreshableView.setOnRefreshListener(new RefreshableScanView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mAdapter != null) {
                            mAdapter.clearDevices();
                        }

                        wifiConfigProfile.searchWifiDevices();
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
        mAdapter = new WifiInfoListAdapter(this);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(mListItemClickListener);
    }

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
        wifiConfigProfile.searchWifiDevices();
    }

    private void addConnectedDevice(String mac) {
        mAdapter.addBondedDevice(mac);
    }

    @Override
    protected void onDestroy() {
        ZLogger.d(D, "onDestroy()");
        ActivityUtils.getInstance().removeActivity(this);
        super.onDestroy();
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
        finish();
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
        ZLogger.d(D, "remove ap");
        wifiConfigProfile.cancelConnectWifiDevice();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogFragment.show(getFragmentManager(), "loading");
            }
        });
    }

}
