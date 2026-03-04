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

import java.util.Set;

// Class responsible for running the devices fragment
public class DevicesFragment extends Fragment {

    private DatabaseManager databaseManager;
    private MQTTclient mqttClient;
    private DeviceDiscoveryManager discoveryManager;
    private LinearLayout devicesLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        databaseManager = DatabaseManager.getInstance(requireContext());
        mqttClient = MQTTclient.getInstance();
        discoveryManager = new DeviceDiscoveryManager();

        devicesLayout = view.findViewById(R.id.devicesLayout);

        ImageButton deviceSearchImageButton =
                view.findViewById(R.id.deviceSearchImageButton);

        // Search button listener
        deviceSearchImageButton.setOnClickListener(v -> {
            discoveryManager.setRegisteredDevices(
                    DeviceManager.getInstance().getAllDeviceIds()
            );

                DialogManager.openDiscoveredDevicesDialog(requireActivity(), discoveryManager,
                        discoveredDevice -> DialogManager.openAddDeviceDialog(requireActivity(),
                                discoveredDevice, RoomManager.getInstance().getAllRoomNames(),
                                deviceData -> {

                                    databaseManager.addDevice(deviceData.getId(), deviceData.getName(),
                                            deviceData.getRoom(), deviceData.getType(), deviceData.getTopic());

                                    DeviceManager.getInstance().addDevice(deviceData);

                                    String payload = deviceData.getId() + "," + deviceData.getName() +
                                            "," + deviceData.getRoom() + "," + deviceData.getType() +
                                            "," + deviceData.getTopic();
                                    mqttClient.publish("hap/main/database/add_device", payload);

                                    refreshDevices();
                                }
                        )
                );}
        );

        refreshDevices();
        return view;
    }

    // Method to refresh devices on screen
    private void refreshDevices() {

        devicesLayout.removeAllViews();
        Set<String> devicesList = DeviceManager.getInstance().getAllDeviceIds();

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (String deviceID : devicesList) {

            DeviceData device = DeviceManager.getInstance().getDevice(deviceID);

            String deviceName = device.getName();
            String deviceType = device.getType();
            String deviceRoom = device.getRoom();

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

            //Configure button listener
            deviceConfigImageButton.setOnClickListener(v -> DialogManager.openConfigureDeviceDialog(
                            requireActivity(), device, RoomManager.getInstance().getAllRoomNames(),
                            deviceData -> {

                                databaseManager.configureDevice(deviceData.getId(), deviceData.getName(),
                                        deviceData.getRoom(), deviceData.getType(), deviceData.getTopic());

                                DeviceManager.getInstance().configureDevice(deviceID,
                                        deviceData.getName(), deviceData.getRoom());

                                String payload = deviceData.getId() + "," + deviceData.getName() +
                                        "," + deviceData.getRoom() + "," + deviceData.getType() +
                                        "," + deviceData.getTopic();
                                mqttClient.publish("hap/main/database/update_device", payload);

                                refreshDevices();
                            }));

            // Delete button listener
            deviceDeleteImageButton.setOnClickListener(v ->
                    DialogManager.openDeleteDeviceDialog(requireActivity(), deviceID,
                            deviceData -> {

                                databaseManager.deleteDevice(deviceID);

                                DeviceManager.getInstance().deleteDevice(deviceID);

                                mqttClient.publish("hap/main/database/delete_device", deviceID);

                                refreshDevices();
                            }));

            devicesLayout.addView(deviceView);
        }
    }
}
