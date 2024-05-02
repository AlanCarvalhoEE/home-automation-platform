package com.alan.homeautomationapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "HOME_AUTOMATION_DB";
    private static final int DB_VERSION = 1;

    private static final String DEVICE_TABLE_NAME = "Dispositivos";
    private static final String DEVICE_ID_COL = "ID";
    private static final String DEVICE_NAME_COL = "Dispositivo";
    private static final String DEVICE_ROOM_COL = "Cômodo";
    private static final String DEVICE_TYPE_COL = "Tipo";
    private static final String DEVICE_DESIGNATOR_COL = "Designador";
    private static final String DEVICE_ADDRESS_COL = "Endereço";

    private static final String ROOM_TABLE_NAME = "Cômodos";
    private static final String ROOM_ID_COL = "ID";
    private static final String ROOM_NAME_COL = "Cômodo";

    private static final String TYPE_TABLE_NAME = "Tipos";
    private static final String TYPE_ID_COL = "ID";
    private static final String TYPE_NAME_COL = "Tipo";
    private static final String TYPE_PREFIX_COL = "Prefixo";

    private static final String USER_TABLE_NAME = "Usuários";
    private static final String USER_ID_COL = "ID";
    private static final String USER_NAME_COL = "Usuário";
    private static final String USER_PASSWORD_COL = "Senha";
    private static final String USER_LEVEL_COL = "Nível";

    private static DBHandler instance;

    private DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized DBHandler getInstance(Context context) {
        if (instance == null) {
            instance = new DBHandler(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + DEVICE_TABLE_NAME + " ("
                + DEVICE_ID_COL + " INTEGER PRIMARY KEY, "
                + DEVICE_NAME_COL + " TEXT,"
                + DEVICE_ROOM_COL + " TEXT,"
                + DEVICE_TYPE_COL + " TEXT,"
                + DEVICE_DESIGNATOR_COL + " TEXT,"
                + DEVICE_ADDRESS_COL + " TEXT)";
        db.execSQL(query);

        query = "CREATE TABLE " + ROOM_TABLE_NAME + " ("
                + ROOM_ID_COL + " INTEGER PRIMARY KEY, "
                + ROOM_NAME_COL + " TEXT)";
        db.execSQL(query);

        query = "CREATE TABLE " + TYPE_TABLE_NAME + " ("
                + TYPE_ID_COL + " INTEGER PRIMARY KEY, "
                + TYPE_NAME_COL + " TEXT,"
                + TYPE_PREFIX_COL + " TEXT)";
        db.execSQL(query);

        query = "CREATE TABLE " + USER_TABLE_NAME + " ("
                + USER_ID_COL + " INTEGER PRIMARY KEY, "
                + USER_NAME_COL + " TEXT,"
                + USER_PASSWORD_COL + " TEXT,"
                + USER_LEVEL_COL + " TEXT)";
        db.execSQL(query);
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

    public void addNewRoom(String roomName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(ROOM_NAME_COL, roomName);
        db.insert(ROOM_TABLE_NAME, null, values);
        db.close();
    }

    public void addNewType(String typeName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TYPE_NAME_COL, typeName);
        db.insert(TYPE_TABLE_NAME, null, values);
        db.close();
    }

    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(DEVICE_TABLE_NAME, null, null);
        db.delete(ROOM_TABLE_NAME, null, null);
        db.delete(TYPE_TABLE_NAME, null, null);
        db.delete(USER_TABLE_NAME, null, null);
    }

    public void updateDatabase(String databaseString) {
        clearDatabase();

        int startIndex = databaseString.indexOf("-") + 1;
        String data = databaseString.substring(startIndex);
        String[] tables = data.split("/");

        for (int i = 0; i < tables.length; i++) {
            tables[i] = tables[i].substring(1, tables[i].length() - 1);
            String[] rows = tables[i].split("], \\[");

            if (rows.length > 0) {
                for (int j = 0; j < rows.length; j++) {
                    rows[j] = rows[j].replaceAll("[\\[\\]]", "");
                    String[] fields = rows[j].split(", ");

                    if (fields.length > 1) {
                        for (int k = 0; k < fields.length; k++) fields[k] = fields[k].replace("\"", "");

                        if (i == 0) addNewDevice(fields[1], fields[2], fields[3], fields[4]);
                        else if (i == 1) addNewRoom(fields[1]);
                        else if (i == 2) addNewType(fields[1]);
                    }
                }
            }
        }
    }

    public List<String> getRoomsList() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT " + ROOM_NAME_COL + " from " + ROOM_TABLE_NAME, null);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String room = cursor.getString(cursor.getColumnIndex(ROOM_NAME_COL));
            list.add(room);
        }
        cursor.close();
        return list;
    }

    public List<String> getDevicesList(String room) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT " + DEVICE_NAME_COL + " from " + DEVICE_TABLE_NAME
                + " WHERE " + DEVICE_ROOM_COL + "='" + room + "'", null);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String device = cursor.getString(cursor.getColumnIndex(DEVICE_NAME_COL));
            list.add(device);
        }
        cursor.close();
        return list;
    }

    public List<String> getTypeList() {
        SQLiteDatabase db = this.getReadableDatabase();
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

    public String getType(String deviceName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DEVICE_TYPE_COL + " from " + DEVICE_TABLE_NAME
                + " WHERE " + DEVICE_NAME_COL + "='" + deviceName + "'", null);
        cursor.moveToFirst();
        @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(DEVICE_TYPE_COL));
        cursor.close();
        return type;
    }

    public String getDesignator(String deviceName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DEVICE_DESIGNATOR_COL + " from "
                + DEVICE_TABLE_NAME + " WHERE " + DEVICE_NAME_COL + "='" + deviceName
                + "'", null);
        cursor.moveToFirst();
        @SuppressLint("Range") String designator = cursor.getString(cursor.getColumnIndex(DEVICE_DESIGNATOR_COL));
        cursor.close();
        return designator;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DEVICE_TABLE_NAME);
        onCreate(db);
    }
}
