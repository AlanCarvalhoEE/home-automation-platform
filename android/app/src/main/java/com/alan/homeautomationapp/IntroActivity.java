package com.alan.homeautomationapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
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

    private DBHandler dbHandler;                          // Database handler instance
    private TCPclient tcpClient;                          // TCP client instance
    private final Handler introHandler = new Handler();   // IntroActivity finish handler

    private boolean waitingForDatabase = false;           // Indicates whether the database was received

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Initialize database instance
        dbHandler = DBHandler.getInstance(this);

        // Initialize TCP client instance
        tcpClient = TCPclient.getInstance();
        tcpClient.setMessageListener(this::receiveMessage);

        // Start the TCP client
        AsyncTask.execute(() -> tcpClient.run());

        // Open the database
        dbHandler.getWritableDatabase();

        // Hide the actionBar
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Check connection to the server
        introHandler.postDelayed(this::checkConnection, 3000);
    }

    public void receiveMessage(String message) {
        if (message.contains("DATABASE")) {
            dbHandler.updateDatabase(message);
            waitingForDatabase = false;
        }
    }

    public void checkConnection() {
        TextView messageTextView = findViewById(R.id.messageTextView);
        boolean online = tcpClient.pingDevice(TCPclient.SERVER_IP, 22);

        if (online) {
            tcpClient.sendMessage("GET_DATABASE");
            messageTextView.setText(getResources().getString(R.string.loading_message));
            waitingForDatabase = true;
            introHandler.postDelayed(this::checkDatabase, 3000);
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

    public void checkDatabase() {
        if (!waitingForDatabase) {
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

            TextView errorTextView = connectionDialog.findViewById(R.id.typeTextView);
            Button yesButton = connectionDialog.findViewById(R.id.yesButton);
            Button noButton = connectionDialog.findViewById(R.id.noButton);

            errorTextView.setText(getResources().getString(R.string.database_failed_message));

            yesButton.setOnClickListener(view -> {
                mainLayout.setAlpha(1f);
                tcpClient.sendMessage("GET_DATABASE");
                introHandler.postDelayed(this::checkDatabase, 5000);
                connectionDialog.dismiss();
            });

            noButton.setOnClickListener(view -> {
                connectionDialog.dismiss();
                finish();
            });
        }
    }
}