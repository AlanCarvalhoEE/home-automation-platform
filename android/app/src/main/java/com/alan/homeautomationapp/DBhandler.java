package com.alan.homeautomationapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DBhandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "HOME_AUTOMATION_DB";
    private static final int DB_VERSION = 1;

    private static final String DEVICES_TABLE_NAME = "Devices";
    private static final String DEVICE_ID_COL = "ID";
    private static final String DEVICE_NAME_COL = "Name";
    private static final String DEVICE_ROOM_COL = "Room";
    private static final String DEVICE_TYPE_COL = "Type";
    private static final String DEVICE_TOPIC_COL = "Topic";

    private static final String ROOMS_TABLE_NAME = "Rooms";
    private static final String ROOM_ID_COL = "ID";
    private static final String ROOM_NAME_COL = "Room";
    private static final String ROOM_TOPIC_COL = "Topic";

    private static final String TYPES_TABLE_NAME = "Types";
    private static final String TYPE_ID_COL = "ID";
    private static final String TYPE_NAME_COL = "Type";

    private static final String USERS_TABLE_NAME = "Users";
    private static final String USER_ID_COL = "ID";
    private static final String USER_NAME_COL = "User";
    private static final String USER_PASSWORD_COL = "Password";
    private static final String USER_LEVEL_COL = "Level";

    private static DBhandler instance;

    private DBhandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized DBhandler getInstance(Context context) {
        if (instance == null) {
            instance = new DBhandler(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + DEVICES_TABLE_NAME + " ("
                + DEVICE_ID_COL + " INTEGER, "
                + DEVICE_NAME_COL + " TEXT,"
                + DEVICE_ROOM_COL + " TEXT,"
                + DEVICE_TYPE_COL + " TEXT,"
                + DEVICE_TOPIC_COL + " TEXT)";
        db.execSQL(query);

        query = "CREATE TABLE " + ROOMS_TABLE_NAME + " ("
                + ROOM_ID_COL + " INTEGER PRIMARY KEY, "
                + ROOM_NAME_COL + " TEXT,"
                + ROOM_TOPIC_COL + " TEXT)";
        db.execSQL(query);

        query = "CREATE TABLE " + TYPES_TABLE_NAME + " ("
                + TYPE_ID_COL + " INTEGER PRIMARY KEY, "
                + TYPE_NAME_COL + " TEXT)";
        db.execSQL(query);

        query = "CREATE TABLE " + USERS_TABLE_NAME + " ("
                + USER_ID_COL + " INTEGER PRIMARY KEY, "
                + USER_NAME_COL + " TEXT,"
                + USER_PASSWORD_COL + " TEXT,"
                + USER_LEVEL_COL + " TEXT)";
        db.execSQL(query);
    }

    public void addDevice(String deviceID, String deviceName, String deviceRoom,
                          String deviceType, String deviceTopic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DEVICE_ID_COL, deviceID);
        values.put(DEVICE_NAME_COL, deviceName);
        values.put(DEVICE_ROOM_COL, deviceRoom);
        values.put(DEVICE_TYPE_COL, deviceType);
        values.put(DEVICE_TOPIC_COL, deviceTopic);

        db.insert(DEVICES_TABLE_NAME, null, values);
        db.close();
    }

    public void updateDevice(String deviceID, String deviceName, String deviceRoom,
                             String deviceType, String deviceTopic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DEVICE_NAME_COL, deviceName);
        values.put(DEVICE_ROOM_COL, deviceRoom);
        values.put(DEVICE_TYPE_COL, deviceType);
        values.put(DEVICE_TOPIC_COL, deviceTopic);

        String whereClause = DEVICE_ID_COL + " = ?";
        String[] whereArgs = new String[]{deviceID};
        db.update(DEVICES_TABLE_NAME, values, whereClause, whereArgs);

        db.close();
    }

    public void deleteDevice(String deviceID) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = DEVICE_ID_COL + "=?";
        String[] selectionArgs = {deviceID};

        db.delete(DEVICES_TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    public void addRoom(String roomName, String roomTopic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(ROOM_NAME_COL, roomName);
        values.put(ROOM_TOPIC_COL, roomTopic);
        db.insert(ROOMS_TABLE_NAME, null, values);
        db.close();
    }

    public void deleteRoom(String roomName) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = ROOM_NAME_COL + "=?";
        String[] selectionArgs = {roomName};

        db.delete(ROOMS_TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    public void addNewType(String typeName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TYPE_NAME_COL, typeName);
        db.insert(TYPES_TABLE_NAME, null, values);
        db.close();
    }

    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(DEVICES_TABLE_NAME, null, null);
        db.delete(ROOMS_TABLE_NAME, null, null);
        db.delete(TYPES_TABLE_NAME, null, null);
        db.delete(USERS_TABLE_NAME, null, null);
    }

    public void updateDatabase(String databaseString) {
        clearDatabase();

        databaseString = databaseString.replace("\"", "");
        databaseString = databaseString.replace("\\", "");
        databaseString = databaseString.replace(" ", "");
        databaseString = databaseString.replaceAll("\\[]", "[[]]");

        String[] tables = databaseString.split("]],\\[\\[");
        Log.d("DEBUG_DATABASE", databaseString);

        for (int i = 0; i < tables.length; i++) {
            String[] rows = tables[i].split("],\\[");

            if (rows.length > 0) {
                for (int j = 0; j < rows.length; j++) {
                    rows[j] = rows[j].replace("[", "");
                    rows[j] = rows[j].replace("]", "");

                    String[] fields = rows[j].split(",");

                    if (fields.length > 1) {
                        for (int k = 0; k < fields.length; k++) fields[k] = fields[k].replace("\"", "");

                        if (i == 0) addDevice(fields[0], fields[1], fields[2], fields[3], fields[4]);
                        else if (i == 1) addRoom(fields[1], fields[2]);
                        else if (i == 2) addNewType(fields[1]);
                    }
                }
            }
        }
    }

    public List<String> getRoomsList() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT " + ROOM_NAME_COL + " from " + ROOMS_TABLE_NAME, null);
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
        Cursor cursor = db.rawQuery("SELECT " + DEVICE_NAME_COL + " from " + DEVICES_TABLE_NAME
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
                + TYPES_TABLE_NAME, null);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(TYPE_NAME_COL));
            list.add(type);
        }
        cursor.close();
        return list;
    }

    public String getType(String deviceName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DEVICE_TYPE_COL + " from " + DEVICES_TABLE_NAME
                + " WHERE " + DEVICE_NAME_COL + "='" + deviceName + "'", null);
        cursor.moveToFirst();
        @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(DEVICE_TYPE_COL));
        cursor.close();
        return type;
    }

    public String getID(String deviceName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DEVICE_ID_COL + " from "
                + DEVICES_TABLE_NAME + " WHERE " + DEVICE_NAME_COL + "='" + deviceName
                + "'", null);
        cursor.moveToFirst();
        @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(DEVICE_ID_COL));
        cursor.close();
        return id;
    }

    public String getDeviceTopic(String deviceID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DEVICE_TOPIC_COL + " from "
                + DEVICES_TABLE_NAME + " WHERE " + DEVICE_ID_COL + "='" + deviceID
                + "'", null);
        cursor.moveToFirst();
        @SuppressLint("Range") String topic = cursor.getString(cursor.getColumnIndex(DEVICE_TOPIC_COL));
        cursor.close();
        return topic;
    }

    public String getRoomTopic(String roomName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + ROOM_TOPIC_COL + " from "
                + ROOMS_TABLE_NAME + " WHERE " + ROOM_NAME_COL + "='" + roomName
                + "'", null);
        cursor.moveToFirst();
        @SuppressLint("Range") String topic = cursor.getString(cursor.getColumnIndex(ROOM_TOPIC_COL));
        cursor.close();
        return topic;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DEVICES_TABLE_NAME);
        onCreate(db);
    }
}
