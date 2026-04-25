package com.hap.homeautomation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.hap.homeautomation.core.Credentials;
import com.hap.homeautomation.core.DatabaseManager;
import com.hap.homeautomation.core.DialogManager;
import com.hap.homeautomation.core.MQTTclient;
import com.hap.homeautomation.R;
import com.hap.homeautomation.core.VPNmanager;
import com.hap.homeautomation.rooms.RoomData;
import com.hap.homeautomation.rooms.RoomManager;
import com.hap.homeautomation.devices.DeviceData;
import com.hap.homeautomation.devices.DeviceManager;

import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

// Class responsible for running the intro activity (intro screen)
public class IntroActivity extends AppCompatActivity {

    private DatabaseManager databaseManager;                  // DatabaseManager instance
    MQTTclient mqttClient;                                    // MQTTclient instance
    DeviceManager deviceManager;                              // DeviceManager instance
    private final Handler introHandler = new Handler();       // IntroActivity finish handler

    private boolean databaseUpdated = false;                  // Store the database update status
    private boolean waitingUserAction = false;                // Defines if the App is waiting for some user action

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

        // Check VPN connection status
        checkVPNconnection();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(waitingUserAction) {
            waitingUserAction = false;
            checkVPNconnection();
        }
    }

    // Method to connect to MQTT
    public void connectMQTT() {
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
    }

    // Method to check the VPN installation
    public void checkVPNconnection() {

        new Thread(() -> {

            boolean vpnInstallation = VPNmanager.checkInstallation(this);
            boolean vpnConnection = VPNmanager.checkVPNconnection(this);
            boolean serverConnection = VPNmanager.checkserverConnection(
                    Credentials.SERVER_IP, Credentials.SERVER_PORT, 2000);

            TextView messageTextView = findViewById(R.id.messageTextView);

            runOnUiThread(() -> {

                messageTextView.setText(getString(R.string.vpn_check_message));

                introHandler.postDelayed(() -> {

                    if (!vpnInstallation) {

                        DialogManager.openIntroDialog(this,
                                getString(R.string.vpn_installation_error_message), () -> {
                                    waitingUserAction = true;
                                    VPNmanager.openPlayStore(this);
                                }
                        );
                        return;
                    }

                    messageTextView.setText(getString(R.string.server_check_message));

                    introHandler.postDelayed(() -> {

                        if (!vpnConnection) {

                            DialogManager.openIntroDialog(this,
                                    getString(R.string.vpn_connection_error_message), () -> {
                                        waitingUserAction = true;
                                        VPNmanager.openZeroTier(this);
                                    }
                            );
                            return;
                        }

                        messageTextView.setText(getString(R.string.database_check_message));

                        introHandler.postDelayed(() -> {

                            if (!serverConnection) {
                                DialogManager.openIntroDialog(this,
                                        getString(R.string.server_error_message),
                                        this::checkVPNconnection);
                                return;
                            }

                            connectMQTT();
                            introHandler.postDelayed(this::checkDatabase, 2000);

                        }, 1200);
                    }, 1200);
                }, 1200);
            });
        }).start();
    }

    // Method to check the database update status
    public void checkDatabase() {

        if(databaseUpdated) {

            TextView messageTextView = findViewById(R.id.messageTextView);
            messageTextView.setText(getResources().getString(R.string.database_check_message));

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

                    boolean changed = false;

                    if (!fwVersion.equals(device.getFirmwareVersion())) {
                        device.setFirmwareVersion(fwVersion);
                        changed = true;
                    }
                    if (!loadStatus.equals(device.getLoadStatus())) {
                        device.setLoadStatus(loadStatus);
                        changed = true;
                    }
                    if (!ldrStatus.equals(device.getLdrStatus())) {
                        device.setLdrStatus(ldrStatus);
                        changed = true;
                    }
                    if (threshold != device.getLdrThreshold()) {
                        device.setLdrThreshold(threshold);
                        changed = true;
                    }
                    if (ldrValue != device.getLdrValue()) {
                        device.setLdrValue(ldrValue);
                        changed = true;
                    }
                    if (changed) {
                        DeviceManager.getInstance(this).notifyDeviceUpdated(device);
                    }

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

                    if (!status.equals(device.getStatus())) {
                        device.setStatus(status);
                        DeviceManager.getInstance(this).notifyDeviceUpdated(device);
                    }

                } catch (Exception ignored) {}
            });

            // Finish IntroActivity and start MainActivity
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            DialogManager.openIntroDialog(
                    this, getString(R.string.database_error_message), () -> {

            });
        }
    }
}