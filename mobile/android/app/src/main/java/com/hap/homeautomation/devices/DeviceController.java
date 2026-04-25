package com.hap.homeautomation.devices;

import com.hap.homeautomation.core.MQTTclient;

// Class responsible for implementing devices control
public class DeviceController {

    private static final MQTTclient mqtt = MQTTclient.getInstance();    // MQTTclient instance

    // Method to set the load status (ON or OFF)
    public static void setLoad(String deviceId, boolean on) {
        String topic = "hap/device/" + deviceId + "/set_state";
        mqtt.publish(topic, on ? "ON" : "OFF");
    }

    // Method to set the LDR status (ENABLED or DISABLED)
    public static void setLdr(String deviceId, boolean enabled) {
        String topic = "hap/device/" + deviceId + "/enable_ldr";
        mqtt.publish(topic, enabled ? "ENABLE" : "DISABLE");
    }
}