package com.alan.homeautomationapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "HOME_AUTOMATION_DB";
    private static final int DB_VERSION = 1;

    private static final String DEVICE_TABLE_NAME = "Dispositivos";
    private static final String DEVICE_ID_COL = "ID";
    private static final String DEVICE_NAME_COL = "Dispositivo";
    private static final String DEVICE_ROOM_COL = "Cômodo";
    private static final String DEVICE_TYPE_COL = "Tipo";
    private static final String DEVICE_DESIGNATOR_COL = "Identificação";

    private static final String LOCATION_TABLE_NAME = "Locais";
    private static final String LOCATION_ID_COL = "ID";
    private static final String LOCATION_NAME_COL = "Local";

    private static final String TYPE_TABLE_NAME = "Tipos";
    private static final String TYPE_ID_COL = "ID";
    private static final String TYPE_NAME_COL = "Tipo";
    private static final String TYPE_PREFIX_COL = "Prefixo";


    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + DEVICE_TABLE_NAME + " ("
                + DEVICE_ID_COL + " INTEGER PRIMARY KEY, "
                + DEVICE_NAME_COL + " TEXT,"
                + DEVICE_ROOM_COL + " TEXT,"
                + DEVICE_TYPE_COL + " TEXT,"
                + DEVICE_DESIGNATOR_COL + " TEXT)";
        db.execSQL(query);

        query = "CREATE TABLE " + LOCATION_TABLE_NAME + " ("
                + LOCATION_ID_COL + " INTEGER PRIMARY KEY, "
                + LOCATION_NAME_COL + " TEXT)";
        db.execSQL(query);

        query = "CREATE TABLE " + TYPE_TABLE_NAME + " ("
                + TYPE_ID_COL + " INTEGER PRIMARY KEY, "
                + TYPE_NAME_COL + " TEXT,"
                + TYPE_PREFIX_COL + " TEXT)";
        db.execSQL(query);

        ContentValues values = new ContentValues();
        values.put(TYPE_NAME_COL, "Iluminação");
        values.put(TYPE_PREFIX_COL, "RELAY");
        db.insert(TYPE_TABLE_NAME, null, values);

        values = new ContentValues();
        values.put(TYPE_NAME_COL, "Ar condicionado");
        values.put(TYPE_PREFIX_COL, "AIR");
        db.insert(TYPE_TABLE_NAME, null, values);

        //db.close();
    }

    public void addNewDevice(String deviceName, String deviceRoom, String deviceType,
                             String deviceDesignator) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DEVICE_NAME_COL, deviceName);
        values.put(DEVICE_ROOM_COL, deviceRoom);
        values.put(DEVICE_TYPE_COL, deviceType);
        values.put(DEVICE_DESIGNATOR_COL, deviceDesignator);

        db.insert(DEVICE_TABLE_NAME, null, values);
        db.close();
    }

    public void addNewLocation(String locationName) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(LOCATION_NAME_COL, locationName);

        db.insert(LOCATION_TABLE_NAME, null, values);
        db.close();
    }

    public List<String> getLocations() {

        SQLiteDatabase db=this.getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT " + LOCATION_NAME_COL + " from "
                + LOCATION_TABLE_NAME, null);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String location = cursor.getString(cursor.getColumnIndex(LOCATION_NAME_COL));
            list.add(location);
        }
        cursor.close();
        return list;
    }

    public List<String> getDeviceTypes() {

        SQLiteDatabase db=this.getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT " + TYPE_NAME_COL + " from "
                + TYPE_TABLE_NAME, null);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(TYPE_NAME_COL));
            list.add(type);
        }
        cursor.close();
        return list;
    }

    public Integer getTypeCount(String type) {

        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + TYPE_PREFIX_COL + " from " + TYPE_TABLE_NAME
                + " WHERE " + TYPE_NAME_COL + "='" + type + "'", null);
        cursor.moveToFirst();

        @SuppressLint("Range") String prefix = cursor.getString(cursor.getColumnIndex(TYPE_PREFIX_COL));
        cursor.close();

        cursor = db.rawQuery("SELECT COUNT(" + DEVICE_DESIGNATOR_COL + ") from "
                + DEVICE_TABLE_NAME + " WHERE " + DEVICE_DESIGNATOR_COL + " LIKE '" + prefix
                +"%'", null);

        cursor.moveToFirst();
        int typeCount = cursor.getInt(0);

        cursor.close();
        return typeCount;
    }

    public String getDesignator(String type) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + TYPE_PREFIX_COL + " from " + TYPE_TABLE_NAME
                + " WHERE " + TYPE_NAME_COL + "='" + type + "'", null);
        cursor.moveToFirst();

        @SuppressLint("Range") String prefix = cursor.getString(cursor.getColumnIndex(TYPE_PREFIX_COL));
        cursor.close();

        return prefix + getTypeCount(type) + 1;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DEVICE_TABLE_NAME);
        onCreate(db);
    }
}