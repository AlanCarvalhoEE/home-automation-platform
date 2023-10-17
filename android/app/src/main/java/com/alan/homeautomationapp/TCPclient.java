package com.alan.homeautomationapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPclient {

    // Server information
    public static final String SERVER_IP = "192.168.0.104";
    public static final int SERVER_PORT = 5560;

    // Variables
    private String serverMessage;
    private onMessageReceived messageListener;
    private boolean running = false;
    private PrintWriter bufferOut;
    private BufferedReader bufferIn;


    // Incoming message listener
    public TCPclient(onMessageReceived listener) {
        messageListener = listener;
    }

    // Function to send messages
    public void sendMessage(String message) {
        if (bufferOut != null && !bufferOut.checkError()) {
            bufferOut.println(message);
            bufferOut.flush();
        }
    }

    // Function to ping devices
    public boolean pingDevice (String address, int port) {
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(address, port), 5000);
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    // Function to stop the TCP client
    public void stopClient() {
        running = false;

        if (bufferOut != null) {
            bufferOut.flush();
            bufferOut.close();
        }

        messageListener = null;
        bufferIn = null;
        bufferOut = null;
        serverMessage = null;
    }

    // Function to start the TCP client
    public void run() {

        running = true;

        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            try (Socket socket = new Socket(serverAddr, SERVER_PORT)) {
                bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (running) {
                    serverMessage = bufferIn.readLine();
                    if (serverMessage != null && messageListener != null) {
                        messageListener.messageReceived(serverMessage);
                    }
                }
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            }

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }

    // Function to process the received message
    public interface onMessageReceived {
        void messageReceived(String message);
    }
}