package com.alan.homeautomationapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Class responsible for managing devices
public class DeviceManager {

    private static DeviceManager instance;
    private final List<DeviceUpdateListener> listeners = new ArrayList<>();
    private final Map<String, DeviceData> devicesMap = new HashMap<>();

    // DeviceManager constructor
    private DeviceManager() {}

    // DeviceManager singleton
    public static synchronized DeviceManager getInstance() {
        if (instance == null) {
            instance = new DeviceManager();
        }
        return instance;
    }

    // Method to add a device (DeviceData)
    public void addDevice(DeviceData device) {
        devicesMap.put(device.getId(), device);
    }

    // Method to configure a device
    public void configureDevice(String deviceId, String newName, String newRoom) {
        DeviceData device = devicesMap.get(deviceId);
        if (device != null) {
            device.setName(newName);
            device.setRoom(newRoom);
        }
    }

    // Method to delete a device (DeviceData)
    public void deleteDevice(String id) {
        devicesMap.remove(id);
    }

    // Method to get a device (DeviceData)
    public DeviceData getDevice(String id) {
        return devicesMap.get(id);
    }

    // Method to get all devices (DeviceData)
    public Collection<DeviceData> getAllDevices() {
        return devicesMap.values();
    }

    // Method to get the ID of all devices (String)
    public Set<String> getAllDeviceIds() {
        return new HashSet<>(devicesMap.keySet());
    }

    // Method to listen to device updates
    public interface DeviceUpdateListener {
        void onDeviceUpdated(DeviceData device);
    }

    // Method to add a DeviceManager listener
    public void addListener(DeviceUpdateListener listener) {
        listeners.add(listener);
    }

    // Method to remove a DeviceManager listener
    public void removeListener(DeviceUpdateListener listener) {
        listeners.remove(listener);
    }

    // Method to notify device updates
    public void notifyDeviceUpdated(DeviceData device) {
        for (DeviceUpdateListener l : listeners) {
            l.onDeviceUpdated(device);
        }
    }
}