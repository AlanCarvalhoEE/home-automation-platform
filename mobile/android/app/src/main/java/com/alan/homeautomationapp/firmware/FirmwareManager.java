package com.alan.homeautomationapp.firmware;

import com.alan.homeautomationapp.core.Credentials;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FirmwareManager {

    public static FirmwareData getFirmwareData(String device)
            throws IOException, JSONException {

        URL url = new URL(Credentials.FIRMWARE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        conn.disconnect();

        JSONObject manifest = new JSONObject(response.toString());

        JSONObject deviceInfo = manifest.getJSONObject(device);

        String version = deviceInfo.getString("version");
        String firmwareUrl = deviceInfo.getString("url");

        return new FirmwareData(device, version, firmwareUrl);
    }
}
