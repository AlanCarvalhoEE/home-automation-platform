package com.alan.homeautomationapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

public class ConfigurationActivity extends AppCompatActivity {

    private DBhandler dbHandler;    // Database handler instance
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
        dbHandler = DBhandler.getInstance(this);

        // Initialize TCP client instance
        tcpClient = TCPclient.getInstance();

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
        ImageButton roomAddImageButton = findViewById(R.id.roomAddImageButton);
        ImageButton deviceAddImageButton = findViewById(R.id.deviceAddImageButton);

        // Change the configuration button icon
        configurationImageButton.setImageResource(R.drawable.ic_return);

        // Update the activity views from database
        Utils.updateRooms(this, dbHandler);
        if (roomSpinner.getAdapter().getCount() > 0) {
            Utils.updateDevices(this, dbHandler);
        }

        // Configuration button listener
        configurationImageButton.setOnClickListener(v -> finish());

        // Room add button listener
        roomAddImageButton.setOnClickListener(v ->
                Utils.openDialog(this, dbHandler, "dialog_room_add", null));

        // Device add button listener
        deviceAddImageButton.setOnClickListener(v ->
                Utils.openDialog(this, dbHandler, "dialog_device_add", null));

        roomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Utils.updateDevices(ConfigurationActivity.this, dbHandler);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ConstraintLayout mainLayout = findViewById(R.id.mainLayout);
        mainLayout.setAlpha(1f);
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