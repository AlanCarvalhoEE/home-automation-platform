package com.alan.homeautomationapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Collection;

// Class responsible for rendering the main screen
public class MainScreenRenderer {

    // Method to update rooms
    public static void updateRooms(Context context, DatabaseManager database, Spinner spinner) {
        ArrayAdapter<String> adapter;

        adapter = new ArrayAdapter<>(
                context, R.layout.spinner_item, RoomManager.getInstance().getAllRoomNames());
        spinner.setAdapter(adapter);
    }

    // Method to update devices
    @SuppressLint({"InflateParams", "SetTextI18n"})
    public static void updateDevices(Context context, String room, LinearLayout layout) {

        layout.removeAllViews();

        Collection<DeviceData> devices = DeviceManager.getInstance().getAllDevices();
        LayoutInflater inflater = LayoutInflater.from(context);

        for (DeviceData device : devices) {
            if (!device.getRoom().trim().equalsIgnoreCase(room.trim())) continue;

            if (device.getType().contains("lamp")) {

                View vi = inflater.inflate(R.layout.device_lamp, layout, false);

                TextView name = vi.findViewById(R.id.lampNameTextView);
                ToggleButton control = vi.findViewById(R.id.lampControlToggleButton);
                ToggleButton ldr = vi.findViewById(R.id.lampLdrToggleButton);

                name.setText(device.getName());

                control.setChecked("ON".equals(device.getLoadStatus()));
                ldr.setChecked("ENABLED".equals(device.getLdrStatus()));

                control.setClickable(!"ENABLED".equals(device.getLdrStatus()));

                control.setOnCheckedChangeListener((btn, isChecked) ->
                        DeviceController.setLoad(device.getId(), isChecked));

                ldr.setOnCheckedChangeListener((btn, isChecked) ->
                        DeviceController.setLdr(device.getId(), isChecked));

                layout.addView(vi);

                boolean online = "ONLINE".equals(device.getStatus());
                setViewStatus(vi, online);
            }
        }
    }

    // Method to set device visibility based on its status (ONLINE or OFFLINE)
    public static void setViewStatus(View view, boolean enabled) {

        view.setEnabled(enabled);

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                setViewStatus(group.getChildAt(i), enabled);
            }
        }

        view.setAlpha(enabled ? 1.0f : 0.5f);
    }
}
