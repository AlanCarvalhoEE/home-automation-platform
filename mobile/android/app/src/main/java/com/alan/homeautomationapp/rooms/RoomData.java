package com.alan.homeautomationapp.rooms;

// Class responsible for storing rooms data
public class RoomData {
    private String id;
    private String name;

    public RoomData(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Method to get the room ID
    public String getId() { return id; }

    // Method to get the room name
    public String getName() {
        return name;
    }

    // Method to set the room name
    public void setName(String name) {
        this.name = name;
    }
}