package com.realsil.WifiConfig.utility;

import android.app.Activity;

import java.util.ArrayList;

/**
 * Created by rain1_wen on 2017/1/15.
 */

public class ActivityUtils {
    private static ActivityUtils manager;
    private ArrayList<Activity> list = new ArrayList();

    private ActivityUtils() {
    }

    public static synchronized ActivityUtils getInstance() {
        if(manager == null) {
            manager = new ActivityUtils();
        }

        return manager;
    }

    public ArrayList<Activity> getAliveActivity() {
        return this.list;
    }

    public void addActivity(Activity activity) {
        this.list.add(activity);
    }

    public void removeActivity(Activity activity) {
        this.list.remove(activity);
    }
}