package com.alan.homeautomationapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.List;
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

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, dbHandler.getLocations());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);

        configurationImageButton.setOnClickListener(v -> finish());

        locationAddImageButton.setOnClickListener(v -> {
            Dialog locationDialog = new Dialog(this);
            locationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            locationDialog.setContentView(R.layout.location_add_dialog);
            locationDialog.show();
            locationDialog.setCanceledOnTouchOutside(false);
            Window locationWindow = locationDialog.getWindow();
            locationWindow.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

            EditText nameEditText = locationDialog.findViewById(R.id.nameEditText);
            Button confirmButton = locationDialog.findViewById(R.id.confirmButton);
            Button cancelButton = locationDialog.findViewById(R.id.cancelButton);

            nameEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    confirmButton.setEnabled(s.length() > 0);
                }
            });

            confirmButton.setOnClickListener(view -> {
                String locationName = nameEditText.getText().toString();
                dbHandler.addNewLocation(locationName);

                locationDialog.dismiss();
            });

            cancelButton.setOnClickListener(view -> locationDialog.dismiss());
        });

        deviceAddImageButton.setOnClickListener(v -> {
            Dialog deviceDialog = new Dialog(this);
            deviceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            deviceDialog.setContentView(R.layout.device_add_dialog);
            deviceDialog.show();
            deviceDialog.setCanceledOnTouchOutside(false);
            Window deviceWindow = deviceDialog.getWindow();
            deviceWindow.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

            EditText nameEditText = deviceDialog.findViewById(R.id.nameEditText);
            RadioGroup typeRadioGroup = deviceDialog.findViewById(R.id.typeRadioGroup);
            TextView designatorNumberTextView = deviceDialog.findViewById(R.id.designatorNumberTextView);
            Button confirmButton = deviceDialog.findViewById(R.id.confirmButton);
            Button cancelButton = deviceDialog.findViewById(R.id.cancelButton);

            nameEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    confirmButton.setEnabled(s.length() > 0);
                }
            });

            List<String> typeList = dbHandler.getDeviceTypes();
            List<Integer> idList = new ArrayList<>();

            for (int i = 0; i < typeList.size(); i++) {
                RadioButton typeRadio = new RadioButton(this);
                typeRadioGroup.addView(typeRadio);
                //typeRadio.setId(View.generateViewId());
                idList.add(typeRadio.getId());
                typeRadio.setText(typeList.get(i));
                typeRadio.setTextSize(20);
                typeRadio.setHeight(120);
                typeRadio.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.lightGrey));
            }

            typeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton checkedRadioButton = group.findViewById(checkedId);
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked) designatorNumberTextView.setText(dbHandler.getDesignator(checkedRadioButton.getText().toString()));
            });

            confirmButton.setOnClickListener(view -> {
                String name = nameEditText.getText().toString();
                String room = locationSpinner.getSelectedItem().toString();

                int selectedTypeIndex = typeRadioGroup.getCheckedRadioButtonId();
                RadioButton selectedTypeRadio = typeRadioGroup.findViewById(selectedTypeIndex);
                String type = selectedTypeRadio.getText().toString();
                String designator = designatorNumberTextView.getText().toString();

                dbHandler.addNewDevice(name, room, type, designator);

                deviceDialog.dismiss();
            });

            cancelButton.setOnClickListener(view -> deviceDialog.dismiss());
        });

        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

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

}