package com.alan.homeautomationapp;

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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Utils {

    static TCPclient tcpClient = TCPclient.getInstance();

    private static int temperature = 20;

    // Function to update rooms from database
    public static void updateRooms(Context context, DBHandler database) {
        Activity activity = (Activity) context;
        Spinner roomSpinner = activity.findViewById(R.id.roomSpinner);
        ArrayAdapter<String> adapter;

        adapter = new ArrayAdapter<>(
                context, R.layout.spinner_item, database.getRoomsList());
        roomSpinner.setAdapter(adapter);
    }

    // Function to update devices from database
    @SuppressLint({"InflateParams", "SetTextI18n"})
    public static void updateDevices(Context context, DBHandler database) {
        Activity activity = (Activity) context;
        Spinner roomSpinner = activity.findViewById(R.id.roomSpinner);
        LinearLayout roomDevicesLayout = activity.findViewById(R.id.roomDevicesLayout);
        List<String> devicesList = database.getDevicesList(roomSpinner.getSelectedItem().toString());

        if (roomDevicesLayout.getChildCount() > 0) roomDevicesLayout.removeAllViews();

        for (int i = 0; i < devicesList.size(); i++) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View vi;
            String deviceType = database.getType(devicesList.get(i));
            String designator = database.getDesignator(devicesList.get(i));

            switch (deviceType) {

                case "Lâmpada":
                    vi = inflater.inflate(R.layout.device_lamp, null);
                    TextView lampNameTextView = vi.findViewById(R.id.lampNameTextView);
                    ToggleButton lampControlToggleButton = vi.findViewById(R.id.lampControlToggleButton);
                    ImageButton lampConfigImageButton = vi.findViewById(R.id.lampConfigImageButton);
                    ImageButton lampDeleteImageButton = vi.findViewById(R.id.lampDeleteImageButton);

                    lampNameTextView.setText(devicesList.get(i));
                    roomDevicesLayout.addView(vi, 0, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    lampControlToggleButton.setTag(designator);

                    if (context instanceof MainActivity) {
                        lampControlToggleButton.setVisibility(View.VISIBLE);
                        lampConfigImageButton.setVisibility(View.INVISIBLE);
                        lampDeleteImageButton.setVisibility(View.INVISIBLE);

                        lampControlToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                            if (isChecked) tcpClient.sendMessage("SET-" + designator + "_ON");
                            else tcpClient.sendMessage("SET-" + designator + "_OFF");
                        });
                    }

                    else if (context instanceof ConfigurationActivity) {
                        lampControlToggleButton.setVisibility(View.INVISIBLE);
                        lampConfigImageButton.setVisibility(View.VISIBLE);
                        lampDeleteImageButton.setVisibility(View.VISIBLE);

                        lampConfigImageButton.setOnClickListener(v -> {
                            String name = (String) lampNameTextView.getText();
                            openDialog(context, database, "dialog_device_config", name);
                        });

                        lampDeleteImageButton.setOnClickListener(v -> {
                            String name = (String) lampNameTextView.getText();
                            openDialog(context, database, "dialog_delete", name);
                        });
                    }

                    break;

                case "Tomada":
                    vi = inflater.inflate(R.layout.device_socket, null);
                    TextView socketNameTextView = vi.findViewById(R.id.socketNameTextView);
                    ToggleButton socketControlToggleButton = vi.findViewById(R.id.socketControlToggleButton);
                    ImageButton socketConfigImageButton = vi.findViewById(R.id.socketConfigImageButton);
                    ImageButton socketDeleteImageButton = vi.findViewById(R.id.socketDeleteImageButton);

                    socketNameTextView.setText(devicesList.get(i));
                    roomDevicesLayout.addView(vi, 0, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    socketControlToggleButton.setTag(designator);

                    if (context instanceof MainActivity) {
                        socketControlToggleButton.setVisibility(View.VISIBLE);
                        socketConfigImageButton.setVisibility(View.INVISIBLE);
                        socketDeleteImageButton.setVisibility(View.INVISIBLE);

                        socketControlToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                            if (isChecked) tcpClient.sendMessage("SET-" + designator + "_ON");
                            else tcpClient.sendMessage("SET-" + designator + "_OFF");
                        });
                    }

                    else if (context instanceof ConfigurationActivity) {
                        socketControlToggleButton.setVisibility(View.INVISIBLE);
                        socketConfigImageButton.setVisibility(View.VISIBLE);
                        socketDeleteImageButton.setVisibility(View.VISIBLE);

                        socketConfigImageButton.setOnClickListener(v -> {
                            String name = (String) socketNameTextView.getText();
                            openDialog(context, database, "dialog_device_config", name);
                        });

                        socketDeleteImageButton.setOnClickListener(v -> {
                            String name = (String) socketNameTextView.getText();
                            openDialog(context, database, "dialog_delete", name);
                        });
                    }

                    break;

                case "Porta":
                    vi = inflater.inflate(R.layout.device_door, null);
                    TextView doorNameTextView = vi.findViewById(R.id.doorNameTextView);
                    ToggleButton doorControlToggleButton = vi.findViewById(R.id.doorControlToggleButton);
                    ImageButton doorConfigImageButton = vi.findViewById(R.id.doorConfigImageButton);
                    ImageButton doorDeleteImageButton = vi.findViewById(R.id.doorDeleteImageButton);

                    doorNameTextView.setText(devicesList.get(i));
                    roomDevicesLayout.addView(vi, 0, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    doorControlToggleButton.setTag(designator);

                    if (context instanceof MainActivity) {
                        doorControlToggleButton.setVisibility(View.VISIBLE);
                        doorConfigImageButton.setVisibility(View.INVISIBLE);
                        doorDeleteImageButton.setVisibility(View.INVISIBLE);

                        doorControlToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                            if (isChecked) tcpClient.sendMessage("SET-" + designator + "_ON");
                            else tcpClient.sendMessage("SET-" + designator + "_OFF");
                        });
                    }

                    else if (context instanceof ConfigurationActivity) {
                        doorControlToggleButton.setVisibility(View.INVISIBLE);
                        doorConfigImageButton.setVisibility(View.VISIBLE);
                        doorDeleteImageButton.setVisibility(View.VISIBLE);

                        doorConfigImageButton.setOnClickListener(v -> {
                            String name = (String) doorNameTextView.getText();
                            openDialog(context, database, "dialog_device_config", name);
                        });

                        doorDeleteImageButton.setOnClickListener(v -> {
                            String name = (String) doorNameTextView.getText();
                            openDialog(context, database, "dialog_delete", name);
                        });
                    }

                    break;

                case "Ar condicionado":
                    vi = inflater.inflate(R.layout.device_air_conditioner, null);
                    TextView airNameTextView = vi.findViewById(R.id.airNameTextView);
                    ToggleButton airControlToggleButton = vi.findViewById(R.id.airControlToggleButton);
                    ImageButton upImageButton = vi.findViewById(R.id.upImageButton);
                    ImageButton downImageButton = vi.findViewById(R.id.downImageButton);
                    EditText temperatureEditText = vi.findViewById(R.id.temperatureEditText);
                    airNameTextView.setText(devicesList.get(i));
                    temperatureEditText.setText(String.valueOf(temperature));
                    roomDevicesLayout.addView(vi, 0, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    airControlToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                        if (isChecked) tcpClient.sendMessage("SET-" + designator + "_ON");
                        else tcpClient.sendMessage("SET-" + designator + "_OFF");
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
                            tcpClient.sendMessage("SET-" + designator + "_T" + temperature);
                        }
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                        public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    });

                    break;
            }
        }
    }

    @SuppressLint("InflateParams")
    public static void openDialog(Context context, DBHandler dbHandler, String dialogType, String deviceName) {
        Activity activity = (Activity) context;
        ConstraintLayout backgroundLayout = activity.findViewById(R.id.mainLayout);
        backgroundLayout.setAlpha(0.25f);

        View dialogView = null;

        if (dialogType.equals("dialog_room_add")) {
            dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_room_add, null);
        }
        else if (dialogType.contains("dialog_device")) {
            dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_device_add, null);
        }
        else if (dialogType.contains("dialog_delete")) {
            dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_delete, null);
        }

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(Objects.requireNonNull(dialogView));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        Objects.requireNonNull(window).setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        Button confirmButton = dialog.findViewById(R.id.yesButton);
        Button cancelButton = dialog.findViewById(R.id.noButton);

        cancelButton.setOnClickListener(view -> {
            backgroundLayout.setAlpha(1f);
            dialog.dismiss();
        });

        if (dialogType.equals("dialog_room_add")) {

            EditText nameEditText = dialog.findViewById(R.id.nameEditText);

            nameEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {}
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    confirmButton.setEnabled(s.length() > 0);
                }
            });

            confirmButton.setOnClickListener(v -> {
                String roomName = nameEditText.getText().toString();
                dbHandler.addRoom(roomName);
                Utils.updateRooms(context, dbHandler);

                String newRoomRequest = "ADD_ROOM-";
                newRoomRequest += roomName;
                tcpClient.sendMessage(newRoomRequest);

                backgroundLayout.setAlpha(1f);
                dialog.dismiss();
            });
        }

        else if (dialogType.contains("dialog_device")) {

            Spinner roomSpinner = activity.findViewById(R.id.roomSpinner);
            EditText nameEditText = dialog.findViewById(R.id.nameEditText);
            RadioGroup typeRadioGroup = dialog.findViewById(R.id.typeRadioGroup);
            EditText designatorEditText = dialog.findViewById(R.id.designatorEditText);
            EditText ipEditText = dialog.findViewById(R.id.ipEditText);

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

            designatorEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    deviceInfo[2] = s.toString();
                    checkInfo(deviceInfo, confirmButton);
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });

            ipEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    deviceInfo[3] = s.toString();
                    checkInfo(deviceInfo, confirmButton);
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });

            if (dialogType.equals("dialog_device_add")) {
                confirmButton.setOnClickListener(view -> {
                    String name = deviceInfo[0];
                    String type = deviceInfo[1];
                    String designator = deviceInfo[2];
                    String ip = deviceInfo[3];

                    String room = roomSpinner.getSelectedItem().toString();

                    dbHandler.addDevice(name, room, type, designator, ip);

                    String newDeviceRequest = "ADD_DEVICE-";
                    newDeviceRequest += name + ",";
                    newDeviceRequest += room + ",";
                    newDeviceRequest += type + ",";
                    newDeviceRequest += designator + ",";
                    newDeviceRequest += ip;

                    tcpClient.sendMessage(newDeviceRequest);
                    updateDevices(context, dbHandler);

                    backgroundLayout.setAlpha(1f);
                    dialog.dismiss();
                });
            }

            else if (dialogType.equals("dialog_device_config")) {
                nameEditText.setText(deviceName);
                designatorEditText.setText(dbHandler.getDesignator(deviceName));
                ipEditText.setText(dbHandler.getAddress(deviceName));
                for (int i = 0; i < typeRadioGroup.getChildCount(); i++) {
                    View child = typeRadioGroup.getChildAt(i);
                    RadioButton radio = (RadioButton) child;
                    if (radio.getText().toString().equals(deviceName)) {
                        radio.setChecked(true);
                    }
                }

                confirmButton.setOnClickListener(view -> {
                    String name = deviceInfo[0];
                    String type = deviceInfo[1];
                    String designator = deviceInfo[2];
                    String ip = deviceInfo[3];

                    String room = roomSpinner.getSelectedItem().toString();

                    dbHandler.updateDevice(name, room, type, designator, ip);

                    String updateDeviceRequest = "UPDATE_DEVICE-";
                    updateDeviceRequest += name + ",";
                    updateDeviceRequest += room + ",";
                    updateDeviceRequest += type + ",";
                    updateDeviceRequest += designator + ",";
                    updateDeviceRequest += ip;

                    tcpClient.sendMessage(updateDeviceRequest);
                    updateDevices(context, dbHandler);

                    backgroundLayout.setAlpha(1f);
                    dialog.dismiss();
                });
            }
        }

        else if (dialogType.equals("dialog_delete")) {

            confirmButton.setOnClickListener(view -> {
                dbHandler.deleteDevice(deviceName);

                String deleteDeviceRequest = "DELETE_DEVICE-";
                deleteDeviceRequest += deviceName;

                tcpClient.sendMessage(deleteDeviceRequest);
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