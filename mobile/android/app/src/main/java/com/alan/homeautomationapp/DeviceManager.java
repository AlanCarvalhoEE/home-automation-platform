package com.alan.homeautomationapp;

import android.content.Context;

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
    private final Context context;
    private final List<DeviceUpdateListener> listeners = new ArrayList<>();
    private final Map<String, DeviceData> devicesMap = new HashMap<>();

    // DeviceManager constructor
    private DeviceManager(Context context) {
        this.context = context.getApplicationContext();
    }

    // DeviceManager singleton
    public static synchronized DeviceManager getInstance(Context context) {
        if (instance == null) {
            instance = new DeviceManager(context);
        }
        return instance;
    }

    // Method to add a device (DeviceData)
    public void addDevice(DeviceData device, boolean log) {

        devicesMap.put(device.getId(), device);

        if (log) {
            String message = context.getString(R.string.log_device_message) +
                    device.getName() + "(" + device.getId() + ")" +
                    context.getString(R.string.log_device_add_message) + device.getRoom() + ".";
            DatabaseManager.getInstance(context).logEvent("DEVICE_ADD", message);
        }
    }

    // Method to configure a device
    public void configureDevice(String deviceId, String newName, String newRoom, boolean log) {
        DeviceData device = devicesMap.get(deviceId);
        if (device != null) {
            device.setName(newName);
            device.setRoom(newRoom);
        }

        if (log) {
            String message = context.getString(R.string.log_device_message) + device.getId() +
                    context.getString(R.string.log_configure_message) + device.getName() +
                    context.getString(R.string.log_device_room_message) + device.getRoom() + ".";
            DatabaseManager.getInstance(context).logEvent("DEVICE_CONFIGURE", message);
        }
    }

    // Method to delete a device (DeviceData)
    public void deleteDevice(String id, boolean log) {

        DeviceData device = devicesMap.get(id);
        devicesMap.remove(id);

        if (log) {
            String message = context.getString(R.string.log_device_message) + device.getName() +
                    "(" + device.getId() + ")" + context.getString(R.string.log_delete_message) +".";
            DatabaseManager.getInstance(context).logEvent("DEVICE_DELETE", message);
        }
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