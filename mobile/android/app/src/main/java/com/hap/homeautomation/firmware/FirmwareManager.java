package com.hap.homeautomation.firmware;

import com.hap.homeautomation.core.Credentials;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FirmwareManager {

    private static final Map<String, FirmwareData> cache = new HashMap<>();

    public static FirmwareData getFirmwareData(String type)
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

        JSONObject deviceInfo = manifest.getJSONObject(type);

        String version = deviceInfo.getString("version");
        String firmwareUrl = deviceInfo.getString("url");

        return new FirmwareData(type, version, firmwareUrl);
    }

    public interface FirmwareCallback {
        void onLoaded(FirmwareData data);
    }

    public static void getFirmwareDataAsync(String type, FirmwareCallback callback) {

        if (cache.containsKey(type)) {
            callback.onLoaded(cache.get(type));
            return;
        }

        new Thread(() -> {
            try {
                FirmwareData data = getFirmwareData(type);
                cache.put(type, data);

                new android.os.Handler(android.os.Looper.getMainLooper())
                        .post(() -> callback.onLoaded(data));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
