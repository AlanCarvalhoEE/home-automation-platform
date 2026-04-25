package com.hap.homeautomation.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.hap.homeautomation.core.DatabaseManager;
import com.hap.homeautomation.core.DialogManager;
import com.hap.homeautomation.core.LanguageManager;
import com.hap.homeautomation.R;
import com.hap.homeautomation.devices.DeviceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Class responsible for running the main activity (main screen)
public class MainActivity extends AppCompatActivity {

    private DeviceManager.DeviceUpdateListener listener;            // DeviceManager listener
    private final Map<String, View> deviceViews = new HashMap<>();  // Views map

    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the layout
        setContentView(R.layout.activity_main);

        // DatabaseManager instance
        DatabaseManager databaseManager = DatabaseManager.getInstance(this);
        databaseManager.getWritableDatabase();

        // Configure the action bar
        Objects.requireNonNull(this.getSupportActionBar()).setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        View actionBarView = getSupportActionBar().getCustomView();
        androidx.appcompat.widget.Toolbar toolbar =
                (androidx.appcompat.widget.Toolbar) actionBarView.getParent();
        toolbar.setContentInsetsAbsolute(0,0);

        // Component references
        ImageButton configurationImageButton = actionBarView.findViewById(R.id.configurationImageButton);
        ImageButton languageImageButton = actionBarView.findViewById(R.id.languageImageButton);
        Spinner roomSpinner = findViewById(R.id.roomSpinner);
        LinearLayout roomDevicesLayout = findViewById(R.id.roomDevicesLayout);

        // Configure the language button
        String currentLanguage = LanguageManager.getLanguage();
        switch (currentLanguage) {
            case "pt": languageImageButton.setImageDrawable(
                    getDrawable(R.drawable.portuguese_image));
                break;
            default: languageImageButton.setImageDrawable(
                    getDrawable(R.drawable.english_image));
        }

        // Update rooms on the spinner
        MainScreenRenderer.updateRooms(this, roomSpinner);
        if (roomSpinner.getAdapter().getCount() > 0) {
            String roomName = roomSpinner.getSelectedItem().toString();

            MainScreenRenderer.renderDevices(this, roomName, roomDevicesLayout);
        }

        // Configuration button listener
        configurationImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfigurationActivity.class);
            startActivity(intent);
        });

        // Language button listener
        languageImageButton.setOnClickListener(v -> {
            DialogManager.openLanguageSelectionDialog(this, currentLanguage);
        });

        // Room selection spinner listener
        roomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                MainScreenRenderer.renderDevices(
                        MainActivity.this,
                        roomSpinner.getSelectedItem().toString(),
                        roomDevicesLayout
                );
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        // Listener to update the screen when there is an update on devices
        listener = device ->
                runOnUiThread(() ->
                        MainScreenRenderer.updateDeviceView(device)
        );
        DeviceManager.getInstance(this).addListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Spinner roomSpinner = findViewById(R.id.roomSpinner);
        LinearLayout roomDevicesLayout = findViewById(R.id.roomDevicesLayout);

        MainScreenRenderer.updateRooms(this, roomSpinner);

        if (roomSpinner.getAdapter() != null &&
                roomSpinner.getAdapter().getCount() > 0 &&
                roomSpinner.getSelectedItem() != null) {

            String roomName = roomSpinner.getSelectedItem().toString();

            MainScreenRenderer.renderDevices(this, roomName, roomDevicesLayout
            );
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

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DeviceManager.getInstance(this).removeListener(listener);
    }
}