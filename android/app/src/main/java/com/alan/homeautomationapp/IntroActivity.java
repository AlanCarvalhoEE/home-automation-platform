package com.alan.homeautomationapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class IntroActivity extends AppCompatActivity {

    private DBHandler dbHandler;        // Database handler instance
    private TCPclient tcpClient;        // TCP client instance
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Initialize database instance
        dbHandler = new DBHandler(IntroActivity.this);

        // Initialize TCP client instance
        tcpClient = new TCPclient(this::handleServerMessage);

        // Start the TCP client
        AsyncTask.execute(() -> tcpClient.run());

        // Open the database
        dbHandler.getWritableDatabase();

        // Hide the actionBar
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Update the activity views from database
        Handler handler = new Handler();
        handler.postDelayed(() -> tcpClient.sendMessage("GET_DATABASE"), 2500);
    }

    public void handleServerMessage(String message) {
        if (message.contains("DATABASE")) {
            dbHandler.updateDatabase(message);
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}