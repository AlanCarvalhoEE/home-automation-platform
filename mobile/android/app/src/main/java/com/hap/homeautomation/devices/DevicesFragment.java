package com.hap.homeautomation.devices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.hap.homeautomation.core.DatabaseManager;
import com.hap.homeautomation.core.DialogManager;
import com.hap.homeautomation.firmware.FirmwareManager;
import com.hap.homeautomation.core.MQTTclient;
import com.hap.homeautomation.R;
import com.hap.homeautomation.rooms.RoomManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// Class responsible for running the devices fragment
public class DevicesFragment extends Fragment {

    private DatabaseManager databaseManager;
    private DeviceManager.DeviceUpdateListener listener;
    private MQTTclient mqttClient;
    private DeviceDiscoveryManager discoveryManager;
    private LinearLayout devicesLayout;
    private final Map<String, View> deviceViews = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        databaseManager = DatabaseManager.getInstance(requireContext());
        mqttClient = MQTTclient.getInstance();
        discoveryManager = new DeviceDiscoveryManager();

        devicesLayout = view.findViewById(R.id.devicesLayout);

        ImageButton deviceSearchImageButton = view.findViewById(R.id.deviceSearchImageButton);
        ImageButton deviceManualImageButton = view.findViewById(R.id.deviceManualImageButton);

        // Search button listener
        deviceSearchImageButton.setOnClickListener(v -> {
            discoveryManager.setRegisteredDevices(
                    DeviceManager.getInstance(requireContext()).getAllDeviceIds()
            );

                DialogManager.openDiscoveredDevicesDialog(requireActivity(), discoveryManager,
                        discoveredDevice -> DialogManager.openAddDiscoveredDeviceDialog(
                                requireActivity(), discoveredDevice,
                                RoomManager.getInstance(requireContext()).getAllRoomNames(),
                                deviceData -> {

                                    databaseManager.addDevice(
                                            deviceData.getId(), deviceData.getName(),
                                            deviceData.getRoom(), deviceData.getType(),
                                            deviceData.getFunction(), deviceData.getTopic());

                                    DeviceManager.getInstance(requireContext()).
                                            addDevice(deviceData, true);

                                    String payload = deviceData.getId() + "," +
                                            deviceData.getName() + "," + deviceData.getRoom() +
                                            "," + deviceData.getType() + "," +
                                            deviceData.getFunction() + "," + deviceData.getTopic();

                                    mqttClient.publish(
                                            "hap/main/database/add_device", payload);

                                    addDeviceView(deviceData);
                                }
                        )
                );}
        );

        // Manual insert button listener
        deviceManualImageButton.setOnClickListener(v -> {

                    DialogManager.openAddManualDeviceDialog(requireActivity(),
                            RoomManager.getInstance(requireContext()).getAllRoomNames(),
                            deviceData -> {

                                databaseManager.addDevice(
                                        deviceData.getId(), deviceData.getName(),
                                        deviceData.getRoom(), deviceData.getType(),
                                        deviceData.getFunction(), deviceData.getTopic());

                                DeviceManager.getInstance(requireContext()).addDevice(deviceData, true);

                                String payload = deviceData.getId() + "," +
                                        deviceData.getName() + "," + deviceData.getRoom() +
                                        "," + deviceData.getType() + "," +
                                        deviceData.getFunction() + "," + deviceData.getTopic();

                                mqttClient.publish("hap/main/database/add_device", payload);
                                addDeviceView(deviceData);
                            });
                });
        renderDevices();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listener = device ->
                requireActivity().runOnUiThread(() -> updateDeviceView(device));

        DeviceManager.getInstance(requireContext()).addListener(listener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        DeviceManager.getInstance(requireContext()).removeListener(listener);
    }

    // Method to render devices
    private void renderDevices() {

        devicesLayout.removeAllViews();
        deviceViews.clear();

        Set<String> devicesList =
                DeviceManager.getInstance(requireContext()).getAllDeviceIds();

        for (String deviceID : devicesList) {
            DeviceData device =
                    DeviceManager.getInstance(requireContext()).getDevice(deviceID);

            addDeviceView(device);
        }
    }

    // Method to add a device view
    private void addDeviceView(DeviceData device) {

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        View deviceView = inflater.inflate(
                R.layout.device_info, devicesLayout, false);

        devicesLayout.addView(deviceView);
        deviceViews.put(device.getId(), deviceView);

        bindDeviceView(deviceView, device);
    }

    // Method to update a devce view
    private void updateDeviceView(DeviceData device) {

        View deviceView = deviceViews.get(device.getId());

        if (deviceView == null) return;

        bindDeviceView(deviceView, device);
    }

    // Method to bind UI elements
    private void bindDeviceView(View deviceView, DeviceData device) {

        TextView deviceNameTextView = deviceView.findViewById(R.id.deviceNameTextView);
        TextView deviceIdTextView = deviceView.findViewById(R.id.deviceIdTextView);
        TextView deviceRoomTextView = deviceView.findViewById(R.id.lampRoomTextView);
        TextView deviceTypeTextView = deviceView.findViewById(R.id.deviceTypeTextView);
        TextView deviceFunctionTextView = deviceView.findViewById(R.id.deviceFunctionTextView);
        ImageButton deviceConfigImageButton = deviceView.findViewById(R.id.deviceConfigImageButton);
        ImageButton deviceDeleteImageButton = deviceView.findViewById(R.id.deviceDeleteImageButton);

        deviceNameTextView.setText(device.getName());
        deviceIdTextView.setText(device.getId());
        deviceRoomTextView.setText(device.getRoom());
        deviceTypeTextView.setText(device.getType());
        deviceFunctionTextView.setText(device.getFunction());

        // Configure button listener
        deviceConfigImageButton.setOnClickListener(
                v -> DialogManager.openConfigureDeviceDialog(
                        requireActivity(), device,
                        RoomManager.getInstance(requireContext()).getAllRoomNames(),

                        deviceData -> {
                            databaseManager.configureDevice(
                                    deviceData.getId(),
                                    deviceData.getName(),
                                    deviceData.getRoom(),
                                    deviceData.getType(),
                                    deviceData.getFunction(),
                                    deviceData.getTopic());

                            DeviceManager.getInstance(requireContext())
                                    .configureDevice(
                                            device.getId(),
                                            deviceData.getName(),
                                            deviceData.getRoom(),
                                            deviceData.getFunction(),
                                            true);

                            String payload = deviceData.getId() + "," +
                                    deviceData.getName() + "," +
                                    deviceData.getRoom() + "," +
                                    deviceData.getType() + "," +
                                    deviceData.getFunction() + "," +
                                    deviceData.getTopic();

                            mqttClient.publish(
                                    "hap/main/database/update_device", payload);

                            if (deviceData.getLdrThreshold() >= 0) {
                                String topic = "hap/device/" +
                                        deviceData.getId() + "/adjust_ldr";

                                mqttClient.publish(topic,
                                        String.valueOf(deviceData.getLdrThreshold()));
                            }
                        },

                        deviceData -> {

                            FirmwareManager.getFirmwareDataAsync(deviceData.getType(), firmwareData -> {

                                String payload = "{\"version\":\"" +
                                        firmwareData.version + "\"}";

                                mqttClient.publish(
                                        "hap/device/" +
                                                deviceData.getId() + "/update",
                                        payload);
                            });
                        }
                )
        );

        // Delete button listener
        deviceDeleteImageButton.setOnClickListener(v ->
                DialogManager.openDeleteDeviceDialog(
                        requireActivity(), device.getId(),
                        deviceData -> {

                            databaseManager.deleteDevice(device.getId());

                            DeviceManager.getInstance(requireContext())
                                    .deleteDevice(device.getId(), true);

                            mqttClient.publish(
                                    "hap/main/database/delete_device",
                                    device.getId());

                            // 🔥 remove only this view
                            devicesLayout.removeView(deviceView);
                            deviceViews.remove(device.getId());
                        }
                )
        );

        // Online status (visual)
        boolean online = "ONLINE".equals(device.getStatus());
        deviceView.setAlpha(online ? 1.0f : 0.5f);
    }
}
