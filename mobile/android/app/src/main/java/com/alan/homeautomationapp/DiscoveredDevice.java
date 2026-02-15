package com.alan.homeautomationapp;

public class DiscoveredDevice {

    public final String id;
    public final String type;
    public long lastSeen;

    public DiscoveredDevice(String id, String type) {
        this.id = id;
        this.type = type;
        this.lastSeen = System.currentTimeMillis();
    }

    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }
}
