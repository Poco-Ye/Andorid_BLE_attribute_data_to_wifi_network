package com.realsil.WifiConfigGreendao;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;


public class WifiConfigGen {
    private static final int DATA_BASE_VERSION = 2;
    private static final String GENERATE_PACKAGE_NAME = "com.realsil.android.wifiConfig.greendao";
    private static final String GENERATE_PATH = "../app/src/main/java-gen";


    public static void main(String[] args) throws Exception {
        // Database version, package name
        Schema schema = new Schema(DATA_BASE_VERSION, GENERATE_PACKAGE_NAME);

        // add table
        addMeshDevice(schema);
        // Create class
        new DaoGenerator().generateAll(schema, GENERATE_PATH);
    }

    private static void addMeshDevice(Schema schema) {
        Entity meshDevice = schema.addEntity("WifiConfigDevice");

        // set column
        meshDevice.addIdProperty();
        meshDevice.addStringProperty("deviceName");
        meshDevice.addStringProperty("deviceAddress").notNull();
        meshDevice.addBooleanProperty("isConnected");
        meshDevice.addIntProperty("rssi");
    }
}