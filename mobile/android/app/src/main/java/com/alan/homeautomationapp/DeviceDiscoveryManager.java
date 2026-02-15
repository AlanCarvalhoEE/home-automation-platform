package com.alan.homeautomationapp;

import org.json.JSONObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DeviceDiscoveryManager {

    private final Map<String, DiscoveredDevice> devices = new HashMap<>();

    public interface DiscoveryListener {
        void onDeviceFound(DiscoveredDevice device);
    }

    public void startDiscovery(DiscoveryListener listener) {

        MQTTclient.getInstance().subscribeDiscovery((topic, message) -> {
            try {
                JSONObject json = new JSONObject(message);

                String id = json.getString("id");
                String type = json.getString("type");

                boolean isNew = false;

                if (!devices.containsKey(id)) {
                    devices.put(id, new DiscoveredDevice(id, type));
                    isNew = true;
                } else {
                    devices.get(id).updateLastSeen();
                }

                if (isNew && listener != null) {
                    listener.onDeviceFound(devices.get(id));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Collection<DiscoveredDevice> getDevices() {
        return devices.values();
    }

    public void clear() {
        devices.clear();
    }
}
