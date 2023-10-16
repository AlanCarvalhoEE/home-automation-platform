package com.alan.homeautomationapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

public class Commom {

    // Function to update rooms from database
    public static void updateRooms(Context context, DBHandler database, Spinner spinner) {
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(
                context, R.layout.spinner_item, database.getRoomsList());
        spinner.setAdapter(adapter);
    }

    // Function to update devices from database
    @SuppressLint("InflateParams")
    public static void updateDevices(Context context, DBHandler database, Spinner spinner, LinearLayout layout) {

        List<String> devicesList = database.getDevicesList(spinner.getSelectedItem().toString());

        if (layout.getChildCount() > 0) layout.removeAllViews();

        for (int i = 0; i < devicesList.size(); i++) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View vi;

            String deviceType = database.getType(devicesList.get(i));
            Log.d("DEVICE_TYPE", deviceType);

            if (deviceType.equals("Lâmpada")) {
                vi = inflater.inflate(R.layout.device_lamp, null);
                TextView roomNameTextView = vi.findViewById(R.id.lampNameTextView);
                roomNameTextView.setText(devicesList.get(i));
                layout.addView(vi, 0, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            else if (deviceType.equals("Ar condicionado")) {
                vi = inflater.inflate(R.layout.device_lamp, null);
                TextView roomNameTextView = vi.findViewById(R.id.lampNameTextView);
                roomNameTextView.setText(devicesList.get(i));
                layout.addView(vi, 0, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
    }
}
