package com.realsil.WifiConfig.utility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.realsil.WifiConfig.R;
import com.realsil.sdk.core.logger.ZLogger;
import com.realsil.wificonfigprofile.entity.WifiConfigInfo;

import java.util.ArrayList;

/**
 * DeviceListAdapter class is list adapter for showing scanned Devices name, address and RSSI image based on RSSI values.
 */
public class WifiInfoListAdapter extends BaseAdapter {
    public static final int TYPE_TITLE = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_EMPTY = 2;
    public static final int TYPE_BONDED_ITEM = 3;

    private static final int TOTAL_TYPE_COUNT = 4;

    private final ArrayList<WifiConfigInfo> mListBondedValues = new ArrayList<>();
    private final ArrayList<WifiConfigInfo> mListValues = new ArrayList<>();
    private final Context mContext;
    private final WifiConfigInfo.AddressComparator comparator = new WifiConfigInfo.AddressComparator();

    public WifiInfoListAdapter(Context context) {
        mContext = context;
    }

    public boolean getConnectState(int position) {
        return ((WifiConfigInfo) getItem(position)).isConnected();
    }

    public void addBondedDevice(String mac) {
        WifiConfigInfo device = null;
        for (WifiConfigInfo temp : mListValues) {
            if (temp.getMacString().equals(mac)) {
                device = temp;
                break;
            }
        }
        if (device != null) {
            mListValues.remove(device);
            device.setConnected(true);
        } else {
            return;
        }
        mListBondedValues.clear();
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
            WifiConfigInfo previousDevice = mListBondedValues.get(indexInBonded);

            ZLogger.d("previousDevice.rssi: " + previousDevice.getRssi()
                    + ", previousDevice.name: " + previousDevice.getSsid()
                    + ", previousDevice.isConnected: " + previousDevice.isConnected()
                    + ", rssi: " + rssi
                    + ", name: " + name
                    + ", isConnected: " + state);
            if (previousDevice.getSsid() != null) {
                if (previousDevice.getRssi() != rssi
                        || !previousDevice.getSsid().equals(name)
                        || previousDevice.isConnected() != state) {
                    previousDevice.setRssi((byte) rssi);
                    previousDevice.setSsid(name);
                    previousDevice.setConnected(state);
                    notifyDataSetChanged();
                }
            } else {
                if (previousDevice.getRssi() != rssi
                        || previousDevice.isConnected() != state) {
                    previousDevice.setRssi((byte) rssi);
                    previousDevice.setSsid(name);
                    previousDevice.setConnected(state);
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
    public void addOrUpdateDevice(WifiConfigInfo device) {
        final boolean indexInBonded = mListBondedValues.contains(device);
        // if the device is bonded device
        if (indexInBonded) {
            updateRssiOfBondedDevice(device.getMacString(), device.getSsid(), device.getRssi(), device.isConnected());
            return;
        }

        final int indexInNotBonded = mListValues.indexOf(device);
        if (indexInNotBonded >= 0) {
            WifiConfigInfo previousDevice = mListValues.get(indexInNotBonded);
            previousDevice.setRssi(device.getRssi());
            previousDevice.setSsid(device.getSsid());
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
            WifiConfigInfo temp = mListBondedValues.get(i);
            temp.setConnected(isConnected);
        }
        notifyDataSetChanged();
    }

    public void clearShowedDevices() {
        mListValues.clear();
        notifyDataSetChanged();
    }

    public void clearConnectedDevice() {
        mListBondedValues.clear();
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
                return "CONNECTABLE APS";
            else {
                return mListValues.get(position - 1);
            }
        } else {
            if (position == 0) {
                return "CONNECTED AP";
            }
            if (position < bondedCount) {
                return mListBondedValues.get(position - 1);
            }
            if (!mListValues.isEmpty()) {
                if (position == bondedCount) {
                    return "CONNECTABLE APS";
                }
                return mListValues.get(position - bondedCount - 1);
            }
            return null;
        }
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
        final WifiConfigInfo dev = (WifiConfigInfo) getItem(position);
        if (dev.isConnected()) {
            return TYPE_BONDED_ITEM;
        }
        // other is item type, means it enable
        return TYPE_ITEM;
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
                    view = inflater.inflate(R.layout.wifi_info_list_row, parent, false);
                    final ViewHolder holder = new ViewHolder();
                    holder.name = (TextView) view.findViewById(R.id.name);
                    holder.address = (TextView) view.findViewById(R.id.address);
                    holder.state = (TextView) view.findViewById(R.id.connect_state);
                    holder.lock = (ImageView) view.findViewById(R.id.lock);
                    holder.rssi = (ImageView) view.findViewById(R.id.rssi);
                    view.setTag(holder);
                }

                final WifiConfigInfo dev = (WifiConfigInfo) getItem(position);
                final ViewHolder holder = (ViewHolder) view.getTag();
                // set the list item show information
                // set show name
                final String deviceName = dev.getSsid();
                if (deviceName != null && deviceName.length() > 0) {
                    holder.name.setText(deviceName);
                } else {
                    holder.name.setText(mContext.getString(R.string.unknown_device));
                }
                // set show address
                holder.address.setText(dev.getMacString());

                // set connect state
                if (dev.isConnected()) {
                    holder.state.setText("Connected");
                    holder.state.setTextColor(mContext.getResources().getColor(R.color.light_blue));
                } else {
                    holder.state.setText("Disconnected");
                    holder.state.setTextColor(mContext.getResources().getColor(R.color.darkgrey));
                }
                if (dev.getEncryptType() == 0) {
                    holder.lock.setImageResource(R.mipmap.unlock);
                } else {
                    holder.lock.setImageResource(R.mipmap.green_lock);
                }
                holder.rssi.setImageLevel(dev.getRssi()+127);
                break;
        }

        return view;
    }

    private class ViewHolder {
        private TextView name;
        private TextView address;
        private TextView state;
        private ImageView lock;
        private ImageView rssi;
    }
}
