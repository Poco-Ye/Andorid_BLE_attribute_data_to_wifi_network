package com.realsil.WifiConfig.utility;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.realsil.WifiConfig.R;
import com.realsil.sdk.core.bluetooth.scanner.ExtendedBluetoothDevice;
import com.realsil.sdk.core.logger.ZLogger;

import java.util.ArrayList;

/**
 * DeviceListAdapter class is list adapter for showing scanned Devices name, address and RSSI image based on RSSI values.
 */
public class DeviceListAdapter extends BaseAdapter {
    public static final int TYPE_TITLE = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_EMPTY = 2;
    public static final int TYPE_BONDED_ITEM = 3;

    private static final int TOTAL_TYPE_COUNT = 4;

    private final ArrayList<ExtendedBluetoothDevice> mListBondedValues = new ArrayList<>();
    private final ArrayList<ExtendedBluetoothDevice> mListValues = new ArrayList<>();
    private final Context mContext;
    private final ExtendedBluetoothDevice.AddressComparator comparator = new ExtendedBluetoothDevice.AddressComparator();
    private DeviceListCallback mCallback;

    public DeviceListAdapter(Context context, DeviceListCallback callback) {
        mCallback = callback;
        mContext = context;
    }


    public boolean getConnectState(int position) {
        return ((ExtendedBluetoothDevice) getItem(position)).isConnected;
    }

    public void addBondedDevice(ExtendedBluetoothDevice device) {
        final boolean indexInDevice = mListValues.contains(device);
        // if the bond device is un bonded device list, remove it.
        if (indexInDevice) {
            mListValues.remove(device);
        }
        final boolean indexInBonded = mListBondedValues.contains(device);
        // if the device is bonded device
        if (indexInBonded) {
            updateRssiOfBondedDevice(device.device.getAddress(), device.name, device.rssi, device.isConnected);
            return;
        }
        mListBondedValues.add(device);
        notifyDataSetChanged();
    }

    /**
     * Looks for the device with the same address as given one in the list of bonded devices. If the device has been found it updates its RSSI value.
     *
     * @param address the device address
     * @param rssi    the RSSI of the scanned device
     * @param state   the connect state
     */
    public void updateRssiOfBondedDevice(String address, String name, int rssi, boolean state) {
        comparator.address = address;
        final int indexInBonded = mListBondedValues.indexOf(comparator);
        if (indexInBonded >= 0) {
            ExtendedBluetoothDevice previousDevice = mListBondedValues.get(indexInBonded);

            ZLogger.d("previousDevice.rssi: " + previousDevice.rssi
                    + ", previousDevice.name: " + previousDevice.name
                    + ", previousDevice.isConnected: " + previousDevice.isConnected
                    + ", rssi: " + rssi
                    + ", name: " + name
                    + ", isConnected: " + state);
            if (previousDevice.name != null) {
                if (previousDevice.rssi != rssi
                        || !previousDevice.name.equals(name)
                        || previousDevice.isConnected != state) {
                    previousDevice.rssi = rssi;
                    previousDevice.name = name;
                    previousDevice.isConnected = state;
                    notifyDataSetChanged();
                }
            } else {
                if (previousDevice.rssi != rssi
                        || previousDevice.isConnected != state) {
                    previousDevice.rssi = rssi;
                    previousDevice.name = name;
                    previousDevice.isConnected = state;
                    notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * If such device exists on the bonded device list, this method does nothing. If not then the device is updated (rssi value) or added.
     *
     * @param device the device to be added or updated
     */
    public void addOrUpdateDevice(ExtendedBluetoothDevice device) {
        final boolean indexInBonded = mListBondedValues.contains(device);
        // if the device is bonded device
        if (indexInBonded) {
            updateRssiOfBondedDevice(device.device.getAddress(), device.name, device.rssi, device.isConnected);
            return;
        }

        final int indexInNotBonded = mListValues.indexOf(device);
        if (indexInNotBonded >= 0) {
            ExtendedBluetoothDevice previousDevice = mListValues.get(indexInNotBonded);
            previousDevice.rssi = device.rssi;
            previousDevice.name = device.name;
            notifyDataSetChanged();
            return;
        }
        mListValues.add(device);
        notifyDataSetChanged();
    }

    public void clearDevices() {
        mListBondedValues.clear();
        mListValues.clear();
        notifyDataSetChanged();
    }

    public void changeBondedDevicesState(boolean isConnected) {
        for (int i = 0; i < mListBondedValues.size(); i++) {
            ExtendedBluetoothDevice temp = mListBondedValues.get(i);
            temp.isConnected = isConnected;
        }
        notifyDataSetChanged();
    }

    public void clearShowedDevices() {
        mListValues.clear();
        notifyDataSetChanged();
    }

    public void deleteConnectedDevice(int position) {
        mListBondedValues.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        final int bondedCount = mListBondedValues.size() + 1; // 1 for the title
        final int availableCount = mListValues.isEmpty() ? 0 : mListValues.size() + 1; // 1 for title, 1 for empty text
        if (bondedCount == 1) {
            if (availableCount == 0) {
                return 2;
            }
            return availableCount;

        }
        return bondedCount + availableCount;
    }

    @Override
    public Object getItem(int position) {
        final int bondedCount = mListBondedValues.size() + 1; // 1 for the title
        if (mListBondedValues.isEmpty()) {
            if (position == 0)
                return mContext.getString(R.string.scanner_subtitle_not_bonded);
            else {
                return mListValues.get(position - 1);
            }
        } else {
            if (position == 0) {
                return mContext.getString(R.string.scanner_subtitle_bonded);
            }
            if (position < bondedCount) {
                return mListBondedValues.get(position - 1);
            }
            if (!mListValues.isEmpty()) {
                if (position == bondedCount) {
                    return mContext.getString(R.string.scanner_subtitle_not_bonded);
                }
                return mListValues.get(position - bondedCount - 1);
            }
            return null;
        }
    }

    public BluetoothDevice getDevice(int position) {
        return ((ExtendedBluetoothDevice) getItem(position)).device;
    }

    @Override
    public int getViewTypeCount() {
        return TOTAL_TYPE_COUNT;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return (getItemViewType(position) == TYPE_ITEM) || (getItemViewType(position) == TYPE_BONDED_ITEM);
    }

    @Override
    public int getItemViewType(int position) {
        // the first position must be a title
        if (position == 0) {
            return TYPE_TITLE;
        }
        // if have bonded devices, it must have a second title
        if (!mListBondedValues.isEmpty() && position == mListBondedValues.size() + 1) {
            return TYPE_TITLE;
        }
        // if no find devices
        if (position == getCount() - 1 && mListValues.isEmpty() && mListBondedValues.isEmpty()) {
            return TYPE_EMPTY;
        }
        final ExtendedBluetoothDevice dev = (ExtendedBluetoothDevice) getItem(position);
        if (dev.isBonded) {
            return TYPE_BONDED_ITEM;
        }
        // other is item type, means it enable
        return TYPE_ITEM;
    }

    public interface DeviceListCallback {
        public void removeBond(int position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View oldView, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        // get the view type
        final int type = getItemViewType(position);

        View view = oldView;
        switch (type) {
            case TYPE_EMPTY:
                if (view == null) {
                    view = inflater.inflate(R.layout.device_list_empty, parent, false);
                }
                break;
            case TYPE_TITLE:
                if (view == null) {
                    view = inflater.inflate(R.layout.device_list_title, parent, false);
                }
                final TextView title = (TextView) view;
                title.setText((String) getItem(position));
                break;
            default:
                if (view == null) {
                    view = inflater.inflate(R.layout.device_list_row, parent, false);
                    final ViewHolder holder = new ViewHolder();
                    holder.name = (TextView) view.findViewById(R.id.name);
                    holder.address = (TextView) view.findViewById(R.id.address);
                    holder.state = (TextView) view.findViewById(R.id.connect_state);
                    holder.battery = (ImageView) view.findViewById(R.id.battery);
                    holder.ivInfo = (ImageView) view.findViewById(R.id.ivInfo);
                    holder.rssi = (ImageView) view.findViewById(R.id.rssi);
                    view.setTag(holder);
                }

                final ExtendedBluetoothDevice dev = (ExtendedBluetoothDevice) getItem(position);
                final ViewHolder holder = (ViewHolder) view.getTag();
                // set the list item show information
                // set show name
                final String deviceName = dev.name;
                if (deviceName != null && deviceName.length() > 0) {
                    holder.name.setText(deviceName);
                } else {
                    holder.name.setText(mContext.getString(R.string.unknown_device));
                }
                // set show address
                holder.address.setText(dev.device.getAddress());

                // set connect state
                if (dev.isConnected) {
                    holder.state.setText("Connected");
                    holder.state.setTextColor(mContext.getResources().getColor(R.color.light_blue));
                } else {
                    holder.state.setText("Disconnected");
                    holder.state.setTextColor(mContext.getResources().getColor(R.color.darkgrey));
                }

                // set show rssi
                if (!dev.isBonded || dev.rssi != ExtendedBluetoothDevice.NO_RSSI) {
                    final int rssiPercent = (int) (100.0f * (127.0f + dev.rssi) / (127.0f + 20.0f));
                    // set show image of rssi
                    holder.rssi.setImageLevel(rssiPercent);
                    holder.rssi.setVisibility(View.VISIBLE);
                    holder.battery.setVisibility(View.GONE);
                    holder.ivInfo.setVisibility(View.GONE);
                } else {
                    holder.rssi.setVisibility(View.GONE);
                    if (dev.isConnected) {
                        // here we just use to read battery info, do not enable notification
                        holder.ivInfo.setVisibility(View.GONE);
                        holder.battery.setVisibility(View.VISIBLE);
                    } else {
                        holder.ivInfo.setVisibility(View.VISIBLE);
                        holder.battery.setVisibility(View.GONE);
                    }
                }
                break;
        }

        return view;
    }

    private class ViewHolder {
        private TextView name;
        private TextView address;
        private TextView state;
        private ImageView battery;
        private ImageView ivInfo;
        private ImageView rssi;
    }
}
