package com.alan.homeautomationapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class ConfigurationActivity extends AppCompatActivity {

    private DBHandler dbHandler;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the layout
        setContentView(R.layout.activity_configuration);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        dbHandler = new DBHandler(ConfigurationActivity.this);

        // Configure the action bar
        Objects.requireNonNull(this.getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        View actionBarView =getSupportActionBar().getCustomView();

        // Component references
        ImageButton configurationImageButton = actionBarView.findViewById(R.id.configurationImageButton);
        Spinner locationSpinner = findViewById(R.id.locationSpinner);
        ImageButton locationAddImageButton = findViewById(R.id.locationAddImageButton);
        ImageButton deviceAddImageButton = findViewById(R.id.deviceAddImageButton);

        configurationImageButton.setOnClickListener(v -> finish());

        locationAddImageButton.setOnClickListener(v -> {
            Dialog locationDialog = new Dialog(this);
            locationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            locationDialog.setContentView(R.layout.location_add_dialog);
            locationDialog.show();
            locationDialog.setCanceledOnTouchOutside(false);
            Window locationWindow = locationDialog.getWindow();
            locationWindow.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

            Button confirmButton = locationDialog.findViewById(R.id.confirmButton);
            Button cancelButton = locationDialog.findViewById(R.id.cancelButton);

            confirmButton.setOnClickListener(view -> {
                locationDialog.dismiss();
            });

            cancelButton.setOnClickListener(view -> {
                locationDialog.dismiss();
            });
        });

        deviceAddImageButton.setOnClickListener(v -> {
            Dialog deviceDialog = new Dialog(this);
            deviceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            deviceDialog.setContentView(R.layout.device_add_dialog);
            deviceDialog.show();
            deviceDialog.setCanceledOnTouchOutside(false);
            Window deviceWindow = deviceDialog.getWindow();
            deviceWindow.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

            Button confirmButton = deviceDialog.findViewById(R.id.confirmButton);
            Button cancelButton = deviceDialog.findViewById(R.id.cancelButton);

            confirmButton.setOnClickListener(view -> {
                deviceDialog.dismiss();
            });

            cancelButton.setOnClickListener(view -> {
                deviceDialog.dismiss();
            });
        });

        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

}