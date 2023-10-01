package com.alan.homeautomationapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Objects;

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
        tcpClient = new TCPclient(message -> {});

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

        // Update the activity views from database
        updateRooms();
        updateDevices();

        // Configuration button listener
        configurationImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfigurationActivity.class);
            startActivity(intent);
        });

        // Room selection spinner listener
        roomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updateDevices();
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
        Spinner roomSpinner = findViewById(R.id.roomSpinner);
        ArrayAdapter<String> adapter;

        adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, dbHandler.getRoomsList());

        roomSpinner.setAdapter(adapter);
    }

    // Function to update devices from database
    @SuppressLint("InflateParams")
    public void updateDevices() {
        Spinner roomSpinner = findViewById(R.id.roomSpinner);
        LinearLayout roomDevicesLayout = findViewById(R.id.roomDevicesLayout);
        List<String> devicesList = dbHandler.getDevicesList(roomSpinner.getSelectedItem().toString());

        if (roomDevicesLayout.getChildCount() > 0) roomDevicesLayout.removeAllViews();

        for (int i = 0; i < devicesList.size(); i++) {
            LayoutInflater inflater = (LayoutInflater)
                    getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View vi;

            String deviceType = dbHandler.getType(devicesList.get(i));

            if (deviceType.equals("Iluminação")) {
                vi = inflater.inflate(R.layout.control_lamp, null);
                TextView roomNameTextView = vi.findViewById(R.id.roomNameTextView);
                roomNameTextView.setText(devicesList.get(i));
                roomDevicesLayout.addView(vi, 0, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                ToggleButton lampControlToggleButton = findViewById(R.id.lampControlToggleButton);
                lampControlToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        tcpClient.sendMessage("RELAY1-1");
                    }
                    else {
                        tcpClient.sendMessage("RELAY1-0");
                    }
                });
            }

            else if (deviceType.equals("Ar condicionado")) {
                vi = inflater.inflate(R.layout.control_lamp, null);
                TextView roomNameTextView = vi.findViewById(R.id.roomNameTextView);
                roomNameTextView.setText(devicesList.get(i));
                roomDevicesLayout.addView(vi, 0, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
    }
}