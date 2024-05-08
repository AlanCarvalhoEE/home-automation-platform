package com.alan.homeautomationapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

public class Commom {

    private static int temperature = 20;

    // Function to update rooms from database
    public static void updateRooms(Context context, DBHandler database, Spinner spinner) {
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(
                context, R.layout.spinner_item, database.getRoomsList());
        spinner.setAdapter(adapter);
    }

    // Function to update devices from database
    @SuppressLint({"InflateParams", "SetTextI18n"})
    public static void updateDevices(Context context, DBHandler database, TCPclient tcpClient, Spinner spinner, LinearLayout layout) {

        List<String> devicesList = database.getDevicesList(spinner.getSelectedItem().toString());

        if (layout.getChildCount() > 0) layout.removeAllViews();

        for (int i = 0; i < devicesList.size(); i++) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View vi;

            String deviceType = database.getType(devicesList.get(i));
            String designator = database.getDesignator(devicesList.get(i));

            switch (deviceType) {

                case "Lâmpada":
                    vi = inflater.inflate(R.layout.device_lamp, null);
                    TextView lampNameTextView = vi.findViewById(R.id.lampNameTextView);
                    ToggleButton lampControlToggleButton = vi.findViewById(R.id.lampControlToggleButton);
                    lampNameTextView.setText(devicesList.get(i));
                    layout.addView(vi, 0, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    lampControlToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                        if (isChecked) tcpClient.sendMessage("CONTROL" + "-" + designator + "_ON");
                        else tcpClient.sendMessage("CONTROL" + "-" + designator + "_OFF");
                    });

                    break;

                case "Tomada":
                    vi = inflater.inflate(R.layout.device_socket, null);
                    TextView socketNameTextView = vi.findViewById(R.id.socketNameTextView);
                    ToggleButton socketControlToggleButton = vi.findViewById(R.id.socketControlToggleButton);
                    socketNameTextView.setText(devicesList.get(i));
                    layout.addView(vi, 0, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    socketControlToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                        if (isChecked) tcpClient.sendMessage(designator + "-ON");
                        else tcpClient.sendMessage(designator + "-OFF");
                    });

                    break;

                case "Porta":
                    vi = inflater.inflate(R.layout.device_door, null);
                    TextView doorNameTextView = vi.findViewById(R.id.doorNameTextView);
                    ToggleButton doorControlToggleButton = vi.findViewById(R.id.doorControlToggleButton);
                    doorNameTextView.setText(devicesList.get(i));
                    layout.addView(vi, 0, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    doorControlToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                        if (isChecked) tcpClient.sendMessage(designator + "-OPEN");
                        else tcpClient.sendMessage(designator + "-CLOSED");
                    });

                    break;

                case "Ar condicionado":
                    vi = inflater.inflate(R.layout.device_air_conditioner, null);
                    TextView airNameTextView = vi.findViewById(R.id.airNameTextView);
                    ToggleButton airControlToggleButton = vi.findViewById(R.id.airControlToggleButton);
                    ImageButton upImageButton = vi.findViewById(R.id.upImageButton);
                    ImageButton downImageButton = vi.findViewById(R.id.downImageButton);
                    EditText temperatureEditText = vi.findViewById(R.id.temperatureEditText);
                    airNameTextView.setText(devicesList.get(i));
                    temperatureEditText.setText(String.valueOf(temperature));
                    layout.addView(vi, 0, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    airControlToggleButton.setOnCheckedChangeListener((toggleButton, isChecked) -> {
                        if (isChecked) tcpClient.sendMessage(designator + "-ON");
                        else tcpClient.sendMessage(designator + "-OFF");
                    });

                    upImageButton.setOnClickListener(view -> {
                        temperature++;
                        temperatureEditText.setText(String.valueOf(temperature));
                    });

                    downImageButton.setOnClickListener(view -> {
                        temperature--;
                        temperatureEditText.setText(String.valueOf(temperature));
                    });

                    temperatureEditText.addTextChangedListener(new TextWatcher() {
                        public void afterTextChanged(Editable s) {
                            temperature = Integer.parseInt(s.toString());
                            tcpClient.sendMessage(designator + "-T" + temperature);
                        }
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                        public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    });

                    break;
            }
        }
    }
}
