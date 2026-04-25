package com.hap.homeautomation.core;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.hap.homeautomation.devices.DeviceData;
import com.hap.homeautomation.log.LogData;
import com.hap.homeautomation.log.LogType;
import com.hap.homeautomation.rooms.RoomData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// Class responsible for managing the database
public class DatabaseManager extends SQLiteOpenHelper {

    // Database information
    private static final String DB_NAME = "HOME_AUTOMATION_DB";
    private static final int DB_VERSION = 2;

    // Database "Devices" table definition
    private static final String DEVICES_TABLE_NAME = "Devices";
    private static final String DEVICE_ID_COL = "ID";
    private static final String DEVICE_NAME_COL = "Name";
    private static final String DEVICE_ROOM_COL = "Room";
    private static final String DEVICE_TYPE_COL = "Type";
    private static final String DEVICE_FUNCTION_COL = "Function";
    private static final String DEVICE_TOPIC_COL = "Topic";

    // Database "Rooms" table definition
    private static final String ROOMS_TABLE_NAME = "Rooms";
    private static final String ROOM_ID_COL = "ID";
    private static final String ROOM_NAME_COL = "Name";

    // Database "Users" table definition
    private static final String USERS_TABLE_NAME = "Users";
    private static final String USER_ID_COL = "ID";
    private static final String USER_NAME_COL = "User";
    private static final String USER_PASSWORD_COL = "Password";
    private static final String USER_LEVEL_COL = "Level";

    // Database "Log" table definition
    private static final String LOG_TABLE_NAME = "Log";
    private static final String LOG_ID_COL = "ID";
    private static final String LOG_TIMESTAMP_COL = "Timestamp";
    private static final String LOG_TYPE_COL = "Type";
    private static final String LOG_MESSAGE_COL = "Message";

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
                + DEVICE_FUNCTION_COL + " TEXT,"
                + DEVICE_TOPIC_COL + " TEXT)";
        db.execSQL(query);

        // Create "Rooms" table
        query = "CREATE TABLE " + ROOMS_TABLE_NAME + " ("
                + ROOM_ID_COL + " TEXT PRIMARY KEY, "
                + ROOM_NAME_COL + " TEXT)";
        db.execSQL(query);

        // Create "Users" table
        query = "CREATE TABLE " + USERS_TABLE_NAME + " ("
                + USER_ID_COL + " INTEGER PRIMARY KEY, "
                + USER_NAME_COL + " TEXT,"
                + USER_PASSWORD_COL + " TEXT,"
                + USER_LEVEL_COL + " TEXT)";
        db.execSQL(query);

        // Create "Log" table
        query = "CREATE TABLE " + LOG_TABLE_NAME + " ("
                + LOG_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LOG_TIMESTAMP_COL + " INTEGER,"
                + LOG_TYPE_COL + " TEXT,"
                + LOG_MESSAGE_COL + " TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + DEVICES_TABLE_NAME + " ADD COLUMN " +
                    DEVICE_FUNCTION_COL + " TEXT");
        }
    }

    // Method to clear the whole database
    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(DEVICES_TABLE_NAME, null, null);
        db.delete(ROOMS_TABLE_NAME, null, null);
        db.delete(USERS_TABLE_NAME, null, null);
    }

    // Method to update the database from MQTT
    public void updateDatabase(String databaseString) {

        clearDatabase();

        try {
            JSONObject dbJson = new JSONObject(databaseString);

            if (dbJson.has("Devices")) {
                JSONArray devices = dbJson.getJSONArray("Devices");

                for (int i = 0; i < devices.length(); i++) {
                    JSONArray row = devices.getJSONArray(i);

                    String id = row.getString(0);
                    String name = row.getString(1);
                    String room = row.getString(2);
                    String type = row.getString(3);
                    String function = row.getString(4);
                    String topic = row.getString(5);

                    addDevice(id, name, room, type, function, topic);
                }
            }

            if (dbJson.has("Rooms")) {
                JSONArray rooms = dbJson.getJSONArray("Rooms");

                for (int i = 0; i < rooms.length(); i++) {
                    JSONArray row = rooms.getJSONArray(i);

                    String id = row.getString(0);
                    String name = row.getString(1);

                    addRoom(id, name);
                }
            }

            if (dbJson.has("Users")) {
                JSONArray users = dbJson.getJSONArray("Users");

                for (int i = 0; i < users.length(); i++) {
                    JSONArray row = users.getJSONArray(i);

                    ContentValues values = new ContentValues();
                    values.put(USER_ID_COL, row.getInt(0));
                    values.put(USER_NAME_COL, row.getString(1));
                    values.put(USER_PASSWORD_COL, row.getString(2));
                    values.put(USER_LEVEL_COL, row.getString(3));

                    getWritableDatabase().insert(USERS_TABLE_NAME, null, values);
                }
            }

        } catch (Exception e) {
            Log.e("DB_PARSE", "Failed to parse database JSON", e);
            throw new RuntimeException("Failed to parse database JSON", e);
        }
    }

    // Method to add a room to the database
    public void addRoom(String roomID, String roomName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        System.out.println("JJJJJJJJJJJJJJJJJJ");
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
                          String deviceType, String deviceFunction, String deviceTopic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DEVICE_ID_COL, deviceID);
        values.put(DEVICE_NAME_COL, deviceName);
        values.put(DEVICE_ROOM_COL, deviceRoom);
        values.put(DEVICE_TYPE_COL, deviceType);
        values.put(DEVICE_FUNCTION_COL, deviceFunction);
        values.put(DEVICE_TOPIC_COL, deviceTopic);

        db.insert(DEVICES_TABLE_NAME, null, values);
        db.close();
    }

    // Method to update a device on the database
    public void configureDevice(String deviceID, String deviceName, String deviceRoom,
                                String deviceType, String deviceFunction, String deviceTopic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DEVICE_NAME_COL, deviceName);
        values.put(DEVICE_ROOM_COL, deviceRoom);
        values.put(DEVICE_TYPE_COL, deviceType);
        values.put(DEVICE_FUNCTION_COL, deviceFunction);
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
    @SuppressLint("Range")
    public List<DeviceData> getAllDevices() {

        SQLiteDatabase db = this.getReadableDatabase();
        List<DeviceData> devices = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + DEVICES_TABLE_NAME,
                null
        );

        while (cursor.moveToNext()) {

            String id = cursor.getString(cursor.getColumnIndex(DEVICE_ID_COL));
            String name = cursor.getString(cursor.getColumnIndex(DEVICE_NAME_COL));
            String room = cursor.getString(cursor.getColumnIndex(DEVICE_ROOM_COL));
            String type = cursor.getString(cursor.getColumnIndex(DEVICE_TYPE_COL));
            String function = cursor.getString(cursor.getColumnIndex(DEVICE_FUNCTION_COL));
            String topic = cursor.getString(cursor.getColumnIndex(DEVICE_TOPIC_COL));

            devices.add(new DeviceData(id, name, room, type, function, topic));
        }

        cursor.close();
        return devices;
    }

    // Method to add a log entry to the database
    public void logEvent(LogType type, String message) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(LOG_TIMESTAMP_COL, System.currentTimeMillis());
        values.put(LOG_TYPE_COL, type.name());
        values.put(LOG_MESSAGE_COL, message);

        db.insert(LOG_TABLE_NAME, null, values);
        db.close();
    }

    // Method to get the event logs from the database
    @SuppressLint("Range")
    public List<LogData> getEvents() {

        SQLiteDatabase db = this.getWritableDatabase();
        List<LogData> events = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + LOG_TABLE_NAME + " ORDER BY " + LOG_TIMESTAMP_COL + " DESC",
                null);

        while(cursor.moveToNext()) {

            LogData log = new LogData();

            log.timestamp = cursor.getLong(cursor.getColumnIndex(LOG_TIMESTAMP_COL));
            log.type = cursor.getString(cursor.getColumnIndex(LOG_TYPE_COL));
            log.message = cursor.getString(cursor.getColumnIndex(LOG_MESSAGE_COL));

            events.add(log);
        }

        cursor.close();
        return events;
    }
}