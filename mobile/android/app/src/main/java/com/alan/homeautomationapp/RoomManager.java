package com.alan.homeautomationapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Class responsible for managing rooms
public class RoomManager {

    private static RoomManager instance;
    private final Map<String, RoomData> roomsMap = new HashMap<>();

    // RoomManager constructor
    private RoomManager() {}

    // RoomManager singleton
    public static synchronized RoomManager getInstance() {
        if (instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }

    // Method to add a room
    public void addRoom(RoomData room) {
        roomsMap.put(room.getId(), room);
    }

    // Method to delete a room
    public void deleteRoom(String roomId) {
        roomsMap.remove(roomId);
    }

    // Method to configure a room
    public void configureRoom(String roomId, String newName) {
        RoomData room = roomsMap.get(roomId);
        if (room != null) room.setName(newName);
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