package com.alan.homeautomationapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private DBHandler dbHandler;        // Database handler instance
    private TCPclient tcpClient;        // TCP client instance

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the layout
        setContentView(R.layout.activity_main);

        // Setup the StrictMode tool
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Initialize database instance
        dbHandler = new DBHandler(MainActivity.this);

        // Initialize TCP client instance
        tcpClient = new TCPclient(this::handleServerMessage);

        // Start the TCP client
        AsyncTask.execute(() -> tcpClient.run());

        // Open the database
        dbHandler.getWritableDatabase();

        // Configure the action bar
        Objects.requireNonNull(this.getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        View actionBarView =getSupportActionBar().getCustomView();

        // Component references
        ImageButton configurationImageButton = actionBarView.findViewById(R.id.configurationImageButton);
        Spinner roomSpinner = findViewById(R.id.roomSpinner);

        updateRooms();

        // Configuration button listener
        configurationImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfigurationActivity.class);
            startActivity(intent);
        });

        // Room selection spinner listener
        roomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                tcpClient.sendMessage("GET_DEVICES_LIST(" + roomSpinner.getSelectedItem().toString() + ")");
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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    // Function to update rooms from database
    public void updateRooms() {
        Spinner locationSpinner = findViewById(R.id.roomSpinner);
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, dbHandler.getRoomsList());
        locationSpinner.setAdapter(adapter);
    }

    // Function to update devices from database
    public void getDevicesList(String message) {
        LinearLayout roomDevicesLayout = findViewById(R.id.roomDevicesLayout);
        ArrayAdapter<String> adapter;

        if (roomDevicesLayout.getChildCount() > 0) roomDevicesLayout.removeAllViews();

        String cleanMessage = message.substring(message.indexOf("-") + 1);
        cleanMessage = cleanMessage.replaceAll("[\\[\\]\",]", "");
        String[] items = cleanMessage.split(" ");
        List<String> devicesList = new ArrayList<>(Arrays.asList(items));

        for (int i = 0; i < devicesList.size(); i++) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View vi;

            String deviceType = dbHandler.getType(devicesList.get(i));
            String deviceDesignator = dbHandler.getDesignator(devicesList.get(i));

            if (deviceType.equals("Iluminação")) {
                vi = inflater.inflate(R.layout.device_lamp, null);
                TextView roomNameTextView = vi.findViewById(R.id.lampNameTextView);
                roomNameTextView.setText(devicesList.get(i));
                roomDevicesLayout.addView(vi, 0, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                ToggleButton lampControlToggleButton = findViewById(R.id.lampControlToggleButton);

                lampControlToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        tcpClient.sendMessage(deviceDesignator + "-ON");
                    }
                    else {
                        tcpClient.sendMessage(deviceDesignator + "-OFF");
                    }
                });
            }

            else if (deviceType.equals("Ar condicionado")) {
                vi = inflater.inflate(R.layout.device_air_conditioner, null);
                TextView airNameTextView = vi.findViewById(R.id.airNameTextView);
                airNameTextView.setText(devicesList.get(i));
                roomDevicesLayout.addView(vi, 0, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                AtomicInteger temperature = new AtomicInteger(20);

                ToggleButton airControlToggleButton = findViewById(R.id.airControlToggleButton);
                ImageButton upImageButton = findViewById(R.id.upImageButton);
                ImageButton downImageButton = findViewById(R.id.downImageButton);
                EditText temperatureEditText = findViewById(R.id.temperatureEditText);

                temperatureEditText.setText(temperature + "°C");

                airControlToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        tcpClient.sendMessage(deviceDesignator + "-ON");
                    }
                    else {
                        tcpClient.sendMessage(deviceDesignator + "-OFF");
                    }
                });

                upImageButton.setOnClickListener(v -> {
                    temperature.getAndIncrement();
                    temperatureEditText.setText(temperature + "°C");
                    tcpClient.sendMessage(deviceDesignator + "-TEMP_UP");
                });

                downImageButton.setOnClickListener(v -> {
                    temperature.getAndDecrement();
                    temperatureEditText.setText(temperature + "°C");
                    tcpClient.sendMessage(deviceDesignator + "-TEMP_DOWN");
                });
            }
        }
    }

    public void handleServerMessage(String message) {
        if (message.contains("DATABASE")) dbHandler.updateDatabase(message);
    }
}