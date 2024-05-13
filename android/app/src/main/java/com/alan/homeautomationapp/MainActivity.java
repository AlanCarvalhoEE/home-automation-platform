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
import android.widget.ToggleButton;

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
        dbHandler = DBHandler.getInstance(this);

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
        View actionBarView = getSupportActionBar().getCustomView();
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) actionBarView.getParent();
        toolbar.setContentInsetsAbsolute(0,0);

        // Component references
        ImageButton configurationImageButton = actionBarView.findViewById(R.id.configurationImageButton);
        Spinner roomSpinner = findViewById(R.id.roomSpinner);

        Utils.updateRooms(this, dbHandler);
        if (roomSpinner.getAdapter().getCount() > 0) {
            Utils.updateDevices(this, dbHandler);
        }

        // Configuration button listener
        configurationImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfigurationActivity.class);
            startActivity(intent);
        });

        // Room selection spinner listener
        roomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Utils.updateDevices(MainActivity.this, dbHandler);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Spinner roomSpinner = findViewById(R.id.roomSpinner);

        Utils.updateRooms(this, dbHandler);
        if (roomSpinner.getAdapter().getCount() > 0) {
            Utils.updateDevices(this, dbHandler);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void receiveMessage(String changeMessage) {
        if (changeMessage.contains("DATABASE"))
            dbHandler.updateDatabase(changeMessage);

        else if (changeMessage.contains("MANUAL")) {
            int startIndex = changeMessage.indexOf("-") + 1;
            int endIndex = changeMessage.indexOf("_");
            String designator = changeMessage.substring(startIndex, endIndex);

            LinearLayout roomDevicesLayout = findViewById(R.id.roomDevicesLayout);
            View view = Utils.findViewByTag(designator, roomDevicesLayout);

            if (view instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) view;

                toggleButton.setOnCheckedChangeListener(null);
                toggleButton.setChecked(!toggleButton.isChecked());
                toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) tcpClient.sendMessage("SET-" + designator + "_ON");
                    else tcpClient.sendMessage("SET-" + designator + "_OFF");
                });
            }
        }
    }
}