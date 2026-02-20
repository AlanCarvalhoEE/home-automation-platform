package com.alan.homeautomationapp;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeviceDiscoveryManager {

    private final Map<String, DiscoveredDevice> discoveredDevices = new HashMap<>();
    private final Set<String> registeredDeviceIds =
            Collections.synchronizedSet(new HashSet<>());

    private final MQTTclient mqttClient = MQTTclient.getInstance();

    private MQTTclient.MqttMessageCallback discoveryCallback;
    private boolean discovering = false;

    public interface DiscoveryListener {
        void onDeviceFound(DiscoveredDevice device);
    }

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
                        discoveredDevices.get(id).updateLastSeen();
                        return;
                    }

                    DiscoveredDevice device =
                            new DiscoveredDevice(id, type);

                    discoveredDevices.put(id, device);

                    if (listener != null) {
                        listener.onDeviceFound(device);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        mqttClient.subscribeDiscovery(discoveryCallback);
    }

    public void stopDiscovery() {

        if (!discovering) return;

        discovering = false;

        if (discoveryCallback != null) {
            mqttClient.unsubscribeDiscovery(discoveryCallback);
            discoveryCallback = null;
        }
    }

    public void setRegisteredDevices(Set<String> deviceIds) {
        synchronized (registeredDeviceIds) {
            registeredDeviceIds.clear();
            registeredDeviceIds.addAll(deviceIds);
        }
    }
}