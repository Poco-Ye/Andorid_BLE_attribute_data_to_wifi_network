package com.realsil.WifiConfig.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.realsil.android.wifiConfig.greendao.DaoMaster;
import com.realsil.android.wifiConfig.greendao.DaoSession;
import com.realsil.android.wifiConfig.greendao.WifiConfigDevice;
import com.realsil.android.wifiConfig.greendao.WifiConfigDeviceDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author neil_zhou
 * @version v1.0.2
 */
public class GlobalGreenDAO {
    // Log
    private static final String TAG = GlobalGreenDAO.class.getSimpleName();
    private static final boolean D = true;

    // object
    private static GlobalGreenDAO mInstance;
    private static Context mAppContext;

    // data base object
    private static DaoMaster mDaoMaster;
    private static DaoSession mDaoSession;
    private static SQLiteDatabase mSqlDb;
    private static String DB_NAME = "wificonfig-db";

    // table object
    private WifiConfigDeviceDao wifiConfigDeviceDao;

    /**
     * get DaoMaster
     *
     * @param context
     * @return
     */
    public static DaoMaster getDaoMaster(Context context) {
        if (mDaoMaster == null) {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
            mDaoMaster = new DaoMaster(helper.getWritableDatabase());
        }
        return mDaoMaster;
    }

    /**
     * get DaoSession
     *
     * @param context
     * @return
     */
    public static DaoSession getDaoSession(Context context) {
        if (mDaoSession == null) {
            if (mDaoMaster == null) {
                mDaoMaster = getDaoMaster(context);
            }
            mDaoSession = mDaoMaster.newSession();
        }
        return mDaoSession;
    }

    public static SQLiteDatabase getSQLDatebase(Context context) {
        if (mSqlDb == null) {
            if (mDaoMaster == null) {
                mDaoMaster = getDaoMaster(context);
            }
            mSqlDb = mDaoMaster.getDatabase();
        }
        return mSqlDb;
    }

    public static SQLiteDatabase getSQLDatebase() {
        return mSqlDb;
    }

    // Global Green Dao only create one times

    /**
     * Global Green Dao only create one times
     * @param context
     */
    public static void initial(Context context) {
        if (mInstance == null) {
            mInstance = new GlobalGreenDAO();
            if (mAppContext == null) {
                mAppContext = context.getApplicationContext();
            }
            mInstance.mSqlDb = getSQLDatebase(context);
            mInstance.mDaoSession = getDaoSession(context);

            mInstance.wifiConfigDeviceDao = mInstance.mDaoSession.getWifiConfigDeviceDao();
        }
    }

    /**
     * singlon pattern
     * @return
     */
    public static GlobalGreenDAO getInstance() {
        return mInstance;
    }

    /**
     * delete WifiConfigDevice
     * @param id
     */
    public void deleteWifiConfigDevice(long id) {
        wifiConfigDeviceDao.deleteByKey(id);
    }

    public void deleteAll(){
        wifiConfigDeviceDao.deleteAll();
    }

    /**
     * update WifiConfigDevice
     * @param wifiConfigDevice
     */
    public void updateWifiConfigDevice(WifiConfigDevice wifiConfigDevice) {
        wifiConfigDeviceDao.update(wifiConfigDevice);
    }

    /**
     * get all WifiConfigDevice
     * @return
     */
    public List<WifiConfigDevice> getAllWifiConfigDevice() {
        return wifiConfigDeviceDao.loadAll();
    }


    /**
     * insert WifiConfigDevice
     * @param node
     */
    public void insertWifiConfigDevice(WifiConfigDevice node) {
        wifiConfigDeviceDao.insert(node);
    }
}
