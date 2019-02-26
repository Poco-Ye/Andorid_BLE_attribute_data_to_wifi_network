package com.realsil.WifiConfig.view;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.realsil.WifiConfig.R;

/**
 * Created by neil_zhou on 2018/10/12.
 */

public class LoadingDialogFragment extends DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //去掉默认的title
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉白色边角
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = inflater.inflate(R.layout.fragment_loading, container);
        return view;
    }
}
