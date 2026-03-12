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

// Class responsible for managing the database
public class DatabaseManager extends SQLiteOpenHelper {

    // Database information
    private static final String DB_NAME = "HOME_AUTOMATION_DB";
    private static final int DB_VERSION = 1;

    // Database "Devices" table definition
    private static final String DEVICES_TABLE_NAME = "Devices";
    private static final String DEVICE_ID_COL = "ID";
    private static final String DEVICE_NAME_COL = "Name";
    private static final String DEVICE_ROOM_COL = "Room";
    private static final String DEVICE_TYPE_COL = "Type";
    private static final String DEVICE_TOPIC_COL = "Topic";

    // Database "Rooms" table definition
    private static final String ROOMS_TABLE_NAME = "Rooms";
    private static final String ROOM_ID_COL = "ID";
    private static final String ROOM_NAME_COL = "Room";

    // Database "Types" table definition
    private static final String TYPES_TABLE_NAME = "Types";
    private static final String TYPE_ID_COL = "ID";
    private static final String TYPE_NAME_COL = "Type";

    // Database "Users" table definition
    private static final String USERS_TABLE_NAME = "Users";
    private static final String USER_ID_COL = "ID";
    private static final String USER_NAME_COL = "User";
    private static final String USER_PASSWORD_COL = "Password";
    private static final String USER_LEVEL_COL = "Level";

    private static DatabaseManager instance;    // DatabaseManager instance

    // DatabaseManager constructor
    private DatabaseManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // DatabaseManager singleton
    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create "Devices" table
        String query = "CREATE TABLE " + DEVICES_TABLE_NAME + " ("
                + DEVICE_ID_COL + " TEXT PRIMARY KEY, "
                + DEVICE_NAME_COL + " TEXT,"
                + DEVICE_ROOM_COL + " TEXT,"
                + DEVICE_TYPE_COL + " TEXT,"
                + DEVICE_TOPIC_COL + " TEXT)";
        db.execSQL(query);

        // Create "Rooms" table
        query = "CREATE TABLE " + ROOMS_TABLE_NAME + " ("
                + ROOM_ID_COL + " TEXT PRIMARY KEY, "
                + ROOM_NAME_COL + " TEXT)";
        db.execSQL(query);

        // Create "Types" table
        query = "CREATE TABLE " + TYPES_TABLE_NAME + " ("
                + TYPE_ID_COL + " INTEGER PRIMARY KEY, "
                + TYPE_NAME_COL + " TEXT)";
        db.execSQL(query);

        // Create "Users" table
        query = "CREATE TABLE " + USERS_TABLE_NAME + " ("
                + USER_ID_COL + " INTEGER PRIMARY KEY, "
                + USER_NAME_COL + " TEXT,"
                + USER_PASSWORD_COL + " TEXT,"
                + USER_LEVEL_COL + " TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DEVICES_TABLE_NAME);
        onCreate(db);
    }

    // Method to clear the whole database
    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(DEVICES_TABLE_NAME, null, null);
        db.delete(ROOMS_TABLE_NAME, null, null);
        db.delete(TYPES_TABLE_NAME, null, null);
        db.delete(USERS_TABLE_NAME, null, null);
    }

    // Method to update the database from MQTT
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
                        else if (i == 1) addRoom(fields[0], fields[1]);
                        else if (i == 2) addType(fields[1]);
                    }
                }
            }
        }
    }

    // Method to add a room to the database
    public void addRoom(String roomID, String roomName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(ROOM_ID_COL, roomID);
        values.put(ROOM_NAME_COL, roomName);
        db.insert(ROOMS_TABLE_NAME, null, values);
        db.close();
    }

    // Method to configure a room on the database
    public void configureRoom(String roomID, String newRoomName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ROOM_NAME_COL, newRoomName);

        String whereClause = ROOM_ID_COL + " = ?";
        String[] whereArgs = new String[]{roomID};

        db.update(ROOMS_TABLE_NAME, values, whereClause, whereArgs);
        db.close();
    }

    // Method to delete a room from the database
    public void deleteRoom(String roomID) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = ROOM_ID_COL + "=?";
        String[] selectionArgs = {roomID};

        db.delete(ROOMS_TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    // Method to add a device to the database
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

    // Method to update a device on the database
    public void configureDevice(String deviceID, String deviceName, String deviceRoom,
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

    // Method to delete a device from the database
    public void deleteDevice(String deviceID) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = DEVICE_ID_COL + "=?";
        String[] selectionArgs = {deviceID};

        db.delete(DEVICES_TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    // Method to add a type to the database
    public void addType(String typeName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TYPE_NAME_COL, typeName);
        db.insert(TYPES_TABLE_NAME, null, values);
        db.close();
    }

    // Method to get the list of rooms from the database
    public List<RoomData> getAllRooms() {

        SQLiteDatabase db = this.getReadableDatabase();
        List<RoomData> rooms = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + ROOMS_TABLE_NAME,
                null
        );

        int idIndex = cursor.getColumnIndexOrThrow(ROOM_ID_COL);
        int nameIndex = cursor.getColumnIndexOrThrow(ROOM_NAME_COL);

        while (cursor.moveToNext()) {

            String id = cursor.getString(idIndex);
            String name = cursor.getString(nameIndex);

            rooms.add(new RoomData(id, name));
        }

        cursor.close();
        return rooms;
    }

    // Method to get the list of devices from the database
    public List<DeviceData> getAllDevices() {

        SQLiteDatabase db = this.getReadableDatabase();
        List<DeviceData> devices = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + DEVICES_TABLE_NAME,
                null
        );

        while (cursor.moveToNext()) {

            @SuppressLint("Range")
            String id = cursor.getString(cursor.getColumnIndex(DEVICE_ID_COL));
            @SuppressLint("Range")
            String name = cursor.getString(cursor.getColumnIndex(DEVICE_NAME_COL));
            @SuppressLint("Range")
            String room = cursor.getString(cursor.getColumnIndex(DEVICE_ROOM_COL));
            @SuppressLint("Range")
            String type = cursor.getString(cursor.getColumnIndex(DEVICE_TYPE_COL));
            @SuppressLint("Range")
            String topic = cursor.getString(cursor.getColumnIndex(DEVICE_TOPIC_COL));

            devices.add(new DeviceData(id, name, room, type, topic));
        }

        cursor.close();
        return devices;
    }
}
