package com.alan.homeautomationapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;


public class IntroActivity extends AppCompatActivity {

    private DBhandler dbHandler;                          // Database handler instance
    private final Handler introHandler = new Handler();   // IntroActivity finish handler
    private boolean databaseUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Initialize database instance
        dbHandler = DBhandler.getInstance(this);
        dbHandler.getWritableDatabase();

        // Hide the actionBar
        Objects.requireNonNull(getSupportActionBar()).hide();

        MQTTclient mqttClient = MQTTclient.getInstance();

        mqttClient.connect(new MQTTclient.MqttConnectionCallback() {
            @Override
            public void onSuccess() {
                // Only subscribe when connection is ready
                mqttClient.subscribe("hap/main/database/data", (topic, message) -> {
                    dbHandler.updateDatabase(message);
                    databaseUpdated = true;
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                // Handle connection failure (show dialog, retry, etc.)
            }
        });

        // Check connection to the server
        introHandler.postDelayed(this::checkConnection, 3000);
    }

    public void checkConnection() {
        if(databaseUpdated) {
            TextView messageTextView = findViewById(R.id.messageTextView);
            messageTextView.setText(getResources().getString(R.string.loading_message));
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            ConstraintLayout mainLayout = findViewById(R.id.mainLayout);
            mainLayout.setAlpha(0.25f);

            Dialog connectionDialog = new Dialog(IntroActivity.this);
            connectionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            connectionDialog.setContentView(R.layout.dialog_connection);
            connectionDialog.show();
            connectionDialog.setCanceledOnTouchOutside(false);

            Window roomWindow = connectionDialog.getWindow();
            Objects.requireNonNull(roomWindow).setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT);
            roomWindow.setBackgroundDrawableResource(android.R.color.transparent);

            Button yesButton = connectionDialog.findViewById(R.id.yesButton);
            Button noButton = connectionDialog.findViewById(R.id.noButton);

            yesButton.setOnClickListener(view -> {
                introHandler.postDelayed(this::checkConnection, 5000);
                connectionDialog.dismiss();
                mainLayout.setAlpha(1f);
            });

            noButton.setOnClickListener(view -> {
                connectionDialog.dismiss();
                finish();
            });
        }
    }
}