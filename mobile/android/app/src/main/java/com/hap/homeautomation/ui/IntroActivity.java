package com.hap.homeautomation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hap.homeautomation.R;
import com.hap.homeautomation.core.ConnectionManager;
import com.hap.homeautomation.core.DatabaseManager;
import com.hap.homeautomation.core.DialogManager;
import com.hap.homeautomation.core.MQTTclient;
import com.hap.homeautomation.devices.DeviceData;
import com.hap.homeautomation.devices.DeviceManager;
import com.hap.homeautomation.rooms.RoomData;
import com.hap.homeautomation.rooms.RoomManager;

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
        //mqttClient = MQTTclient.getInstance();

        // Check connection status
        checkConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(waitingUserAction) {
            waitingUserAction = false;
            checkConnection();
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

    // Method to check the connection to server
    public void checkConnection() {

        new Thread(() -> {

            TextView messageTextView = findViewById(R.id.messageTextView);

            runOnUiThread(() ->
                    messageTextView.setText(getString(R.string.connection_check_message)));

            boolean localConnection = ConnectionManager.connectLocal();

            try { Thread.sleep(1500); }
            catch (Exception ignored) {}

            if (localConnection) {

                runOnUiThread(() ->
                        messageTextView.setText(getString(R.string.local_connection_message)));

                try { Thread.sleep(1500); }
                catch (Exception ignored) {}

                mqttClient = MQTTclient.getInstance();
                connectMQTT();

                runOnUiThread(() ->
                        messageTextView.setText(getString(R.string.database_check_message)));

                introHandler.postDelayed(this::checkDatabase, 3000);

                return;
            }

            runOnUiThread(() -> messageTextView.setText(R.string.vpn_check_message));

            try { Thread.sleep(1500); }
            catch (Exception ignored) {}

            boolean vpnInstalled = ConnectionManager.isVPNinstalled(this);

            if (!vpnInstalled) {

                runOnUiThread(() -> DialogManager.openIntroDialog(
                        this, getString(R.string.vpn_installation_error_message),
                        () -> {

                            waitingUserAction = true;
                            ConnectionManager.openPlayStore(this);
                        }));

                return;
            }

            boolean vpnConnected = ConnectionManager.isVPNconnected(this);

            if (vpnConnected) {

                boolean vpnConnection =
                        ConnectionManager.connectVPN(this);

                if (vpnConnection) {

                    runOnUiThread(() ->
                            messageTextView.setText(getString(R.string.vpn_connection_message)));

                    try { Thread.sleep(1500); }
                    catch (Exception ignored) {}

                    mqttClient = MQTTclient.getInstance();
                    connectMQTT();

                    runOnUiThread(() ->
                            messageTextView.setText(getString(R.string.database_check_message)));

                    introHandler.postDelayed(this::checkDatabase, 3000);

                    return;
                }
            }

            runOnUiThread(() -> DialogManager.openIntroDialog(
                    this, getString(R.string.vpn_connection_error_message),
                    () -> {

                        waitingUserAction = true;
                        ConnectionManager.openVPNapp(this);
                    }));
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