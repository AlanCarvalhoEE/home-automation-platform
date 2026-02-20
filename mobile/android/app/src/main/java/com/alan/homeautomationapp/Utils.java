package com.alan.homeautomationapp;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.List;

public class Utils {

    private static VideoStreamPlayer streamPlayer;
    private static final String rtspUrl = "rtsp://192.168.88.50:554/avstream/channel=1/stream=1.sdp";

    private static int temperature = 20;
    static MQTTclient mqttClient = MQTTclient.getInstance();

    public static class DeviceState {
        public boolean ldrEnabled = false;
        public int ldrThreshold = -1;
        public int ldrValue = -1;
        public boolean loadOn = false;
    }

    // Function to update rooms from database
    public static void updateRooms(Context context, DBhandler database, Spinner spinner) {
        ArrayAdapter<String> adapter;

        adapter = new ArrayAdapter<>(
                context, R.layout.spinner_item, database.getRoomsList());
        spinner.setAdapter(adapter);
    }

    // Function to update devices from database
    @SuppressLint({"InflateParams", "SetTextI18n"})
    public static void updateDevices(Context context, DBhandler database, String room, LinearLayout layout) {
        Activity activity = (Activity) context;

        List<String> devicesList = database.getDevicesListByRoom(room);

        if (layout.getChildCount() > 0) layout.removeAllViews();

        for (int i = 0; i < devicesList.size(); i++) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View vi;

            String deviceID = database.getDeviceID(devicesList.get(i));
            String deviceType = database.getDeviceType(deviceID);
            String deviceTopic = database.getDeviceTopic(deviceID);
            String roomID = database.getRoomID(room);

            if (deviceType.contains("LAMP")) {
                vi = inflater.inflate(R.layout.device_lamp, null);
                TextView lampNameTextView = vi.findViewById(R.id.lampNameTextView);
                ToggleButton lampControlToggleButton = vi.findViewById(R.id.lampControlToggleButton);
                ImageButton lampConfigImageButton = vi.findViewById(R.id.lampConfigImageButton);
                ImageButton lampDeleteImageButton = vi.findViewById(R.id.lampDeleteImageButton);
                ToggleButton lampLdrToggleButton = vi.findViewById(R.id.lampLdrToggleButton);

                lampNameTextView.setText(devicesList.get(i));
                layout.addView(vi, 0, new ViewGroup.LayoutParams(
                        MATCH_PARENT, WRAP_CONTENT));

                lampControlToggleButton.setTag(deviceID);
                vi.setTag(deviceID);
                DeviceState deviceState = new DeviceState();
                vi.setTag(R.id.lampNameTextView, deviceState);

                String getStateTopic = "hap/";
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
                    setStateTopic += deviceTopic + "/";
                    setStateTopic += "set_state";

                    if (isChecked) mqttClient.publish(setStateTopic, "ON");
                    else mqttClient.publish(setStateTopic, "OFF");
                });

                if (deviceType.contains("LDR")) {
                    lampLdrToggleButton.setVisibility(View.VISIBLE);

                    lampLdrToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                        String enableLdrTopic = "hap/";
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
                layout.addView(vi, 0, new ViewGroup.LayoutParams(
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
                layout.addView(vi, 0, new ViewGroup.LayoutParams(
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
