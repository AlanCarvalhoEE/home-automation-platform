package com.alan.homeautomationapp.devices;

// Class responsible for storing devices data
public class DeviceData {

    private String id;
    private String name;
    private String room;
    private String type;
    private String function;
    private String topic;

    private String firmwareVersion;
    private String status;
    private String loadStatus;
    private String ldrStatus;
    private int ldrThreshold;
    private int ldrValue;

    // DeviceData constructor
    public DeviceData(String id, String name, String room, String type,
                      String function, String topic) {
        this.id = id;
        this.name = name;
        this.room = room;
        this.type = type;
        this.function = function;
        this.topic = topic;

        this.firmwareVersion = null;
        this.status = "OFFLINE";
        this.loadStatus = null;
        this.ldrStatus = null;
        this.ldrThreshold = -1;
        this.ldrValue = -1;
    }

    // Method to get the device ID
    public String getId() { return id; }

    // Method to get the device name
    public String getName() {
        return name;
    }

    // Method to get the device room
    public String getRoom() {
        return room;
    }

    // Method to get the device type
    public String getType() {
        return type;
    }

    // Method to get the device function
    public String getFunction() {
        return function;
    }

    // Method to get the device topic
    public String getTopic() {
        return topic;
    }

    // Method to set the device name
    public void setName(String name) {
        this.name = name;
    }

    // Method to set the device room
    public void setRoom(String room) {
        this.room = room;
    }

    // Method to get the device's firmware version
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    // Method to get the device status (ONLINE or OFFLINE)
    public String getStatus() {
        return status;
    }

    // Method to get the load status (ON or OFF)
    public String getLoadStatus() {
        return loadStatus;
    }

    // Method to get the LDR status (ENABLED or DISABLED)
    public String getLdrStatus() {
        return ldrStatus;
    }

    // Method to get the LDR threshold
    public int getLdrThreshold() {
        return ldrThreshold;
    }

    // Method to get the LDR current value
    public int getLdrValue() {
        return ldrValue;
    }

    // Method to set the device's firmware version
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }

    // Method to set the load status (ON or OFF)
    public void setStatus(String status) {
        this.status = status;
    }

    // Method to set the load status (ON or OFF)
    public void setLoadStatus(String loadStatus) {
        this.loadStatus = loadStatus;
    }

    // Method to set the LDR status (ENABLED or DISABLED)
    public void setLdrStatus(String ldrStatus) {
        this.ldrStatus = ldrStatus;
    }

    // Method to set the LDR threshold
    public void setLdrThreshold(int ldrThreshold) {
        this.ldrThreshold = ldrThreshold;
    }

    // Method to set the LDR current value
    public void setLdrValue(int ldrValue) {
        this.ldrValue = ldrValue;
    }
}