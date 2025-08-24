package com.alan.homeautomationapp;

import android.util.Log;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import java.nio.charset.StandardCharsets;

public class MQTTclient {

    private static MQTTclient instance;
    private final Mqtt3AsyncClient client;
    private final String ID = "11";
    private static final String MQTT_BROKER = "192.168.88.11";
    private static final int port = 1883;

    public MQTTclient() {
        client = MqttClient.builder()
                .useMqttVersion3()
                .identifier(ID)
                .serverHost(MQTT_BROKER)
                .serverPort(port)
                .automaticReconnectWithDefaultConfig()
                .addConnectedListener(context -> Log.d("MQTT_DEBUG", "Connected to broker!"))
                .addDisconnectedListener(context -> Log.d("MQTT_DEBUG", "Disconnected from broker: " + context.getCause()))
                .buildAsync();
    }

    public static synchronized MQTTclient getInstance() {
        if (instance == null) {
            instance = new MQTTclient();
        }
        return instance;
    }

    public void connect() {
        client.connect().whenComplete((ack, throwable) -> {
            if (throwable != null) {
                Log.e("MQTT_DEBUG", "Connection failed", throwable);
            } else {
                Log.d("MQTT_DEBUG", "Connection successful: " + ack);
            }
        });
    }

    public void subscribe(String topic, MqttMessageCallback callback) {
        client.subscribeWith()
                .topicFilter(topic)
                .callback(publish -> {
                    String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    Log.d("MQTT_DEBUG", "Message received on " + topic + ": " + payload);
                    callback.onMessageReceived(topic, payload);
                })
                .send();
    }

    public void publish(String topic, String payload) {
        client.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(payload.getBytes(StandardCharsets.UTF_8))
                .send()
                .whenComplete((pubAck, throwable) -> {
                    if (throwable != null) {
                        Log.e("MQTT_DEBUG", "Publish failed", throwable);
                    } else {
                        Log.d("MQTT_DEBUG", "Message published to " + topic);
                    }
                });
    }

    public void disconnect() {
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