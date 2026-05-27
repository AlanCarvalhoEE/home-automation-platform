package com.hap.homeautomation.core;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.hap.homeautomation.R;
import com.hap.homeautomation.devices.DeviceData;
import com.hap.homeautomation.devices.DeviceDiscoveryManager;
import com.hap.homeautomation.devices.HapDevicesList;
import com.hap.homeautomation.devices.DeviceManager;
import com.hap.homeautomation.devices.DiscoveredDevice;
import com.hap.homeautomation.devices.OtherDevicesList;
import com.hap.homeautomation.firmware.FirmwareManager;
import com.hap.homeautomation.rooms.RoomData;
import com.hap.homeautomation.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

// Class responsible for managing dialogs
public class DialogManager {

    // Method to open IntroActivity dialogs
    public static void openIntroDialog(Activity activity, String errorMessage, Runnable onRetry) {

        ViewGroup root = activity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_connection, root, false);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogView);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        ConstraintLayout mainLayout = activity.findViewById(R.id.mainLayout);
        TextView messageTextView = dialog.findViewById(R.id.messageTextView);

        if (mainLayout != null) mainLayout.setAlpha(0.25f);
        messageTextView.setText(errorMessage);

        Button yesButton = dialog.findViewById(R.id.yesButton);
        Button noButton = dialog.findViewById(R.id.noButton);

        yesButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (mainLayout != null) mainLayout.setAlpha(1f);

            if (onRetry != null) onRetry.run();
        });

        noButton.setOnClickListener(v -> {
            dialog.dismiss();
            activity.finish();
        });
    }


    // Method to open "Add room" dialog
    public static void openAddRoomDialog(Activity activity, Consumer<RoomData> onConfirm) {

        ViewGroup root = activity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(activity).inflate
                (R.layout.dialog_add_room, root, false);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

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
    public static void openConfigureRoomDialog(Activity activity, RoomData roomData,
                                               Consumer<RoomData> onConfirm) {

        ViewGroup root = activity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(activity).inflate
                (R.layout.dialog_add_room, root, false);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

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
    public static void openDeleteRoomDialog(Activity activity, String roomID, String roomName,
                                            Consumer<RoomData> onConfirm) {

        ViewGroup root = activity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(activity).inflate
                (R.layout.dialog_delete_room, root, false);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

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

        ViewGroup root = activity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(activity).inflate
                (R.layout.dialog_discovered_devices, root, false);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

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

    // Method to open "Add discovered device" dialog
    public static void openAddDiscoveredDeviceDialog(Activity activity,
                                                     DiscoveredDevice discoveredDevice,
                                                     List<String> roomsList,
                                                     Consumer<DeviceData> onConfirm) {

        ViewGroup root = activity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(activity).inflate
                (R.layout.dialog_add_device, root, false);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText nameEditText = dialog.findViewById(R.id.nameEditText);
        Spinner roomSpinner = dialog.findViewById(R.id.roomSpinner);
        Spinner functionSpinner = dialog.findViewById(R.id.functionSpinner);
        Button confirmButton = dialog.findViewById(R.id.yesButton);
        Button cancelButton = dialog.findViewById(R.id.noButton);

        ArrayAdapter<String> roomAdapter;
        roomAdapter = new ArrayAdapter<>(activity, R.layout.spinner_item, roomsList);
        roomSpinner.setAdapter(roomAdapter);

        List<String> functionList = new ArrayList<>();
        for (HapDevicesList function : HapDevicesList.values()) functionList.add(function.name());

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

    // Method to open "Manually add device" dialog
    public static void openAddManualDeviceDialog(Activity activity,
                                                 List<String> roomsList,
                                                 Consumer<DeviceData> onConfirm) {

        ViewGroup root = activity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(activity).inflate
                (R.layout.dialog_add_device, root, false);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText nameEditText = dialog.findViewById(R.id.nameEditText);
        Spinner roomSpinner = dialog.findViewById(R.id.roomSpinner);
        Spinner functionSpinner = dialog.findViewById(R.id.functionSpinner);
        ConstraintLayout idLayout = dialog.findViewById(R.id.idLayout);
        ConstraintLayout topicLayout = dialog.findViewById(R.id.topicLayout);
        EditText idEditText = dialog.findViewById(R.id.idEditText);
        View idSeparator = dialog.findViewById(R.id.idSeparator);
        EditText topicEditText = dialog.findViewById(R.id.topicEditText);
        View topicSeparator = dialog.findViewById(R.id.topicSeparator);
        Button confirmButton = dialog.findViewById(R.id.yesButton);
        Button cancelButton = dialog.findViewById(R.id.noButton);

        idLayout.setVisibility(View.VISIBLE);
        idSeparator.setVisibility(View.VISIBLE);
        topicLayout.setVisibility(View.VISIBLE);
        topicSeparator.setVisibility(View.VISIBLE);

        ArrayAdapter<String> roomAdapter;
        roomAdapter = new ArrayAdapter<>(activity, R.layout.spinner_item, roomsList);
        roomSpinner.setAdapter(roomAdapter);

        List<String> functionList = new ArrayList<>();
        for (OtherDevicesList function : OtherDevicesList.values()) functionList.add(function.name());

        ArrayAdapter<String> functionAdapter;
        functionAdapter = new ArrayAdapter<>(activity, R.layout.spinner_item, functionList);
        functionSpinner.setAdapter(functionAdapter);

        Runnable validateFields = () -> {
            boolean enabled = !nameEditText.getText().toString().trim().isEmpty() &&
                              !idEditText.getText().toString().trim().isEmpty() &&
                              !topicEditText.getText().toString().trim().isEmpty();
            confirmButton.setEnabled(enabled);
        };

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFields.run();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        nameEditText.addTextChangedListener(textWatcher);
        idEditText.addTextChangedListener(textWatcher);
        topicEditText.addTextChangedListener(textWatcher);

        confirmButton.setOnClickListener(v -> {

            String deviceID = idEditText.getText().toString();
            String deviceName = nameEditText.getText().toString();
            String deviceRoom = roomSpinner.getSelectedItem().toString();
            String deviceFunction = functionSpinner.getSelectedItem().toString();
            String deviceTopic = topicEditText.getText().toString();

            if (onConfirm != null) {onConfirm.accept(
                    new DeviceData(deviceID, deviceName, deviceRoom, deviceFunction,
                            deviceFunction, deviceTopic));}
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(view -> dialog.dismiss());
    }

    // Method to open "Configure device" dialog
    public static void openConfigureDeviceDialog(Activity activity, DeviceData device,
                                                 List<String> roomsList,
                                                 Consumer<DeviceData> onConfirm,
                                                 Consumer<DeviceData> onFirmwareUpdate) {

        DeviceManager deviceManager = DeviceManager.getInstance(activity);
        ViewGroup root = activity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(activity).inflate
                (R.layout.dialog_configure_device, root, false);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText nameEditText = dialog.findViewById(R.id.nameEditText);
        Spinner roomSpinner = dialog.findViewById(R.id.roomSpinner);
        Spinner functionSpinner = dialog.findViewById(R.id.functionSpinner);
        TextView ldrThresholdEditText = dialog.findViewById(R.id.ldrThresholdEditText);
        TextView ldrReadingTextView = dialog.findViewById(R.id.ldrReadingValueTextView);
        TextView currentVersionTextView = dialog.findViewById(R.id.currentVersionTextView);
        TextView latestVersionTextView = dialog.findViewById(R.id.latestVersionTextView);
        TextView firmwareStatusTextView = dialog.findViewById(R.id.firmwareStatusTextView);
        ImageButton firmwareUpdateImageButton = dialog.findViewById(R.id.updateImageButton);
        ProgressBar loadingProgressBar = dialog.findViewById(R.id.loadingProgressBar);
        Button confirmButton = dialog.findViewById(R.id.yesButton);
        Button cancelButton = dialog.findViewById(R.id.noButton);

        ArrayAdapter<String> roomAdapter;
        roomAdapter = new ArrayAdapter<>(activity, R.layout.spinner_item, roomsList);
        roomSpinner.setAdapter(roomAdapter);

        List<String> functionList = new ArrayList<>();
        for (HapDevicesList function : HapDevicesList.values()) functionList.add(function.name());
        ArrayAdapter<String> functionAdapter;
        functionAdapter = new ArrayAdapter<>(activity, R.layout.spinner_item, functionList);
        functionSpinner.setAdapter(functionAdapter);

        DeviceData currentDevice = deviceManager.getDevice(device.getId());
        ldrReadingTextView.setText(
                String.valueOf(currentDevice.getLdrValue())
        );

        String currentFirmwareVersion = currentDevice.getFirmwareVersion();

        nameEditText.setText(currentDevice.getName());
        currentVersionTextView.setText(currentFirmwareVersion);
        roomSpinner.setSelection(roomAdapter.getPosition(currentDevice.getRoom()));
        functionSpinner.setSelection(functionAdapter.getPosition(currentDevice.getFunction()));
        ldrThresholdEditText.setText(String.valueOf(currentDevice.getLdrThreshold()));

        final String[] latestFirmwareVersion = new String[1];
        FirmwareManager.getFirmwareDataAsync(device.getType(), firmwareData -> {

            if (!dialog.isShowing()) return;

            latestFirmwareVersion[0] = firmwareData.version;
            String currentFirmwareVersionLocal = currentDevice.getFirmwareVersion();

            latestVersionTextView.setText(latestFirmwareVersion[0]);

            if (currentFirmwareVersionLocal.equals(latestFirmwareVersion[0])) {
                firmwareStatusTextView.setText(
                        activity.getString(R.string.firmware_updated_message));
                firmwareUpdateImageButton.setEnabled(false);
            } else {
                firmwareStatusTextView.setText(
                        activity.getString(R.string.firmware_not_updated_message));
                firmwareUpdateImageButton.setEnabled(true);
            }
        });

        if (currentDevice.getFunction().contains("ldr")) {
            ConstraintLayout ldrLayout = dialog.findViewById(R.id.ldrLayout);
            ldrLayout.setVisibility(View.VISIBLE);
        }

        confirmButton.setEnabled(true);

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
            String deviceFunction = functionSpinner.getSelectedItem().toString();
            int ldrThreshold = Integer.parseInt(ldrThresholdEditText.getText().toString());

            DeviceData updated = new DeviceData(device.getId(), deviceName, deviceRoom,
                    device.getType(), deviceFunction, device.getTopic());

            updated.setLdrThreshold(ldrThreshold);

            onConfirm.accept(updated);
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
                            .equals(latestFirmwareVersion[0])) {

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

                ldrReadingTextView.setText(String.valueOf(updatedDevice.getLdrValue()));
            });
        };

        deviceManager.addListener(updateListener);
        dialog.setOnDismissListener(d -> deviceManager.removeListener(updateListener));
    }

    // Method to open "Delete device" dialog
    public static void openDeleteDeviceDialog(Activity activity, String deviceID,
                                              Consumer<String> onConfirm) {

        ViewGroup root = activity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(activity).inflate
                (R.layout.dialog_delete_device, root, false);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button confirmButton = dialog.findViewById(R.id.yesButton);
        Button cancelButton = dialog.findViewById(R.id.noButton);

        confirmButton.setOnClickListener(v -> {

            if (onConfirm != null) onConfirm.accept(deviceID);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(view -> dialog.dismiss());
    }

    // Method to open "Language selection" dialog
    public static void openLanguageSelectionDialog(Activity activity, String currentLanguage) {

        ViewGroup root = activity.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(activity).inflate
                (R.layout.dialog_language, root, false);

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int)(activity.getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

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
