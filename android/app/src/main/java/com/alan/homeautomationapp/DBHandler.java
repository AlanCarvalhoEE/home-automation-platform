package com.alan.homeautomationapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "HOME_AUTOMATION_DB";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "Dispositivos";
    private static final String ID_COL = "ID";
    private static final String NAME_COL = "Dispositivo";
    private static final String ROOM_COL = "Cômodo";
    private static final String TYPE_COL = "Tipo";
    private static final String MODEL_COL = "Modelo";
    private static final String DESIGNATOR_COL = "Identificação";

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, "
                + NAME_COL + " TEXT,"
                + ROOM_COL + " TEXT,"
                + TYPE_COL + " TEXT,"
                + MODEL_COL + " TEXT,"
                + DESIGNATOR_COL + " TEXT)";

        db.execSQL(query);
    }

    public void addNewDevice(String deviceName, String deviceRoom, String deviceType,
                             String deviceModel, String deviceDesignator) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(NAME_COL, deviceName);
        values.put(ROOM_COL, deviceRoom);
        values.put(TYPE_COL, deviceType);
        values.put(MODEL_COL, deviceModel);
        values.put(DESIGNATOR_COL, deviceDesignator);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}