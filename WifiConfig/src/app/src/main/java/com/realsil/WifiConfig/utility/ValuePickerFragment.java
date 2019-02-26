package com.realsil.WifiConfig.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.realsil.WifiConfig.R;
import com.realsil.WifiConfig.view.CycleWheelView;
import com.realsil.sdk.core.logger.ZLogger;

import java.util.ArrayList;

public class ValuePickerFragment extends DialogFragment {
    private static Context mContext;
    private OnSaveListener mListener;

    // Type
    public final static int TYPE_MESH_PROVISION = 0;
    public final static int TYPE_MESH_ADD_KEY = 1;
    public final static int TYPE_MESH_ADD_GROUP = 2;
    private int mType = TYPE_MESH_PROVISION;
    public static final String EXTRAS_VALUE_TYPE = "VALUE_TYPE";
    public static final String EXTRAS_VALUE_DEFAULT = "VALUE_DEFAULT";

    private int mDefaultValue = 0;

    private RelativeLayout mrlSave;
    private RelativeLayout mrlCancel;

    private CycleWheelView mcwvWristSetValue;

    /**
     * Static implementation of fragment so that it keeps data when phone orientation is changed.
     * For standard BLE Service UUID, we can filter devices using normal android provided command
     * startScanLe() with required BLE Service UUID
     * For custom BLE Service UUID, we will use class ScannerServiceParser to filter out required
     * device.
     */
    public static ValuePickerFragment getInstance(Context context) {
        final ValuePickerFragment fragment = new ValuePickerFragment();
        mContext = context;
        return fragment;
    }

    /**
     * When dialog is created then set AlertDialog with list and button views
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_value_picker, null);
        final AlertDialog dialog = builder.setView(dialogView).create();

        mType = getArguments().getInt(EXTRAS_VALUE_TYPE);
        mDefaultValue = getArguments().getInt(EXTRAS_VALUE_DEFAULT);
        ZLogger.i("mType: " + mType + ", mDefaultValue: " + mDefaultValue);

        // initial UI
        mcwvWristSetValue = (CycleWheelView) dialogView.findViewById(R.id.cwvWristSetValue);
        ArrayList<String> labels = new ArrayList<>();
        int min = 0;
        int max = 0;
        switch (mType) {
            case TYPE_MESH_PROVISION:
            case TYPE_MESH_ADD_KEY:
            case TYPE_MESH_ADD_GROUP:
                max = mDefaultValue;
                break;
        }
        for (int i = min; i < max; i++) {
            labels.add(String.valueOf(i));
        }
        labels.add("New");
        mcwvWristSetValue.setLabels(labels);
        mcwvWristSetValue.setSelection(min);

        mrlSave = (RelativeLayout) dialogView.findViewById(R.id.rlWristValueSure);
        mrlSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int returnValue = 0;
                switch (mType) {
                    case TYPE_MESH_PROVISION:
                    case TYPE_MESH_ADD_KEY:
                    case TYPE_MESH_ADD_GROUP:
                        if ("New".equals(mcwvWristSetValue.getSelectLabel())){
                            returnValue = -1;
                        } else {
                            returnValue = Integer.valueOf(mcwvWristSetValue.getSelectLabel());
                        }
                        break;
                }

                // cancel
                dialog.cancel();
                // tell the saved
                mListener.onValueInfoSaved(mType, returnValue);
            }
        });

        mrlCancel = (RelativeLayout) dialogView.findViewById(R.id.rlWristValueCancel);
        mrlCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // cancel
                dialog.cancel();
            }
        });

        return dialog;
    }

    /**
     * Interface required to be implemented by activity
     */
    public static interface OnSaveListener {
        /**
         * Fired when user click the save button
         *
         * @param type  the value type
         * @param value the select value
         */
        public void onValueInfoSaved(int type, int value);
    }

    /**
     * This will make sure that {@link OnSaveListener} interface is implemented by activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnSaveListener) activity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnSaveListener");
        }
    }
}
