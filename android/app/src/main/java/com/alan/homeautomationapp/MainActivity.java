package com.alan.homeautomationapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DBHandler dbHandler;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the layout
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        dbHandler = new DBHandler(MainActivity.this);

        // Configure the action bar
        Objects.requireNonNull(this.getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        View actionBarView =getSupportActionBar().getCustomView();

        // Component references
        ImageButton configurationImageButton = actionBarView.findViewById(R.id.configurationImageButton);
        Spinner roomSpinner = findViewById(R.id.roomSpinner);

        updateRooms();
        updateDevices();

        configurationImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfigurationActivity.class);
            startActivity(intent);
        });

        dbHandler.getWritableDatabase();

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

    public void updateRooms() {
        Spinner locationSpinner = findViewById(R.id.roomSpinner);
        ArrayAdapter<String> adapter;

        adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, dbHandler.getRoomsList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        locationSpinner.setAdapter(adapter);
    }

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