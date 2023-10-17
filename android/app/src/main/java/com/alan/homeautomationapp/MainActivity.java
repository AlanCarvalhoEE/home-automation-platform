package com.alan.homeautomationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
        tcpClient = TCPclient.getInstance();
        tcpClient.setMessageListener(this::receiveMessage);

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
        LinearLayout roomDevicesLayout = findViewById(R.id.roomDevicesLayout);

        Commom.updateRooms(this, dbHandler, roomSpinner);
        Commom.updateDevices(this, dbHandler, tcpClient, roomSpinner, roomDevicesLayout);

        // Configuration button listener
        configurationImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfigurationActivity.class);
            startActivity(intent);
        });

        // Room selection spinner listener
        roomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Commom.updateDevices(MainActivity.this, dbHandler, tcpClient, roomSpinner, roomDevicesLayout);
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

    public void receiveMessage(String message) {
        if (message.contains("DATABASE")) dbHandler.updateDatabase(message);
    }
}