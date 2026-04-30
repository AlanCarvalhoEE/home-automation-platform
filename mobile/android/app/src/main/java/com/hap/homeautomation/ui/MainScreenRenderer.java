package com.hap.homeautomation.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ArrayAdapter;

import com.hap.homeautomation.R;
import com.hap.homeautomation.devices.DeviceData;
import com.hap.homeautomation.devices.DeviceManager;
import com.hap.homeautomation.devices.DeviceController;
import com.hap.homeautomation.rooms.RoomManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainScreenRenderer {


    private static final Map<String, View> deviceViews = new HashMap<>();
    private static String currentRoom = "";

    // Method to update the rooms
    public static void updateRooms(Context context, Spinner roomSpinner) {

        List<String> rooms = new ArrayList<>();
        rooms.add(context.getString(R.string.all_rooms_label));
        rooms.addAll(RoomManager.getInstance(context).getAllRoomNames());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, rooms);
        roomSpinner.setAdapter(adapter);
    }

    // Method to render devices
    @SuppressLint("InflateParams")
    public static void renderDevices(
            Context context,
            String room,
            LinearLayout layout) {

        if (room.equals(currentRoom) && layout.getChildCount() > 0) return;
        currentRoom = room;

        layout.removeAllViews();
        deviceViews.clear();

        Collection<DeviceData> devices =
                DeviceManager.getInstance(context).getAllDevices();

        LayoutInflater inflater = LayoutInflater.from(context);

        for (DeviceData device : devices) {

            if (!room.equals(context.getString(R.string.all_rooms_label)) &&
                    !device.getRoom().trim().equalsIgnoreCase(room.trim())) continue;

            View view = addDeviceView(inflater, layout, device);

            if (view != null) {
                layout.addView(view);
                deviceViews.put(device.getId(), view);
                bindDeviceView(view, room, device);
            }
        }
    }

    // Method to add a device view
    private static View addDeviceView(
            LayoutInflater inflater,
            LinearLayout layout,
            DeviceData device) {

        String function = device.getFunction() != null
                ? device.getFunction().toLowerCase()
                : "";

        if (function.contains("lamp")) {
            return inflater.inflate(R.layout.device_lamp, layout, false);
        }

        return null;
    }

    // Method to update a device view
    public static void updateDeviceView(String selectedRoom, DeviceData device) {

        View view = deviceViews.get(device.getId());

        if (view == null) return;

        bindDeviceView(view, selectedRoom, device);
    }

    // Method to bind UI elements
    @SuppressLint("SetTextI18n")
    private static void bindDeviceView(View view, String selectedRoom, DeviceData device) {

        String function = device.getFunction() != null
                ? device.getFunction().toLowerCase()
                : "";

        if (function.contains("lamp")) {

            TextView name = view.findViewById(R.id.lampNameTextView);
            ToggleButton control = view.findViewById(R.id.lampControlToggleButton);
            ToggleButton ldr = view.findViewById(R.id.lampLdrToggleButton);
            TextView deviceRoomView = view.findViewById(R.id.deviceRoomTextView);

            name.setText(device.getName());
            if (selectedRoom.equals("All")) deviceRoomView.setVisibility(View.VISIBLE);
            deviceRoomView.setText(device.getRoom());

            boolean isOn = "ON".equals(device.getLoadStatus());
            boolean ldrEnabled = "ENABLED".equals(device.getLdrStatus());
            boolean online = "ONLINE".equals(device.getStatus());
            boolean hasLdr = function.contains("ldr");

            control.setOnCheckedChangeListener(null);
            ldr.setOnCheckedChangeListener(null);

            if (control.isChecked() != isOn) {
                control.setChecked(isOn);
            }

            if (ldr.isChecked() != ldrEnabled) {
                ldr.setChecked(ldrEnabled);
            }

            ldr.setVisibility(hasLdr ? View.VISIBLE : View.GONE);
            control.setClickable(!ldrEnabled);

            control.setOnCheckedChangeListener((btn, isChecked) -> {
                if (!btn.isPressed()) return;
                DeviceController.setLoad(device.getId(), isChecked);
            });

            if (hasLdr) {
                ldr.setOnCheckedChangeListener((btn, isChecked) -> {
                    if (!btn.isPressed()) return;
                    DeviceController.setLdr(device.getId(), isChecked);
                });
            }

            setViewStatus(view, online, true);
        }
    }

    // Method to set the view status
    private static void setViewStatus(View view, boolean enabled, boolean affectChildren) {

        view.setAlpha(enabled ? 1.0f : 0.5f);

        if (affectChildren && view instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) view;

            for (int i = 0; i < layout.getChildCount(); i++) {
                layout.getChildAt(i).setEnabled(enabled);
            }
        }
    }
}