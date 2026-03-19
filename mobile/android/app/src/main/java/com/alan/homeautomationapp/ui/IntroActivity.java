package com.alan.homeautomationapp.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.alan.homeautomationapp.core.DatabaseManager;
import com.alan.homeautomationapp.core.MQTTclient;
import com.alan.homeautomationapp.R;
import com.alan.homeautomationapp.rooms.RoomData;
import com.alan.homeautomationapp.rooms.RoomManager;
import com.alan.homeautomationapp.devices.DeviceData;
import com.alan.homeautomationapp.devices.DeviceManager;

import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

// Class responsible for running the intro activity (intro screen)
public class IntroActivity extends AppCompatActivity {

    private DatabaseManager databaseManager;                                // DatabaseManager instance
    MQTTclient mqttClient;                                                  // MQTTclient instance
    DeviceManager deviceManager;                                            // DeviceManager instance
    private final Handler introHandler = new Handler();                     // IntroActivity finish handler
    private boolean databaseUpdated = false;                                // Store the database update status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Initialize database instance
        deviceManager = DeviceManager.getInstance(this);
        databaseManager = DatabaseManager.getInstance(this);
        databaseManager.getWritableDatabase();

        // Hide the actionBar
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Initialize the MQTTclient instance
        mqttClient = MQTTclient.getInstance();

        // Connect MQTT
        mqttClient.connect(new MQTTclient.MqttConnectionCallback() {
            @Override
            public void onSuccess() {

                mqttClient.subscribe("hap/main/database/data", (topic, message) -> {
                    databaseManager.updateDatabase(message);
                    databaseUpdated = true;
                });
            }

            @Override
            public void onFailure(Throwable exception) {
            }
        });

        // Check connection to the server
        introHandler.postDelayed(this::checkConnection, 3000);
    }

    // Method to check connection to server
    public void checkConnection() {
        if(databaseUpdated) {       // If the connection is active...
            TextView messageTextView = findViewById(R.id.messageTextView);
            messageTextView.setText(getResources().getString(R.string.loading_message));

            RoomManager roomManager = RoomManager.getInstance(this);
            DeviceManager deviceManager = DeviceManager.getInstance(this);

            mqttClient = MQTTclient.getInstance();
            List<DeviceData> databaseDevices = databaseManager.getAllDevices();
            List<RoomData> databaseRooms = databaseManager.getAllRooms();

            for (RoomData room : databaseRooms) roomManager.addRoom(room, false);
            for (DeviceData device : databaseDevices) deviceManager.addDevice(device, false);

            // Subscribe to "get_state" topic of each device
            mqttClient.subscribe("hap/device/+/get_state", (topic, message) -> {
                try {
                    String[] parts = topic.split("/");
                    if (parts.length < 4) return;
                    String deviceId = parts[2];

                    DeviceData device = this.deviceManager.getDevice(deviceId);
                    if (device == null) return;

                    JSONObject json = new JSONObject(message);

                    String fwVersion   = json.optString("fw_version", null);
                    String loadStatus  = json.optString("load_status", null);
                    String ldrStatus   = json.optString("ldr_status", null);
                    int threshold      = json.optInt("ldr_threshold", -1);
                    int ldrValue       = json.optInt("ldr_value", -1);

                    device.setFirmwareVersion(fwVersion);
                    device.setLoadStatus(loadStatus);
                    device.setLdrStatus(ldrStatus);
                    device.setLdrThreshold(threshold);
                    device.setLdrValue(ldrValue);

                    DeviceManager.getInstance(this).notifyDeviceUpdated(device);

                } catch (Exception ignored) {}
            });

            // Subscribe to "status" topic of each device
            mqttClient.subscribe("hap/device/+/status", (topic, message) -> {
                try {
                    String[] parts = topic.split("/");
                    if (parts.length < 4) return;
                    String deviceId = parts[2];

                    DeviceData device = deviceManager.getDevice(deviceId);

                    if (device == null) return;

                    String status = message.trim();
                    device.setStatus(status);

                    DeviceManager.getInstance(this).notifyDeviceUpdated(device);

                } catch (Exception ignored) {}
            });

            // Finish IntroActivity and start MainActivity
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else {      // If the connection is not active
            ConstraintLayout mainLayout = findViewById(R.id.mainLayout);
            mainLayout.setAlpha(0.25f);

            Dialog connectionDialog = new Dialog(IntroActivity.this);
            connectionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            connectionDialog.setContentView(R.layout.dialog_connection);
            connectionDialog.show();
            connectionDialog.setCanceledOnTouchOutside(false);

            Window roomWindow = connectionDialog.getWindow();
            Objects.requireNonNull(roomWindow).setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT);
            roomWindow.setBackgroundDrawableResource(android.R.color.transparent);

            Button yesButton = connectionDialog.findViewById(R.id.yesButton);
            Button noButton = connectionDialog.findViewById(R.id.noButton);

            yesButton.setOnClickListener(view -> {
                introHandler.postDelayed(this::checkConnection, 5000);
                connectionDialog.dismiss();
                mainLayout.setAlpha(1f);
            });

            noButton.setOnClickListener(view -> {
                connectionDialog.dismiss();
                finish();
            });
        }
    }
}