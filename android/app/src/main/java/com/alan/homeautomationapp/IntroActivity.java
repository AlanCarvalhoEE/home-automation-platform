package com.alan.homeautomationapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;


public class IntroActivity extends AppCompatActivity {

    private DBhandler dbHandler;                          // Database handler instance
    private final Handler introHandler = new Handler();   // IntroActivity finish handler

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

        mqttClient.connect();
        mqttClient.subscribe("hap/main/database/data", (topic, message) ->
                dbHandler.updateDatabase(message));

        // Check connection to the server
        introHandler.postDelayed(this::checkConnection, 3000);
    }

    public void checkConnection() {
        TextView messageTextView = findViewById(R.id.messageTextView);
        messageTextView.setText(getResources().getString(R.string.loading_message));
        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}