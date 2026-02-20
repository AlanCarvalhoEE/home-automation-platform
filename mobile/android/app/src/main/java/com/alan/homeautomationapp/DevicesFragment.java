package com.alan.homeautomationapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.HashSet;
import java.util.List;

public class DevicesFragment extends Fragment {

    private DBhandler dbHandler;
    private MQTTclient mqttClient;
    private LinearLayout devicesLayout;
    private DeviceDiscoveryManager discoveryManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        dbHandler = DBhandler.getInstance(requireContext());
        mqttClient = MQTTclient.getInstance();
        discoveryManager = new DeviceDiscoveryManager();

        devicesLayout = view.findViewById(R.id.devicesLayout);

        ImageButton deviceSearchImageButton =
                view.findViewById(R.id.deviceSearchImageButton);

        deviceSearchImageButton.setOnClickListener(v -> {
                    discoveryManager.setRegisteredDevices(
                            new HashSet<>(dbHandler.getDeviceIdsList())
                    );

                DialogManager.openDiscoveredDevicesDialog(
                        requireActivity(),
                        discoveryManager,
                        discoveredDevice -> DialogManager.openAddDeviceDialog(requireActivity(),
                                discoveredDevice, dbHandler.getRoomsList(),
                                deviceData -> {
                                    dbHandler.addDevice(deviceData.id, deviceData.name,
                                            deviceData.room, deviceData.type, deviceData.topic);
                                    String payload = deviceData.id + "," + deviceData.name + "," +
                                            deviceData.room + "," + deviceData.type + "," +
                                            deviceData.topic;
                                    mqttClient.publish("hap/main/database/add_device", payload);
                                    refreshDevices();
                                }
                        )
                );}
        );

        refreshDevices();

        return view;
    }

    private void refreshDevices() {

        devicesLayout.removeAllViews();

        List<String> devicesList = dbHandler.getDevicesList();

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (String deviceID : devicesList) {

            String deviceName = dbHandler.getDeviceName(deviceID);
            String deviceType = dbHandler.getDeviceType(deviceID);
            String deviceRoom = dbHandler.getDeviceRoom(deviceID);

            View deviceView = inflater.inflate(R.layout.device_info, devicesLayout, false);

            TextView deviceNameTextView = deviceView.findViewById(R.id.deviceNameTextView);
            TextView deviceIdTextView = deviceView.findViewById(R.id.deviceIdTextView);
            TextView deviceRoomTextView = deviceView.findViewById(R.id.deviceRoomTextView);
            TextView deviceTypeTextView = deviceView.findViewById(R.id.deviceTypeTextView);
            ImageButton deviceConfigImageButton = deviceView.findViewById(R.id.deviceConfigImageButton);
            ImageButton deviceDeleteImageButton = deviceView.findViewById(R.id.deviceDeleteImageButton);

            deviceNameTextView.setText(deviceName);
            deviceIdTextView.setText(deviceID);
            deviceTypeTextView.setText(deviceType);
            deviceRoomTextView.setText(deviceRoom);

            deviceConfigImageButton.setOnClickListener(v ->
                    DialogManager.openUpdateDeviceDialog(
                            requireActivity(), deviceID, deviceType, dbHandler.getRoomsList(),
                            deviceData -> {
                                dbHandler.updateDevice(deviceData.id, deviceData.name,
                                        deviceData.room, deviceData.type, deviceData.topic);
                                String payload = deviceData.id + "," + deviceData.name + "," +
                                        deviceData.room + "," + deviceData.type + "," + deviceData.topic;
                                mqttClient.publish("hap/main/database/update_device", payload);
                                refreshDevices();
                            }));

            deviceDeleteImageButton.setOnClickListener(v ->
                    DialogManager.openDeleteDeviceDialog(
                            requireActivity(), deviceID, deviceData -> {
                                dbHandler.deleteDevice(deviceID);

                                mqttClient.publish("hap/main/database/delete_device", deviceID);
                                refreshDevices();
                            }));

            devicesLayout.addView(deviceView);
        }
    }
}
