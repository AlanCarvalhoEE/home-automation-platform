package com.alan.homeautomationapp;

// Class responsible for storing firmware data
public class FirmwareData {

    public final String device;
    public final String version;
    public final String url;

    public FirmwareData(String device, String version, String url) {
        this.device = device;
        this.version = version;
        this.url = url;
    }
}
