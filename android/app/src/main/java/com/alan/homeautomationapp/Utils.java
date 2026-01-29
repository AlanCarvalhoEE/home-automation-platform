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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    // Function to update devices from database
    @SuppressLint({"InflateParams", "SetTextI18n"})
    public static void updateDevices(Context context, DBhandler database) {
        Activity activity = (Activity) context;
        Spinner roomSpinner = activity.findViewById(R.id.roomSpinner);
        LinearLayout roomDevicesLayout = activity.findViewById(R.id.roomDevicesLayout);
        List<String> devicesList = database.getDevicesList(roomSpinner.getSelectedItem().toString());

        if (roomDevicesLayout.getChildCount() > 0) roomDevicesLayout.removeAllViews();

        for (int i = 0; i < devicesList.size(); i++) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View vi;

            String roomTopic = database.getRoomTopic(roomSpinner.getSelectedItem().toString());
            String deviceType = database.getType(devicesList.get(i));
            String deviceID = database.getID(devicesList.get(i));
            String deviceTopic = database.getDeviceTopic(deviceID);

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
                            if (ds.loadOn) lampControlToggleButton.setChecked(true);
                            else lampControlToggleButton.setChecked(false);

                            lampLdrToggleButton.setChecked(ds.ldrEnabled);
                            lampControlToggleButton.setClickable(!ds.ldrEnabled);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

                if (context instanceof MainActivity) {
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

                } else if (context instanceof ConfigurationActivity) {
                    lampControlToggleButton.setVisibility(View.INVISIBLE);
                    lampLdrToggleButton.setVisibility(View.INVISIBLE);
                    lampConfigImageButton.setVisibility(View.VISIBLE);
                    lampDeleteImageButton.setVisibility(View.VISIBLE);

                    lampConfigImageButton.setOnClickListener(v -> {
                        String name = (String) lampNameTextView.getText();
                        openDialog(context, database, "dialog_config_ldr", name);
                    });

                    lampDeleteImageButton.setOnClickListener(v -> {
                        String name = (String) lampNameTextView.getText();
                        openDialog(context, database, "dialog_delete_device", name);
                    });
                }
            }

            else if(deviceType.contains("Wall socket")) {
                vi = inflater.inflate(R.layout.device_socket, null);
                TextView socketNameTextView = vi.findViewById(R.id.socketNameTextView);
                ToggleButton socketControlToggleButton = vi.findViewById(R.id.socketControlToggleButton);
                ImageButton socketConfigImageButton = vi.findViewById(R.id.socketConfigImageButton);
                ImageButton socketDeleteImageButton = vi.findViewById(R.id.socketDeleteImageButton);

                socketNameTextView.setText(devicesList.get(i));
                roomDevicesLayout.addView(vi, 0, new ViewGroup.LayoutParams(
                        MATCH_PARENT, WRAP_CONTENT));

                socketControlToggleButton.setTag(deviceID);

                if (context instanceof MainActivity) {
                    socketControlToggleButton.setVisibility(View.VISIBLE);
                    socketConfigImageButton.setVisibility(View.INVISIBLE);
                    socketDeleteImageButton.setVisibility(View.INVISIBLE);

                    socketControlToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                        //if (isChecked) mqttClient.publish("SET-" + designator + "_ON");
                        //else mqttClient.publish("SET-" + designator + "_OFF");
                    });
                } else if (context instanceof ConfigurationActivity) {
                    socketControlToggleButton.setVisibility(View.INVISIBLE);
                    socketConfigImageButton.setVisibility(View.VISIBLE);
                    socketDeleteImageButton.setVisibility(View.VISIBLE);

                    socketConfigImageButton.setOnClickListener(v -> {
                        String name = (String) socketNameTextView.getText();
                        openDialog(context, database, "dialog_config_device", name);
                    });

                    socketDeleteImageButton.setOnClickListener(v -> {
                        String name = (String) socketNameTextView.getText();
                        openDialog(context, database, "dialog_delete_device", name);
                    });
                }
            }

            else if(deviceType.contains("Door")) {
                vi = inflater.inflate(R.layout.device_door, null);
                TextView doorNameTextView = vi.findViewById(R.id.doorNameTextView);
                ToggleButton doorControlToggleButton = vi.findViewById(R.id.doorControlToggleButton);
                ImageButton doorConfigImageButton = vi.findViewById(R.id.doorConfigImageButton);
                ImageButton doorDeleteImageButton = vi.findViewById(R.id.doorDeleteImageButton);

                doorNameTextView.setText(devicesList.get(i));
                roomDevicesLayout.addView(vi, 0, new ViewGroup.LayoutParams(
                        MATCH_PARENT, WRAP_CONTENT));

                doorControlToggleButton.setTag(deviceID);

                if (context instanceof MainActivity) {
                    doorControlToggleButton.setVisibility(View.VISIBLE);
                    doorConfigImageButton.setVisibility(View.INVISIBLE);
                    doorDeleteImageButton.setVisibility(View.INVISIBLE);

                    doorControlToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                        //if (isChecked) tcpClient.sendMessage("SET-" + designator + "_ON");
                        //else tcpClient.sendMessage("SET-" + designator + "_OFF");
                    });
                } else if (context instanceof ConfigurationActivity) {
                    doorControlToggleButton.setVisibility(View.INVISIBLE);
                    doorConfigImageButton.setVisibility(View.VISIBLE);
                    doorDeleteImageButton.setVisibility(View.VISIBLE);

                    doorConfigImageButton.setOnClickListener(v -> {
                        String name = (String) doorNameTextView.getText();
                        openDialog(context, database, "dialog_config_device", name);
                    });

                    doorDeleteImageButton.setOnClickListener(v -> {
                        String name = (String) doorNameTextView.getText();
                        openDialog(context, database, "dialog_delete_device", name);
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

                if (context instanceof MainActivity) {
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


                } else if (context instanceof ConfigurationActivity) {
                    cameraControlToggleButton.setVisibility(View.INVISIBLE);
                    cameraConfigImageButton.setVisibility(View.VISIBLE);
                    cameraDeleteImageButton.setVisibility(View.VISIBLE);

                    cameraConfigImageButton.setOnClickListener(v -> {
                        String name = (String) cameraNameTextView.getText();
                        openDialog(context, database, "dialog_config_device", name);
                    });

                    cameraDeleteImageButton.setOnClickListener(v -> {
                        String name = (String) cameraNameTextView.getText();
                        openDialog(context, database, "dialog_delete_device", name);
                    });
                }
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

    @SuppressLint("InflateParams")
    public static void openDialog(Context context, DBhandler dbHandler, String dialogType, String deviceName) {
        Activity activity = (Activity) context;
        ConstraintLayout backgroundLayout = activity.findViewById(R.id.mainLayout);
        backgroundLayout.setAlpha(0.25f);

        View dialogView = null;

        if (dialogType.equals("dialog_add_room")) {
            dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_add_room, null);
        }
        else if (dialogType.equals("dialog_delete_room")) {
            dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_delete_room, null);
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

        if (dialogType.equals("dialog_add_room")) {

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
                String roomName = nameEditText.getText().toString();
                String roomTopic = topicEditText.getText().toString();
                dbHandler.addRoom(roomName, roomTopic);
                Utils.updateRooms(context, dbHandler);

                String payload = roomName + "," + roomTopic;
                mqttClient.publish("hap/main/database/add_room", payload);

                backgroundLayout.setAlpha(1f);
                dialog.dismiss();
            });
        }

        else if (dialogType.equals("dialog_delete_room")) {

            confirmButton.setOnClickListener(view -> {
                Spinner roomSpinner = activity.findViewById(R.id.roomSpinner);
                String roomName = roomSpinner.getSelectedItem().toString();
                dbHandler.deleteRoom(roomName);
                Utils.updateRooms(context, dbHandler);

                mqttClient.publish("hap/main/database/delete_room", roomName);

                backgroundLayout.setAlpha(1f);
                dialog.dismiss();
            });
        }


        else if (dialogType.equals("dialog_add_device")) {

            Spinner roomSpinner = activity.findViewById(R.id.roomSpinner);
            EditText nameEditText = dialog.findViewById(R.id.nameEditText);
            RadioGroup typeRadioGroup = dialog.findViewById(R.id.typeRadioGroup);
            EditText idEditText = dialog.findViewById(R.id.idEditText);
            EditText topicEditText = dialog.findViewById(R.id.topicEditText);

            List<String> typeList = dbHandler.getTypeList();
            List<Integer> idList = new ArrayList<>();
            String[] deviceInfo = new String[4];

            for (int i = 0; i < typeList.size(); i++) {
                RadioButton typeRadio = new RadioButton(context);
                typeRadioGroup.addView(typeRadio);
                idList.add(typeRadio.getId());
                typeRadio.setText(typeList.get(i));
                typeRadio.setTextSize(18);
                typeRadio.setHeight(120);
                typeRadio.setSingleLine();
                typeRadio.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.lightGrey));
            }

            nameEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    deviceInfo[0] = s.toString();
                    checkInfo(deviceInfo, confirmButton);
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });

            typeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton checkedRadioButton = group.findViewById(checkedId);
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked) deviceInfo[1] = checkedRadioButton.getText().toString();
                checkInfo(deviceInfo, confirmButton);
            });

            idEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    deviceInfo[2] = s.toString();
                    checkInfo(deviceInfo, confirmButton);
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });

            topicEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    deviceInfo[3] = s.toString();
                    checkInfo(deviceInfo, confirmButton);
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });

            confirmButton.setOnClickListener(view -> {
                String name = deviceInfo[0];
                String type = deviceInfo[1];
                String id = deviceInfo[2];
                String topic = deviceInfo[3];

                String room = roomSpinner.getSelectedItem().toString();

                dbHandler.addDevice(id, name, room, type, topic);

                String payload = id + "," + name + "," + room + "," + type + "," + topic;
                mqttClient.publish("hap/main/database/add_device", payload);
                updateDevices(context, dbHandler);

                backgroundLayout.setAlpha(1f);
                dialog.dismiss();
            });
        }

        else if (dialogType.equals("dialog_delete_device")) {

            confirmButton.setOnClickListener(view -> {
                String deviceID = dbHandler.getID(deviceName);
                dbHandler.deleteDevice(deviceID);

                mqttClient.publish("hap/main/database/delete_device", deviceID);
                updateDevices(context, dbHandler);

                backgroundLayout.setAlpha(1f);
                dialog.dismiss();
            });
        }

        else if (dialogType.equals("dialog_config_ldr")) {

            // Find the device view by device ID stored in DB
            String deviceID = dbHandler.getID(deviceName);
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

                // Populate dialog UI with last known values
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
                String roomTopic = dbHandler.getRoomTopic(roomName);
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
