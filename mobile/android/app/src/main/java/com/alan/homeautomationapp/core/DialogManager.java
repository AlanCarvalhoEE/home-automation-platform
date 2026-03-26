package com.alan.homeautomationapp.core;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.alan.homeautomationapp.R;
import com.alan.homeautomationapp.devices.DeviceData;
import com.alan.homeautomationapp.devices.DeviceDiscoveryManager;
import com.alan.homeautomationapp.devices.DeviceFunction;
import com.alan.homeautomationapp.devices.DeviceManager;
import com.alan.homeautomationapp.devices.DiscoveredDevice;
import com.alan.homeautomationapp.rooms.RoomData;
import com.alan.homeautomationapp.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

// Class responsible for managing dialogs
public class DialogManager {

    // Method to open "Add room" dialog
    public static void openAddRoomDialog(Activity activity, Consumer<RoomData> onConfirm) {

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_room, null);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        window.setLayout(
                (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9), WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        EditText nameEditText = dialog.findViewById(R.id.nameEditText);
        Button confirmButton = dialog.findViewById(R.id.yesButton);
        Button cancelButton = dialog.findViewById(R.id.noButton);

        nameEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmButton.setEnabled(s.length() > 0);
            }
        });

        confirmButton.setOnClickListener(v -> {
            String roomID = UUID.randomUUID().toString();
            String roomName = nameEditText.getText().toString();

            if (onConfirm != null) {onConfirm.accept(new RoomData(roomID, roomName));}
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(view -> dialog.dismiss());
    }

    // Method to open "Configure room" dialog
    public static void openConfigureRoomDialog(Activity activity, RoomData roomData, Consumer<RoomData> onConfirm) {

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_room, null);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        window.setLayout(
                (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9), WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        EditText nameEditText = dialog.findViewById(R.id.nameEditText);
        Button confirmButton = dialog.findViewById(R.id.yesButton);
        Button cancelButton = dialog.findViewById(R.id.noButton);

        nameEditText.setText(roomData.getName());
        confirmButton.setEnabled(true);

        nameEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmButton.setEnabled(s.length() > 0);
            }
        });

        confirmButton.setOnClickListener(v -> {

            String newRoomName = nameEditText.getText().toString();

            if (onConfirm != null) {onConfirm.accept(new RoomData(roomData.getId(), newRoomName));}
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(view -> dialog.dismiss());
    }

    // Method to open "Delete room" dialog
    public static void openDeleteRoomDialog(Activity activity, String roomID, String roomName, Consumer<RoomData> onConfirm) {

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_delete_room, null);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        window.setLayout(
                (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9), WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        Button confirmButton = dialog.findViewById(R.id.yesButton);
        Button cancelButton = dialog.findViewById(R.id.noButton);

        confirmButton.setOnClickListener(v -> {

            if (onConfirm != null) {onConfirm.accept(new RoomData(roomID, roomName));}
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(view -> dialog.dismiss());
    }

    // Method to open "Device search" dialog
    public static void openDiscoveredDevicesDialog(Activity activity,
                                                   DeviceDiscoveryManager discoveryManager,
                                                   Consumer<DiscoveredDevice> onDeviceSelected) {

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_discovered_devices, null);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        window.setLayout(
                (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9), WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        LinearLayout discoveredLayout = dialog.findViewById(R.id.discoveredLayout);

        discoveryManager.startDiscovery(device -> activity.runOnUiThread(() -> {

            View vi = LayoutInflater.from(activity)
                    .inflate(R.layout.device_discovered, discoveredLayout, false);
            discoveredLayout.addView(vi);

            TextView deviceIdTextView = vi.findViewById(R.id.deviceIdTextView);
            TextView deviceTypeTextView = vi.findViewById(R.id.deviceTypeTextView);
            ImageButton deviceAddImageButton = vi.findViewById(R.id.deviceAddImageButton);

            deviceIdTextView.setText(device.id);
            deviceTypeTextView.setText(device.type);

            DiscoveredDevice discoveredDevice =
                    new DiscoveredDevice(device.id, device.type);

            deviceAddImageButton.setOnClickListener(v -> {
                if (onDeviceSelected != null) {
                    onDeviceSelected.accept(discoveredDevice);
                }

                dialog.dismiss();
            });
        }));

        Button cancelButton = dialog.findViewById(R.id.noButton);

        cancelButton.setOnClickListener(v -> {
            discoveryManager.stopDiscovery();
            dialog.dismiss();
        });

        dialog.setOnDismissListener(d -> discoveryManager.stopDiscovery());
    }

    // Method to open "Add device" dialog
    public static void openAddDeviceDialog(Activity activity, DiscoveredDevice discoveredDevice,
                                           List<String> roomsList, Consumer<DeviceData> onConfirm) {

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_device, null);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        window.setLayout(
                (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9), WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        EditText nameEditText = dialog.findViewById(R.id.nameEditText);
        Spinner roomSpinner = dialog.findViewById(R.id.roomSpinner);
        Spinner functionSpinner = dialog.findViewById(R.id.functionSpinner);
        Button confirmButton = dialog.findViewById(R.id.yesButton);
        Button cancelButton = dialog.findViewById(R.id.noButton);

        ArrayAdapter<String> roomAdapter;
        roomAdapter = new ArrayAdapter<>(activity, R.layout.spinner_item, roomsList);
        roomSpinner.setAdapter(roomAdapter);

        List<String> functionList = new ArrayList<>();
        for (DeviceFunction function : DeviceFunction.values()) functionList.add(function.name());

        ArrayAdapter<String> functionAdapter;
        functionAdapter = new ArrayAdapter<>(activity, R.layout.spinner_item, functionList);
        functionSpinner.setAdapter(functionAdapter);

        nameEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmButton.setEnabled(s.length() > 0);
            }
        });

        confirmButton.setOnClickListener(v -> {

            String deviceID = discoveredDevice.id;
            String deviceName = nameEditText.getText().toString();
            String deviceRoom = roomSpinner.getSelectedItem().toString();
            String deviceFunction = functionSpinner.getSelectedItem().toString();
            String deviceType = discoveredDevice.type;

            if (onConfirm != null) {onConfirm.accept(
                    new DeviceData(deviceID, deviceName, deviceRoom, deviceType,
                            deviceFunction, deviceID));}
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(view -> dialog.dismiss());
    }

    // Method to open "Configure device" dialog
    public static void openConfigureDeviceDialog(Activity activity, DeviceData device,
                                                 List<String> roomsList,
                                                 String latestFirmwareVersion,
                                                 Consumer<DeviceData> onConfirm,
                                                 Consumer<DeviceData> onFirmwareUpdate) {

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_configure_device, null);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        window.setLayout(
                (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9), WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        EditText nameEditText = dialog.findViewById(R.id.nameEditText);
        Spinner roomSpinner = dialog.findViewById(R.id.roomSpinner);
        Spinner functionSpinner = dialog.findViewById(R.id.functionSpinner);
        TextView currentVersionTextView = dialog.findViewById(R.id.currentVersionTextView);
        TextView latestVersionTextView = dialog.findViewById(R.id.latestVersionTextView);
        TextView firmwareStatusTextView = dialog.findViewById(R.id.firmwareStatusTextView);
        ImageButton firmwareUpdateImageButton = dialog.findViewById(R.id.updateImageButton);
        ProgressBar loadingProgressBar = dialog.findViewById(R.id.loadingProgressBar);
        Button confirmButton = dialog.findViewById(R.id.yesButton);
        Button cancelButton = dialog.findViewById(R.id.noButton);

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(activity, R.layout.spinner_item, roomsList);
        roomSpinner.setAdapter(adapter);

        List<String> functionList = new ArrayList<>();
        for (DeviceFunction function : DeviceFunction.values()) functionList.add(function.name());

        ArrayAdapter<String> functionAdapter;
        functionAdapter = new ArrayAdapter<>(activity, R.layout.spinner_item, functionList);
        functionSpinner.setAdapter(functionAdapter);

        String currentFirmwareVersion = device.getFirmwareVersion();

        nameEditText.setText(device.getName());
        currentVersionTextView.setText(currentFirmwareVersion);
        latestVersionTextView.setText(latestFirmwareVersion);
        roomSpinner.setSelection(adapter.getPosition(device.getRoom()));

        confirmButton.setEnabled(true);

        if (currentFirmwareVersion.equals(latestFirmwareVersion)) {
            firmwareStatusTextView.setText(activity.getString(R.string.firmware_updated_message));
        } else {
            firmwareStatusTextView.setText(activity.getString(R.string.firmware_not_updated_message));
        }

        firmwareUpdateImageButton.setEnabled(!currentFirmwareVersion.equals(latestFirmwareVersion));

        nameEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmButton.setEnabled(s.length() > 0);
            }
        });

        firmwareUpdateImageButton.setOnClickListener(v -> {
            if (onFirmwareUpdate != null) {
                onFirmwareUpdate.accept(device);
            }
        });

        confirmButton.setOnClickListener(v -> {

            String deviceName = nameEditText.getText().toString();
            String deviceRoom = roomSpinner.getSelectedItem().toString();

            if (onConfirm != null) {onConfirm.accept(
                    new DeviceData(device.getId(), deviceName, deviceRoom, device.getType(),
                            device.getFunction(), device.getTopic()));}
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(view -> dialog.dismiss());

        DeviceManager.DeviceUpdateListener updateListener = updatedDevice -> {

            if (!updatedDevice.getId().equals(device.getId())) return;

            activity.runOnUiThread(() -> {

                String status = updatedDevice.getStatus();

                if ("UPDATING".equals(status)) {
                    firmwareStatusTextView.setText(activity.getString(R.string.firmware_updating_message));
                    firmwareUpdateImageButton.setEnabled(false);
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    confirmButton.setEnabled(false);
                    cancelButton.setEnabled(false);
                }

                if ("ONLINE".equals(status)) {
                    if (updatedDevice.getFirmwareVersion()
                            .equals(latestFirmwareVersion)) {

                        currentVersionTextView.setText(updatedDevice.getFirmwareVersion());
                        firmwareStatusTextView.setText(activity.getString(R.string.firmware_updated_message));
                        firmwareUpdateImageButton.setEnabled(false);
                        loadingProgressBar.setVisibility(View.INVISIBLE);
                        confirmButton.setEnabled(true);
                        cancelButton.setEnabled(true);

                    } else {
                        firmwareStatusTextView.setText(activity.getString(R.string.firmware_not_updated_message));
                        loadingProgressBar.setVisibility(View.INVISIBLE);
                        confirmButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                    }
                }
            });
        };

        DeviceManager.getInstance(activity.getApplicationContext()).addListener(updateListener);

        dialog.setOnDismissListener(d ->
                DeviceManager.getInstance(activity.getApplicationContext()).removeListener(updateListener));
    }

    // Method to open "Delete device" dialog
    public static void openDeleteDeviceDialog(Activity activity, String deviceID, Consumer<String> onConfirm) {

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_delete_device, null);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        window.setLayout(
                (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9), WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        Button confirmButton = dialog.findViewById(R.id.yesButton);
        Button cancelButton = dialog.findViewById(R.id.noButton);

        confirmButton.setOnClickListener(v -> {

            if (onConfirm != null) onConfirm.accept(deviceID);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(view -> dialog.dismiss());
    }

    // Method to open "Configure LDR" dialog
    public static void openConfigureLdrDialog(Activity activity, int currentReading,
                                              int currentThreshold, Consumer<Integer> onConfirm) {

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_config_ldr, null);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        window.setLayout(
                (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9), WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        TextView readingTextView = dialog.findViewById(R.id.readingTextView);
        SeekBar thresholdSeekBar = dialog.findViewById(R.id.thresholdSeekBar);
        TextView thresholdTextView = dialog.findViewById(R.id.thresholdTextView);
        Button confirmButton = dialog.findViewById(R.id.yesButton);
        Button cancelButton = dialog.findViewById(R.id.noButton);

        if (currentReading != -1) {
            readingTextView.setText(String.valueOf(currentReading));
        }

        if (currentThreshold != -1) {
            thresholdSeekBar.setProgress(currentThreshold);
            thresholdTextView.setText(String.valueOf(currentThreshold));
        }

        thresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    thresholdTextView.setText(String.valueOf(progress));
                    confirmButton.setEnabled(true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        confirmButton.setOnClickListener(v -> {

            int selectedThreshold = thresholdSeekBar.getProgress();

            if (onConfirm != null) {
                onConfirm.accept(selectedThreshold);
            }

            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }

    // Method to open "Language selection" dialog
    public static void openLanguageSelectionDialog(Activity activity, String currentLanguage) {

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_language, null);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        window.setLayout(
                (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9), WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        RadioGroup languageRadioGroup = dialog.findViewById(R.id.languageRadioGroup);
        Button confirmButton = dialog.findViewById(R.id.yesButton);
        Button cancelButton = dialog.findViewById(R.id.noButton);

        confirmButton.setEnabled(true);

        switch (currentLanguage) {
            case "pt": languageRadioGroup.check(R.id.portugueseRadioButton);
                break;
            default: languageRadioGroup.check(R.id.englishRadioButton);
        }

        confirmButton.setOnClickListener(v -> {
            int selectedID = languageRadioGroup.getCheckedRadioButtonId();

            if (selectedID == R.id.englishRadioButton) {
                LanguageManager.setLanguage("en");
            }
            else if (selectedID == R.id.portugueseRadioButton) {
                LanguageManager.setLanguage("pt");
            }

            dialog.dismiss();

            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            activity.startActivity(intent);
            activity.finish();
        });

        cancelButton.setOnClickListener(view -> dialog.dismiss());
    }
}
