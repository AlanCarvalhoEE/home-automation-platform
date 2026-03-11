package com.alan.homeautomationapp;

import android.util.Log;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MQTTclient {

    private static MQTTclient instance;
    private final Mqtt3AsyncClient client;

    public static final String ID = "11";

    private final Map<MqttMessageCallback, String> activeSubscriptions = new HashMap<>();

    public MQTTclient() {
        client = MqttClient.builder()
                .useMqttVersion3()
                .identifier(ID)
                .serverHost(Credentials.SERVER_IP)
                .serverPort(Credentials.SERVER_PORT)
                .automaticReconnectWithDefaultConfig()
                .addConnectedListener(context ->
                        Log.d("MQTT_DEBUG", "Connected to broker!"))
                .addDisconnectedListener(context ->
                        Log.d("MQTT_DEBUG", "Disconnected: " + context.getCause()))
                .buildAsync();
    }

    public static synchronized MQTTclient getInstance() {
        if (instance == null) {
            instance = new MQTTclient();
        }
        return instance;
    }

    public void connect(MqttConnectionCallback callback) {
        client.connectWith()
                .cleanSession(true)
                .send()
                .whenComplete((ack, throwable) -> {
                    if (throwable != null) {
                        Log.e("MQTT_DEBUG", "Connection failed", throwable);
                        callback.onFailure(throwable);
                    } else {
                        Log.d("MQTT_DEBUG", "Connection successful");
                        callback.onSuccess();
                    }
                });
    }

    public void subscribe(String topic, MqttMessageCallback callback) {

        client.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {

                    String payload = new String(
                            publish.getPayloadAsBytes(),
                            StandardCharsets.UTF_8
                    );

                    String actualTopic = publish.getTopic().toString();

                    Log.d("MQTT_DEBUG", "Message on " + actualTopic + ": " + payload);

                    callback.onMessageReceived(actualTopic, payload);
                })
                .send();

        activeSubscriptions.put(callback, topic);
    }

    public void subscribeDiscovery(MqttMessageCallback callback) {

        String topic = "hap/discovery/+";

        client.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {

                    String receivedTopic = publish.getTopic().toString();
                    String payload = new String(
                            publish.getPayloadAsBytes(),
                            StandardCharsets.UTF_8
                    );

                    Log.d("MQTT_DISCOVERY",
                            "Discovered: " + receivedTopic + " -> " + payload);

                    callback.onMessageReceived(receivedTopic, payload);
                })
                .send();

        activeSubscriptions.put(callback, topic);
    }

    public void unsubscribeDiscovery(MqttMessageCallback callback) {

        String topic = activeSubscriptions.get(callback);
        if (topic == null) return;

        client.unsubscribeWith()
                .topicFilter(topic)
                .send()
                .whenComplete((ack, throwable) -> {
                    if (throwable != null) {
                        Log.e("MQTT_DEBUG", "Unsubscribe failed", throwable);
                    } else {
                        Log.d("MQTT_DEBUG", "Unsubscribed from " + topic);
                    }
                });

        activeSubscriptions.remove(callback);
    }

    public void publish(String topic, String payload) {

        client.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(payload.getBytes(StandardCharsets.UTF_8))
                .send()
                .whenComplete((ack, throwable) -> {
                    if (throwable != null) {
                        Log.e("MQTT_DEBUG", "Publish failed", throwable);
                    } else {
                        Log.d("MQTT_DEBUG", "Published to " + topic);
                    }
                });
    }

    public void disconnect() {
        Log.d("MQTT_DEBUG", "Disconnecting...");
        client.disconnect();
    }

    public interface MqttConnectionCallback {
        void onSuccess();
        void onFailure(Throwable exception);
    }

    public interface MqttMessageCallback {
        void onMessageReceived(String topic, String message);
    }
}
