package com.alan.homeautomationapp.devices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.alan.homeautomationapp.core.DatabaseManager;
import com.alan.homeautomationapp.core.DialogManager;
import com.alan.homeautomationapp.firmware.FirmwareData;
import com.alan.homeautomationapp.firmware.FirmwareManager;
import com.alan.homeautomationapp.core.MQTTclient;
import com.alan.homeautomationapp.ui.MainScreenRenderer;
import com.alan.homeautomationapp.R;
import com.alan.homeautomationapp.rooms.RoomManager;

import org.json.JSONException;

import java.io.IOException;
import java.util.Set;

// Class responsible for running the devices fragment
public class DevicesFragment extends Fragment {

    private DatabaseManager databaseManager;
    private DeviceManager.DeviceUpdateListener listener;
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
                    DeviceManager.getInstance(requireContext()).getAllDeviceIds()
            );

                DialogManager.openDiscoveredDevicesDialog(requireActivity(), discoveryManager,
                        discoveredDevice -> DialogManager.openAddDeviceDialog(requireActivity(),
                                discoveredDevice, RoomManager.getInstance(requireContext()).getAllRoomNames(),
                                deviceData -> {

                                    databaseManager.addDevice(deviceData.getId(), deviceData.getName(),
                                            deviceData.getRoom(), deviceData.getType(),
                                            deviceData.getFunction(), deviceData.getTopic());

                                    DeviceManager.getInstance(requireContext()).addDevice(deviceData, true);

                                    String payload = deviceData.getId() + "," + deviceData.getName() +
                                            "," + deviceData.getRoom() + "," + deviceData.getType() +
                                            "," + deviceData.getFunction() + "," + deviceData.getTopic();
                                    mqttClient.publish("hap/main/database/add_device", payload);

                                    refreshDevices();
                                }
                        )
                );}
        );

        refreshDevices();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listener = device ->
                requireActivity().runOnUiThread(this::refreshDevices);

        DeviceManager.getInstance(requireContext()).addListener(listener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        DeviceManager.getInstance(requireContext()).removeListener(listener);
    }

    // Method to refresh devices on screen
    private void refreshDevices() {

        devicesLayout.removeAllViews();
        Set<String> devicesList = DeviceManager.getInstance(requireContext()).getAllDeviceIds();

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (String deviceID : devicesList) {

            DeviceData device = DeviceManager.getInstance(requireContext()).getDevice(deviceID);

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

            FirmwareData firmwareData;
            try {
                firmwareData = FirmwareManager.getFirmwareData(deviceType);
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
            String latestFirmwareVersion = firmwareData.version;

            //Configure button listener
            deviceConfigImageButton.setOnClickListener(v -> DialogManager.openConfigureDeviceDialog(
                            requireActivity(), device, RoomManager.getInstance(requireContext()).getAllRoomNames(),
                            latestFirmwareVersion,

                            deviceData -> {
                                databaseManager.configureDevice(deviceData.getId(), deviceData.getName(),
                                        deviceData.getRoom(), deviceData.getType(),
                                        deviceData.getFunction(), deviceData.getTopic());

                                DeviceManager.getInstance(requireContext()).configureDevice(deviceID,
                                        deviceData.getName(), deviceData.getRoom(), true);

                                String payload = deviceData.getId() + "," + deviceData.getName() +
                                        "," + deviceData.getRoom() + "," + deviceData.getType() +
                                        "," + deviceData.getTopic();
                                mqttClient.publish("hap/main/database/update_device", payload);

                                refreshDevices();
                            },

                    deviceData -> {
                        String payload = "{\"version\":\"" + firmwareData.version + "\"}";
                        mqttClient.publish(
                                "hap/device/" + deviceData.getId() + "/update", payload);
                    }));

            // Delete button listener
            deviceDeleteImageButton.setOnClickListener(v ->
                    DialogManager.openDeleteDeviceDialog(requireActivity(), deviceID,
                            deviceData -> {

                                databaseManager.deleteDevice(deviceID);

                                DeviceManager.getInstance(requireContext()).deleteDevice(deviceID, true);

                                mqttClient.publish("hap/main/database/delete_device", deviceID);

                                refreshDevices();
                            }));

            devicesLayout.addView(deviceView);

            boolean online = "ONLINE".equals(device.getStatus());
            MainScreenRenderer.setViewStatus(deviceView, online, true);
        }
    }
}
