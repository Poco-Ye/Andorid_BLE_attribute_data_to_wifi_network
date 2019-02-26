package com.realsil.WifiConfig.utility;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class RealsilFragmentPagerAdapter extends FragmentStatePagerAdapter {
    ArrayList<Fragment> list;
    FragmentManager fm;

    public RealsilFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> list) {
        super(fm);
        this.fm = fm;
        this.list = list;
    }

    public void clear() {
        for (int i = 0; i < list.size(); i++) {
            fm.beginTransaction().remove(list.get(i)).commitNow();
        }

        list.clear();
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    public void setList(ArrayList<Fragment> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
