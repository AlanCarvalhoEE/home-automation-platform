package com.alan.homeautomationapp;

public class DeviceData {
    public final String id;
    public final String name;
    public final String room;
    public final String type;
    public final String topic;

    public DeviceData(String id, String name, String room, String type, String topic) {
        this.id = id;
        this.name = name;
        this.room = room;
        this.type = type;
        this.topic = topic;
    }
}