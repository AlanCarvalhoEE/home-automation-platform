package com.alan.homeautomationapp;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Utils {

    private static VideoStreamPlayer streamPlayer;
    private static final String rtspUrl = "rtsp://192.168.88.50:554/avstream/channel=1/stream=1.sdp";

    private static int temperature = 20;
    static MQTTclient mqttClient = MQTTclient.getInstance();

    // Per-device LDR state holder (attached to each device view)
    public static class DeviceState {
        public boolean ldrEnabled = false;
        public int ldrThreshold = -1;
        public int ldrValue = -1;
        public boolean loadOn = false;
    }

    // Function to update rooms from database
    public static void updateRooms(Context context, DBhandler database) {
        Activity activity = (Activity) context;
        Spinner roomSpinner = activity.findViewById(R.id.roomSpinner);
        ArrayAdapter<String> adapter;

        adapter = new ArrayAdapter<>(
                context, R.layout.spinner_item, database.getRoomsList());
        roomSpinner.setAdapter(adapter);
    }

    // Function to update rooms from database
    public static void updateConfigurationRooms(View rootView, DBhandler database) {
        LinearLayout roomsLayout = rootView.findViewById(R.id.roomsLayout);
        roomsLayout.removeAllViews();
        List<String> roomsList = database.getRoomsList();

        for (int i = 0; i < roomsList.size(); i++) {
            LayoutInflater inflater = (LayoutInflater)
                    rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            String roomID = database.getRoomID(roomsList.get(i));
            String roomTopic = database.getRoomTopic(roomID);

            View vi = inflater.inflate(R.layout.room_info, roomsLayout, false);
            roomsLayout.addView(vi);
            TextView roomNameTextView = vi.findViewById(R.id.roomNameTextView);
            TextView roomTopicTextView = vi.findViewById(R.id.roomTopicTextView);
            ImageButton roomConfigImageButton = vi.findViewById(R.id.roomConfigImageButton);
            ImageButton roomDeleteImageButton = vi.findViewById(R.id.roomDeleteImageButton);

            roomNameTextView.setText(roomsList.get(i));
            roomTopicTextView.setText(roomTopic);
            String roomName = roomsList.get(i);

            // Room configuration button listener
            roomConfigImageButton.setOnClickListener(v ->
                    Utils.openDialog(rootView, rootView.getContext(), database,"dialog_update_room", roomName));

            // Room delete button listener
            roomDeleteImageButton.setOnClickListener(v ->
                    Utils.openDialog(rootView, rootView.getContext(), database,"dialog_delete_room", roomName));
        }
    }

    // Function to update devices from database
    @SuppressLint({"InflateParams", "SetTextI18n"})
    public static void updateDevices(Context context, DBhandler database) {
        Activity activity = (Activity) context;
        Spinner roomSpinner = activity.findViewById(R.id.roomSpinner);
        LinearLayout roomDevicesLayout = activity.findViewById(R.id.roomDevicesLayout);
        List<String> devicesList = database.getDevicesListByRoom(roomSpinner.getSelectedItem().toString());

        if (roomDevicesLayout.getChildCount() > 0) roomDevicesLayout.removeAllViews();

        for (int i = 0; i < devicesList.size(); i++) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View vi;

            String deviceID = database.getDeviceID(devicesList.get(i));
            String deviceType = database.getDeviceType(deviceID);
            String deviceTopic = database.getDeviceTopic(deviceID);
            String roomName = roomSpinner.getSelectedItem().toString();
            String roomID = database.getRoomID(roomName);
            String roomTopic = database.getRoomTopic(roomID);

            if (deviceType.contains("Lamp")) {
                vi = inflater.inflate(R.layout.device_lamp, null);
                TextView lampNameTextView = vi.findViewById(R.id.lampNameTextView);
                ToggleButton lampControlToggleButton = vi.findViewById(R.id.lampControlToggleButton);
                ImageButton lampConfigImageButton = vi.findViewById(R.id.lampConfigImageButton);
                ImageButton lampDeleteImageButton = vi.findViewById(R.id.lampDeleteImageButton);
                ToggleButton lampLdrToggleButton = vi.findViewById(R.id.lampLdrToggleButton);

                lampNameTextView.setText(devicesList.get(i));
                roomDevicesLayout.addView(vi, 0, new ViewGroup.LayoutParams(
                        MATCH_PARENT, WRAP_CONTENT));

                lampControlToggleButton.setTag(deviceID);
                // Attach deviceID to the device container and create per-device state
                vi.setTag(deviceID);
                DeviceState deviceState = new DeviceState();
                vi.setTag(R.id.lampNameTextView, deviceState);

                String getStateTopic = "hap/";
                getStateTopic += roomTopic + "/";
                getStateTopic += deviceTopic + "/";
                getStateTopic += "get_state";

                mqttClient.subscribe(getStateTopic, (topic1, message) -> {

                    try {
                        JSONObject json = new JSONObject(message);

                        // Update deviceState attached to this view
                        DeviceState ds = (DeviceState) vi.getTag(R.id.lampNameTextView);

                        if (json.has("load_status")) {
                            String state = json.getString("load_status");
                            ds.loadOn = state.equalsIgnoreCase("ON");
                        }

                        if (json.has("ldr_status")) {
                            String state = json.getString("ldr_status");
                            ds.ldrEnabled = state.equalsIgnoreCase("ENABLED");
                        }

                        if (json.has("ldr_threshold")) {
                            ds.ldrThreshold = json.optInt("ldr_threshold", -1);
                        }

                        if (json.has("ldr_value")) {
                            ds.ldrValue = json.optInt("ldr_value", -1);
                        }

                        // Update UI on main thread
                        activity.runOnUiThread(() -> {
                            lampControlToggleButton.setChecked(ds.loadOn);

                            lampLdrToggleButton.setChecked(ds.ldrEnabled);
                            lampControlToggleButton.setClickable(!ds.ldrEnabled);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();}
                });

                lampControlToggleButton.setVisibility(View.VISIBLE);
                lampConfigImageButton.setVisibility(View.INVISIBLE);
                lampDeleteImageButton.setVisibility(View.INVISIBLE);

                lampControlToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                    String setStateTopic = "hap/";
                    setStateTopic += roomTopic + "/";
                    setStateTopic += deviceTopic + "/";
                    setStateTopic += "set_state";

                    if (isChecked) mqttClient.publish(setStateTopic, "ON");
                    else mqttClient.publish(setStateTopic, "OFF");
                });

                if (deviceType.contains("LDR")) {
                    lampLdrToggleButton.setVisibility(View.VISIBLE);

                    lampLdrToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                        String enableLdrTopic = "hap/";
                        enableLdrTopic += roomTopic + "/";
                        enableLdrTopic += deviceTopic + "/";
                        enableLdrTopic += "enable_ldr";

                        if (isChecked) {
                            mqttClient.publish(enableLdrTopic, "ENABLE");
                            lampControlToggleButton.setClickable(false);
                        }
                        else {
                            mqttClient.publish(enableLdrTopic, "DISABLE");
                            lampControlToggleButton.setClickable(true);
                        }
                    });
                }
            }

            else if(deviceType.contains("Camera")) {
                vi = inflater.inflate(R.layout.device_camera, null);
                TextView cameraNameTextView = vi.findViewById(R.id.cameraNameTextView);
                ToggleButton cameraControlToggleButton = vi.findViewById(R.id.cameraControlToggleButton);
                ImageButton cameraConfigImageButton = vi.findViewById(R.id.cameraConfigImageButton);
                ImageButton cameraDeleteImageButton = vi.findViewById(R.id.cameraDeleteImageButton);
                FrameLayout videoLayout = vi.findViewById(R.id.videoLayout);
                VLCVideoLayout cameraVideoLayout = vi.findViewById(R.id.cameraVideoLayout);

                streamPlayer = new VideoStreamPlayer(context, cameraVideoLayout);

                cameraNameTextView.setText(devicesList.get(i));
                roomDevicesLayout.addView(vi, 0, new ViewGroup.LayoutParams(
                        MATCH_PARENT, WRAP_CONTENT));

                cameraControlToggleButton.setTag(deviceID);

                cameraControlToggleButton.setVisibility(View.VISIBLE);
                cameraConfigImageButton.setVisibility(View.INVISIBLE);
                cameraDeleteImageButton.setVisibility(View.INVISIBLE);

                cameraControlToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                    if (isChecked) {
                        videoLayout.setVisibility(View.VISIBLE);
                        streamPlayer.startVideo(rtspUrl);
                    } else {
                        videoLayout.setVisibility(View.GONE);
                        streamPlayer.stopVideo();
                    }
                });
            }

            else if(deviceType.contains("Air conditioner")) {
                vi = inflater.inflate(R.layout.device_air_conditioner, null);
                TextView airNameTextView = vi.findViewById(R.id.airNameTextView);
                ToggleButton airControlToggleButton = vi.findViewById(R.id.airControlToggleButton);
                ImageButton upImageButton = vi.findViewById(R.id.upImageButton);
                ImageButton downImageButton = vi.findViewById(R.id.downImageButton);
                EditText temperatureEditText = vi.findViewById(R.id.temperatureEditText);
                airNameTextView.setText(devicesList.get(i));
                temperatureEditText.setText(String.valueOf(temperature));
                roomDevicesLayout.addView(vi, 0, new ViewGroup.LayoutParams(
                        MATCH_PARENT, WRAP_CONTENT));

                airControlToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                    //if (isChecked) tcpClient.sendMessage("SET-" + designator + "_ON");
                    //else tcpClient.sendMessage("SET-" + designator + "_OFF");
                });

                upImageButton.setOnClickListener(view -> {
                    temperature++;
                    temperatureEditText.setText(String.valueOf(temperature));
                });

                downImageButton.setOnClickListener(view -> {
                    temperature--;
                    temperatureEditText.setText(String.valueOf(temperature));
                });

                temperatureEditText.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        temperature = Integer.parseInt(s.toString());
                        //tcpClient.sendMessage("SET-" + designator + "_T" + temperature);
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }
                });
            }
        }
    }

    // Function to update devices from database
    @SuppressLint({"InflateParams", "SetTextI18n"})
    public static void updateConfigurationDevices(View rootView, DBhandler database) {

        LinearLayout devicesLayout = rootView.findViewById(R.id.devicesLayout);
        List<String> devicesList = database.getDevicesList();

        for (int i = 0; i < devicesList.size(); i++) {
            LayoutInflater inflater = (LayoutInflater)
                    rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            String deviceID = database.getDeviceID(devicesList.get(i));
            String deviceType = database.getDeviceType(deviceID);
            String deviceRoom = database.getDeviceRoom(deviceID);

            View vi = inflater.inflate(R.layout.device_info, devicesLayout, false);
            devicesLayout.addView(vi);
            TextView deviceNameTextView = vi.findViewById(R.id.deviceNameTextView);
            TextView deviceIdTextView = vi.findViewById(R.id.deviceIdTextView);
            TextView deviceRoomTextView = vi.findViewById(R.id.deviceRoomTextView);
            TextView deviceTypeTextView = vi.findViewById(R.id.deviceTypeTextView);

            deviceNameTextView.setText(devicesList.get(i));
            deviceIdTextView.setText(deviceID);
            deviceTypeTextView.setText(deviceType);
            deviceRoomTextView.setText(deviceRoom);
        }
    }

    @SuppressLint("InflateParams")
    public static void openDialog(View rootView, Context context, DBhandler dbHandler, String dialogType, String data) {
        Activity activity = (Activity) context;
        ConstraintLayout backgroundLayout = activity.findViewById(R.id.mainLayout);
        backgroundLayout.setAlpha(0.25f);

        View dialogView = null;

        if (dialogType.equals("dialog_add_room")) {
            dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_add_room, null);
        }
        else if (dialogType.equals("dialog_update_room")) {
            dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_add_room, null);
        }
        else if (dialogType.equals("dialog_delete_room")) {
            dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_delete_room, null);
        }
        else if (dialogType.contains("dialog_discovered_devices")) {
            dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_discovered_devices, null);
        }
        else if (dialogType.contains("dialog_add_device")) {
            dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_add_device, null);
        }
        else if (dialogType.contains("dialog_delete_device")) {
            dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_delete_device, null);
        }
        else if (dialogType.contains("dialog_config_ldr")) {
            dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_config_ldr, null);
        }

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        Objects.requireNonNull(window).setLayout(1000, WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        Button confirmButton = dialog.findViewById(R.id.yesButton);
        Button cancelButton = dialog.findViewById(R.id.noButton);

        cancelButton.setOnClickListener(view -> {
            backgroundLayout.setAlpha(1f);
            dialog.dismiss();
        });

        switch (dialogType) {

            case "dialog_add_room" -> {

                EditText nameEditText = dialog.findViewById(R.id.nameEditText);
                EditText topicEditText = dialog.findViewById(R.id.topicEditText);

                nameEditText.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {}
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        confirmButton.setEnabled(s.length() > 0);
                    }
                });

                topicEditText.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {}
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        confirmButton.setEnabled(s.length() > 0);
                    }
                });

                confirmButton.setOnClickListener(v -> {
                    String roomID = UUID.randomUUID().toString();
                    String roomName = nameEditText.getText().toString();
                    String roomTopic = topicEditText.getText().toString();

                    dbHandler.addRoom(roomID, roomName, roomTopic);
                    Utils.updateConfigurationRooms(rootView, dbHandler);

                    String payload = roomID + "," + roomName + "," + roomTopic;
                    mqttClient.publish("hap/main/database/add_room", payload);

                    backgroundLayout.setAlpha(1f);
                    dialog.dismiss();
                });
            }

            case "dialog_update_room" -> {

                EditText nameEditText = dialog.findViewById(R.id.nameEditText);
                EditText topicEditText = dialog.findViewById(R.id.topicEditText);

                String roomID = dbHandler.getRoomID(data);
                String roomTopic = dbHandler.getRoomTopic(roomID);
                nameEditText.setText(data);
                topicEditText.setText(roomTopic);

                nameEditText.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {}
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        confirmButton.setEnabled(s.length() > 0);
                    }
                });

                topicEditText.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {}
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        confirmButton.setEnabled(s.length() > 0);
                    }
                });

                confirmButton.setOnClickListener(v -> {
                    String newRoomName = nameEditText.getText().toString();
                    String newRoomTopic = topicEditText.getText().toString();

                    dbHandler.updateRoom(roomID, newRoomName, newRoomTopic);
                    Utils.updateConfigurationRooms(rootView, dbHandler);

                    String payload = roomID + "," + newRoomName + "," + newRoomTopic;
                    mqttClient.publish("hap/main/database/update_room", payload);

                    backgroundLayout.setAlpha(1f);
                    dialog.dismiss();
                });
            }

            case "dialog_delete_room" -> confirmButton.setOnClickListener(view -> {

                String roomID = dbHandler.getRoomID(data);
                dbHandler.deleteRoom(roomID);
                Utils.updateConfigurationRooms(rootView, dbHandler);

                mqttClient.publish("hap/main/database/delete_room", roomID);

                backgroundLayout.setAlpha(1f);
                dialog.dismiss();
            });

            case "dialog_discovered_devices" -> {

                DeviceDiscoveryManager discoveryManager = new DeviceDiscoveryManager();
                LinearLayout discoveredLayout = dialog.findViewById(R.id.discoveredLayout);

                discoveryManager.clear();
                if (discoveredLayout.getChildCount() > 0) discoveredLayout.removeAllViews();

                discoveryManager.startDiscovery(device -> activity.runOnUiThread(() -> {

                    LayoutInflater inflater = (LayoutInflater)
                            rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View vi = inflater.inflate(R.layout.device_discovered, discoveredLayout, false);
                    discoveredLayout.addView(vi);

                    TextView deviceIdTextView = vi.findViewById(R.id.deviceIdTextView);
                    TextView deviceTypeTextView = vi.findViewById(R.id.deviceTypeTextView);
                    ImageButton deviceAddImageButton = vi.findViewById(R.id.deviceAddImageButton);

                    deviceIdTextView.setText(device.id);
                    deviceTypeTextView.setText(device.type);
                    String deviceData = device.id + "," + device.type;

                    deviceAddImageButton.setOnClickListener(vv ->
                            Utils.openDialog(rootView, context, dbHandler, "dialog_add_device", deviceData));
                }));
            }

            case "dialog_add_device" -> {

                EditText nameEditText = dialog.findViewById(R.id.nameEditText);
                EditText roomEditText = dialog.findViewById(R.id.roomEditText);

                nameEditText.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {}
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        confirmButton.setEnabled(s.length() > 0);
                    }
                });

                roomEditText.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {}
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        confirmButton.setEnabled(s.length() > 0);
                    }
                });

                confirmButton.setOnClickListener(v -> {
                    String deviceName = nameEditText.getText().toString();
                    String deviceRoom = roomEditText.getText().toString();

                    String[] deviceData = data.split(",");
                    String deviceID = deviceData[0];
                    String deviceType = deviceData[1];

                    dbHandler.addDevice(deviceID, deviceName, deviceRoom, deviceType, deviceID);
                    Utils.updateConfigurationRooms(rootView, dbHandler);

                    String payload = deviceID + "," + deviceName + "," + deviceRoom + ", " + deviceType + ", " + deviceID;
                    mqttClient.publish("hap/main/database/add_room", payload);

                    backgroundLayout.setAlpha(1f);
                    dialog.dismiss();
                });
            }

            case "dialog_delete_device" -> confirmButton.setOnClickListener(view -> {

                String deviceID = dbHandler.getDeviceID(data);
                dbHandler.deleteDevice(deviceID);

                mqttClient.publish("hap/main/database/delete_device", deviceID);
                updateDevices(context, dbHandler);

                backgroundLayout.setAlpha(1f);
                dialog.dismiss();
            });

            case "dialog_config_ldr" -> {

                String deviceID = dbHandler.getDeviceID(data);
                LinearLayout roomDevicesLayout = activity.findViewById(R.id.roomDevicesLayout);
                View deviceView = findViewByTag(deviceID, roomDevicesLayout);
                TextView readingTextView = dialog.findViewById(R.id.readingTextView);
                SeekBar thresholdSeekBar = dialog.findViewById(R.id.thresholdSeekBar);
                TextView thresholdTextView = dialog.findViewById(R.id.thresholdTextView);

                thresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) thresholdTextView.setText(String.valueOf(progress));
                        confirmButton.setEnabled(true);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

                if (deviceView != null) {
                    DeviceState ds = (DeviceState) deviceView.getTag(R.id.lampNameTextView);

                    if (ds != null) {
                        if (ds.ldrValue != -1) {
                            readingTextView.setText(String.valueOf(ds.ldrValue));
                        }
                        if (ds.ldrThreshold != -1) {
                            thresholdSeekBar.setProgress(ds.ldrThreshold);
                            thresholdTextView.setText(String.valueOf(ds.ldrThreshold));
                        }
                    }
                }

                confirmButton.setOnClickListener(view -> {

                    Spinner roomSpinner = activity.findViewById(R.id.roomSpinner);
                    String roomName = roomSpinner.getSelectedItem().toString();
                    String roomID = dbHandler.getRoomID(roomName);
                    String roomTopic = dbHandler.getRoomTopic(roomID);
                    String deviceTopic = dbHandler.getDeviceTopic(deviceID);

                    String payload = (String) thresholdTextView.getText();

                    String adjustLdrTopic = "hap/";
                    adjustLdrTopic += roomTopic + "/";
                    adjustLdrTopic += deviceTopic + "/";
                    adjustLdrTopic += "adjust_ldr";
                    mqttClient.publish(adjustLdrTopic, payload);
                    updateDevices(context, dbHandler);

                    backgroundLayout.setAlpha(1f);
                    dialog.dismiss();
                });
            }
        }
    }

    public static void checkInfo(String[] deviceInfo, Button confirmButton) {
        boolean infoComplete = true;
        for (String info : deviceInfo) {
            if (TextUtils.isEmpty(info)) infoComplete = false;}
        confirmButton.setEnabled(infoComplete);
    }

    public static View findViewByTag(ViewGroup parent, String tag) {
        if (parent == null || tag == null) {
            return null;
        }

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            if (tag.equals(child.getTag())) {
                return child;
            }

            if (child instanceof ViewGroup) {
                View foundView = findViewByTag((ViewGroup) child, tag);
                if (foundView != null) {
                    return foundView;
                }
            }
        }
        return null;
    }

    public static View findViewByTag(String tag, LinearLayout roomDevicesLayout) {
        return findViewByTag(roomDevicesLayout, tag);
    }
}
