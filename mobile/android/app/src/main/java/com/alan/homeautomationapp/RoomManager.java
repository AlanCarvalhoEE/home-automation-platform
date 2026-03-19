package com.alan.homeautomationapp;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Class responsible for managing rooms
public class RoomManager {

    private static RoomManager instance;
    private final Context context;
    private final Map<String, RoomData> roomsMap = new HashMap<>();

    // RoomManager constructor
    private RoomManager(Context context) {
        this.context = context.getApplicationContext();
    }

    // RoomManager singleton
    public static synchronized RoomManager getInstance(Context context) {
        if (instance == null) {
            instance = new RoomManager(context);
        }
        return instance;
    }

    // Method to add a room
    public void addRoom(RoomData room, boolean log) {

        roomsMap.put(room.getId(), room);

        if (log) {
            String message = context.getString(R.string.log_room_message) + room.getName() +
                    context.getString(R.string.log_room_add_message) + ".";
            DatabaseManager.getInstance(context).logEvent("ROOM_ADD", message);
        }
    }

    // Method to delete a room
    public void deleteRoom(String roomId, boolean log) {

        RoomData room = roomsMap.get(roomId);
        roomsMap.remove(roomId);

        if (log) {
            String message = context.getString(R.string.log_room_message) + room.getName() +
                    context.getString(R.string.log_delete_message) + ".";
            DatabaseManager.getInstance(context).logEvent("ROOM_DELETE", message);
        }
    }

    // Method to configure a room
    public void configureRoom(String roomId, String newName, boolean log) {

        RoomData room = roomsMap.get(roomId);
        if (room != null) room.setName(newName);

        if (log) {
            String message = context.getString(R.string.log_device_message) + room.getName() +
                    context.getString(R.string.log_configure_message) + newName + ".";
            DatabaseManager.getInstance(context).logEvent("ROOM_CONFIGURE", message);
        }
    }

    // Method to get all rooms (RoomData)
    public Collection<RoomData> getAllRooms() {
        return roomsMap.values();
    }

    // Method to get a room by its name (RoomData)
    public RoomData getRoomByName(String name) {
        for (RoomData room : roomsMap.values()) {
            if (room.getName().equals(name)) {
                return room;
            }
        }
        return null;
    }

    // Method to get all room names (String)
    public List<String> getAllRoomNames() {

        List<String> names = new ArrayList<>();

        for (RoomData room : roomsMap.values()) {
            names.add(room.getName());
        }

        return names;
    }

}