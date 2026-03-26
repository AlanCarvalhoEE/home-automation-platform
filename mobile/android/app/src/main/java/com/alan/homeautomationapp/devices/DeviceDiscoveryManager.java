package com.alan.homeautomationapp.devices;

import com.alan.homeautomationapp.core.MQTTclient;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

// Class responsible for managing discovered devices
public class DeviceDiscoveryManager {

    private final Map<String, DiscoveredDevice> discoveredDevices = new HashMap<>();
    private final Set<String> registeredDeviceIds = Collections.synchronizedSet(new HashSet<>());

    private final MQTTclient mqttClient = MQTTclient.getInstance();

    private MQTTclient.MqttMessageCallback discoveryCallback;
    private boolean discovering = false;

    // Discovery listener
    public interface DiscoveryListener {
        void onDeviceFound(DiscoveredDevice device);
    }

    // Method to start the discovery process
    public void startDiscovery(DiscoveryListener listener) {

        if (discovering) return;
        discoveredDevices.clear();
        discovering = true;

        discoveryCallback = (topic, message) -> {
            if (!discovering) return;
            try {
                JSONObject json = new JSONObject(message);

                String id = json.getString("id");
                String type = json.getString("type");

                if (registeredDeviceIds.contains(id)) return;

                synchronized (discoveredDevices) {
                    if (discoveredDevices.containsKey(id)) {
                        Objects.requireNonNull(discoveredDevices.get(id)).updateLastSeen();
                        return;
                    }

                    DiscoveredDevice device = new DiscoveredDevice(id, type);
                    discoveredDevices.put(id, device);

                    if (listener != null) {
                        listener.onDeviceFound(device);
                    }
                }
            } catch (Exception ignored) {}
        };
        mqttClient.subscribeDiscovery(discoveryCallback);
    }

    // Method to stop the discovery process
    public void stopDiscovery() {

        if (!discovering) return;
        discovering = false;

        if (discoveryCallback != null) {
            mqttClient.unsubscribeDiscovery(discoveryCallback);
            discoveryCallback = null;
        }
    }

    // Method to register the discovered devices
    public void setRegisteredDevices(Set<String> deviceIds) {
        synchronized (registeredDeviceIds) {
            registeredDeviceIds.clear();
            registeredDeviceIds.addAll(deviceIds);
        }
    }
}