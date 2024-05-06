package com.alan.homeautomationapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigurationActivity extends AppCompatActivity {

    private DBHandler dbHandler;    // Database handler instance
    private TCPclient tcpClient;    // TCP client instance

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the layout
        setContentView(R.layout.activity_configuration);

        // Setup the StrictMode tool
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Initialize database instance
        dbHandler = DBHandler.getInstance(this);

        // Initialize TCP client instance
        tcpClient = TCPclient.getInstance();

        // Configure the action bar
        Objects.requireNonNull(this.getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        View actionBarView =getSupportActionBar().getCustomView();

        // Component references
        ImageButton configurationImageButton = actionBarView.findViewById(R.id.configurationImageButton);
        Spinner roomSpinner = findViewById(R.id.roomSpinner);
        ImageButton roomAddImageButton = findViewById(R.id.roomAddImageButton);
        ImageButton deviceAddImageButton = findViewById(R.id.deviceAddImageButton);
        LinearLayout roomDevicesLayout = findViewById(R.id.roomDevicesLayout);

        // Change the configuration button icon
        configurationImageButton.setImageResource(R.drawable.ic_return);

        // Update the activity views from database
        Commom.updateRooms(this, dbHandler, roomSpinner);
        Commom.updateDevices(this, dbHandler, tcpClient, roomSpinner, roomDevicesLayout);

        // Configuration button listener
        configurationImageButton.setOnClickListener(v -> finish());

        // Room add button listener
        roomAddImageButton.setOnClickListener(v -> {
            Dialog roomDialog = new Dialog(this);
            roomDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            roomDialog.setContentView(R.layout.dialog_room_add);
            roomDialog.show();
            roomDialog.setCanceledOnTouchOutside(false);
            Window roomWindow = roomDialog.getWindow();
            Objects.requireNonNull(roomWindow).setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT);
            roomWindow.setBackgroundDrawableResource(android.R.color.transparent);

            EditText nameEditText = roomDialog.findViewById(R.id.nameEditText);
            Button confirmButton = roomDialog.findViewById(R.id.yesButton);
            Button cancelButton = roomDialog.findViewById(R.id.noButton);

            nameEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {}
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    confirmButton.setEnabled(s.length() > 0);
                }
            });

            confirmButton.setOnClickListener(view -> {
                String roomName = nameEditText.getText().toString();
                dbHandler.addNewRoom(roomName);
                Commom.updateRooms(this, dbHandler, roomSpinner);

                String newRoomRequest = "ADD_ROOM-";
                newRoomRequest += roomName;
                tcpClient.sendMessage(newRoomRequest);

                roomDialog.dismiss();
            });

            cancelButton.setOnClickListener(view -> roomDialog.dismiss());
        });

        // Device add button listener
        deviceAddImageButton.setOnClickListener(v -> {
            Dialog deviceDialog = new Dialog(this);
            deviceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            deviceDialog.setContentView(R.layout.device_add_dialog);
            deviceDialog.show();
            deviceDialog.setCanceledOnTouchOutside(false);
            Window deviceWindow = deviceDialog.getWindow();
            Objects.requireNonNull(deviceWindow).setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT);
            deviceWindow.setBackgroundDrawableResource(android.R.color.transparent);

            EditText nameEditText = deviceDialog.findViewById(R.id.nameEditText);
            RadioGroup typeRadioGroup = deviceDialog.findViewById(R.id.typeRadioGroup);
            EditText designatorEditText = deviceDialog.findViewById(R.id.designatorEditText);
            Button confirmButton = deviceDialog.findViewById(R.id.yesButton);
            Button cancelButton = deviceDialog.findViewById(R.id.noButton);

            List<String> typeList = dbHandler.getTypeList();
            List<Integer> idList = new ArrayList<>();
            String[] deviceInfo = new String[3];

            for (int i = 0; i < typeList.size(); i++) {
                RadioButton typeRadio = new RadioButton(this);
                typeRadioGroup.addView(typeRadio);
                idList.add(typeRadio.getId());
                typeRadio.setText(typeList.get(i));
                typeRadio.setTextSize(20);
                typeRadio.setHeight(120);
                typeRadio.setSingleLine();
                typeRadio.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.lightGrey));
            }

            nameEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    deviceInfo[0] = s.toString();
                    if (!TextUtils.isEmpty(deviceInfo[0]) && !TextUtils.isEmpty(deviceInfo[1]) && !TextUtils.isEmpty(deviceInfo[2])) {
                        confirmButton.setEnabled(true);
                    }
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {                }
            });

            typeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton checkedRadioButton = group.findViewById(checkedId);
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked) deviceInfo[1] = checkedRadioButton.getText().toString();
                if (!TextUtils.isEmpty(deviceInfo[0]) && !TextUtils.isEmpty(deviceInfo[1]) && !TextUtils.isEmpty(deviceInfo[2])) {
                    confirmButton.setEnabled(true);
                }
            });

            designatorEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    deviceInfo[2] = s.toString();
                    if (!TextUtils.isEmpty(deviceInfo[0]) && !TextUtils.isEmpty(deviceInfo[1]) && !TextUtils.isEmpty(deviceInfo[2])) {
                        confirmButton.setEnabled(true);
                    }
                }
            });

            confirmButton.setOnClickListener(view -> {
                String name = deviceInfo[0];
                String type = deviceInfo[1];
                String designator = deviceInfo[2];

                String room = roomSpinner.getSelectedItem().toString();
                String address = "192.168.88.20";

                dbHandler.addNewDevice(name, room, type, designator, address);

                String newDeviceRequest = "ADD_DEVICE-";
                newDeviceRequest += name + ",";
                newDeviceRequest += room + ",";
                newDeviceRequest += type + ",";
                newDeviceRequest += designator + ",";
                newDeviceRequest += address;
                Log.d("DEBUG_MESSAGE", newDeviceRequest);
                tcpClient.sendMessage(newDeviceRequest);

                Commom.updateDevices(ConfigurationActivity.this, dbHandler, tcpClient, roomSpinner, roomDevicesLayout);

                deviceDialog.dismiss();
            });

            cancelButton.setOnClickListener(view -> deviceDialog.dismiss());
        });

        roomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Commom.updateDevices(ConfigurationActivity.this, dbHandler, tcpClient, roomSpinner, roomDevicesLayout);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}