// Project - HAP - Home Automation Platform 
// Code - ON-OFF module
// Author - Alan Carvalho
// Date - 19/03/2026

// Libraries
#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <EEPROM.h>
#include <ArduinoJson.h>
#include <ESP8266httpUpdate.h>
#include "credentials.h"

// Firmware Information
#define TYPE "on-off"
#define FW_VERSION "1.0.2"

// Topics
#define HAP_TOPIC "hap"
#define DEVICE_TOPIC "device"
#define SET_STATE_TOPIC "set_state"
#define GET_STATE_TOPIC "get_state"
#define STATUS_TOPIC "status"
#define ENABLE_LDR_TOPIC "enable_ldr"
#define ADJUST_LDR_TOPIC "adjust_ldr"
#define DISCOVERY_TOPIC "discovery"
#define UPDATE_TOPIC "update"

// Pins
#define SWITCH_PIN 12
#define RELAY_PIN 14
#define LDR_PIN A0

// WiFi and MQTT clients
WiFiClient tcpClient;
PubSubClient mqttClient(tcpClient);

// Variables
bool loadOn = false;                          // Load state (ON = true, OFF = false)
bool ldrEnabled = false;                      // Defines whether the LDR is enabled or not
bool wifiConnected = false;                   // WiFi connection status (Connected = true, Disconnected = false)
unsigned int ldrReading = 0;                  // LDR reading value (0 to 1023)
unsigned int ldrThreshold = 600;              // Threshold to turn the output ON or OFF based on LDR input 
const unsigned int ldrHisteresis = 50;        // Histeresis to avoid output flickering

// Timing
unsigned long lastWifiCheck = 0;              // Last time the WiFi connection was checked (ms)
const long wifiCheckInterval = 5000;          // Interval to check the WiFi connection (ms)
unsigned long lastBrokerCheck = 0;            // Last time the broker connection was checked (ms)
const long brokerCheckInterval = 5000;        // Interval to check the broker connection (ms)
unsigned long lastDiscoveryPublish = 0;       // Last time the device has published to discovery topic (ms)
const long discoveryPublishInterval = 5000;   // Interval to publish to discovery topic (ms)
unsigned long lastReportPrint = 0;            // Last time the report was printed (ms)
const long reportPrintInterval = 500;         // Interval to print the report (ms)
unsigned long lastSwitchChange = 0;           // Last time the switch state has changed (ms)
const long debounceInterval = 500;            // Interval to debounce the switch state changing (ms)
unsigned long lastLdrCheck = 0;               // Last time the LDR input was checked (ms)
const long ldrCheckInterval = 10000;          // Interval to check the LDR input (ms)

// MQTT Strings
String deviceId;                              // The device's ID
String setStateTopic;                         // Topic for setting the module state
String getStateTopic;                         // Topic for retrieving the module state
String statusTopic;                           // Topic for online/offline status
String enableLdrTopic;                        // Topic for enabling and disabling the LDR
String adjustLdrTopic;                        // Topic for adjusting the LDR threshold
String discoveryTopic;                        // Topic for device discovery capability
String updateTopic;                           // Topic for firmware update over HTTP

// OTA Callbacks
void update_started() {
  Serial.println("OTA: Started");
}
void update_finished() {
  Serial.println("OTA: Finished");
}
void update_progress(int cur, int total) {
  Serial.printf("OTA: %d / %d bytes\n", cur, total);
}
void update_error(int err) {
  Serial.printf("OTA: Error %d\n", err);
}

// Initial Setup
void setup() {

  // Start the serial port
  Serial.begin(115200);
  delay(3000);

  // Configure the GPIO
  pinMode(SWITCH_PIN, INPUT_PULLUP);
  pinMode(RELAY_PIN, OUTPUT);
  pinMode(LDR_PIN, INPUT);

  // Starts with the load turned OFF
  digitalWrite(RELAY_PIN, loadOn);

  // Start the EEPROM with 16 bytes
  EEPROM.begin(16);

  // Update the LDR variables from EEPROM
  EEPROM.get(2, ldrEnabled);
  EEPROM.get(0, ldrThreshold);
  if (ldrThreshold == 0xFFFF || ldrThreshold > 1023) ldrThreshold = 600;

  //Configure the WiFi
  WiFi.mode(WIFI_STA);

  // Configure the MQTT client
  mqttClient.setServer(brokerIP, brokerPort);
  mqttClient.setCallback(callback);

  generateDeviceId();   // Generates the device ID
  buildTopics();        // Builds all the relevant topics
}

// Loop
void loop() {
  
  checkWifiConnection();      // Check the WiFi connection status
  checkBrokerConnection();    // Check the MQTT connection status

  if (mqttClient.connected()) mqttClient.loop();  // Whatch MQTT topics

  if (!ldrEnabled) checkSwitch();   // Check switch changes
  if (ldrEnabled) checkLDR();       // Check LDR changes

  publishDiscovery(); // Publish to discovery topic
  printReport();      // Print relevant data
}

// Function to generate the device ID
void generateDeviceId() {

  deviceId = WiFi.macAddress();
  deviceId.replace(":", "");
  deviceId.toLowerCase();
}

// Function to build the topics
void buildTopics() {

  String base = String(HAP_TOPIC) + "/" + DEVICE_TOPIC + "/" + deviceId + "/";

  setStateTopic = base + SET_STATE_TOPIC;
  getStateTopic = base + GET_STATE_TOPIC;
  statusTopic = base + STATUS_TOPIC;
  enableLdrTopic = base + ENABLE_LDR_TOPIC;
  adjustLdrTopic = base + ADJUST_LDR_TOPIC;
  updateTopic = base + UPDATE_TOPIC;

  discoveryTopic = String(HAP_TOPIC) + "/" + DISCOVERY_TOPIC + "/" + deviceId;
}

// Function to setup WiFi
void checkWifiConnection() {

  if ((millis() - lastWifiCheck) > wifiCheckInterval) {

    if (WiFi.status() == WL_CONNECTED) {
      wifiConnected = true;
    } else {
      WiFi.begin(ssid, password);
      wifiConnected = false;
    }

    lastWifiCheck = millis();
  }
}

// Function to setup the broker connection
void checkBrokerConnection() {

  if (wifiConnected && ((millis() - lastBrokerCheck) > brokerCheckInterval)) {

    if (!mqttClient.connected()) {

      if (mqttClient.connect(deviceId.c_str(), statusTopic.c_str(), 1, true, "OFFLINE")) {

        mqttClient.subscribe(setStateTopic.c_str());
        mqttClient.subscribe(enableLdrTopic.c_str());
        mqttClient.subscribe(adjustLdrTopic.c_str());
        mqttClient.subscribe(updateTopic.c_str());

        mqttClient.publish(statusTopic.c_str(), "ONLINE", true);

        publishDiscovery();
        publishState();
      }
    }

    lastBrokerCheck = millis();
  }
}

// Function to handle received MQTT commands
void callback(char* topic, byte* payload, unsigned int length) {

  payload[length] = '\0';
  String message = String((char*)payload);

  if (strcmp(topic, updateTopic.c_str()) == 0) {

    StaticJsonDocument<128> doc;

    if (!deserializeJson(doc, message)) {
      String version = doc["version"];
      performOTA(version);
    }
    return;
  }

  if (strcmp(topic, setStateTopic.c_str()) == 0) {
    loadOn = (message == "ON");
    digitalWrite(RELAY_PIN, loadOn);
  }

  if (strcmp(topic, enableLdrTopic.c_str()) == 0) {
    ldrEnabled = (message == "ENABLE");
    EEPROM.put(2, ldrEnabled);
    EEPROM.commit();
  }

  if (strcmp(topic, adjustLdrTopic.c_str()) == 0) {
    ldrThreshold = message.toInt();
    EEPROM.put(0, ldrThreshold);
    EEPROM.commit();
  }

  publishState();
}

// Function to perform firmware update
void performOTA(String version) {

  if (version == FW_VERSION) {
    Serial.println("OTA: Already running this version.");
    return;
  }

  if (WiFi.status() != WL_CONNECTED) return;

  mqttClient.publish(statusTopic.c_str(), "UPDATING", true);
  delay(200);
  mqttClient.disconnect();
  delay(500);

  char url[128];
  snprintf(url, sizeof(url),
         "http://%s:%d/firmware/%s_v%s.bin",
         FW_SERVER_IP,
         FW_SERVER_PORT,
         TYPE,
         version.c_str());

  ESPhttpUpdate.setLedPin(LED_BUILTIN, LOW);
  ESPhttpUpdate.setClientTimeout(30000);

  ESPhttpUpdate.onStart(update_started);
  ESPhttpUpdate.onEnd(update_finished);
  ESPhttpUpdate.onProgress(update_progress);
  ESPhttpUpdate.onError(update_error);

  WiFiClient client;
  t_httpUpdate_return ret = ESPhttpUpdate.update(client, url);

  switch (ret) {

    case HTTP_UPDATE_FAILED:
      Serial.printf("OTA Failed (%d): %s\n",
        ESPhttpUpdate.getLastError(),
        ESPhttpUpdate.getLastErrorString().c_str());
      break;

    case HTTP_UPDATE_NO_UPDATES:
      Serial.println("No Update Available");
      break;

    case HTTP_UPDATE_OK:
      Serial.println("Update Successful");
      break;
  }
}

// Function to check the switch state
void checkSwitch() {

  if (digitalRead(SWITCH_PIN) == LOW &&
      ((millis() - lastSwitchChange) > debounceInterval)) {

    loadOn = !loadOn;
    digitalWrite(RELAY_PIN, loadOn);
    publishState();
    lastSwitchChange = millis();
  }
}

// Function to check the LDR state
void checkLDR() {

  if ((millis() - lastLdrCheck) > ldrCheckInterval) {

    ldrReading = analogRead(LDR_PIN);

    if (!loadOn && (ldrReading > (ldrThreshold + ldrHisteresis))) {
      loadOn = true;
      digitalWrite(RELAY_PIN, HIGH);
    }
    else if (loadOn && (ldrReading < (ldrThreshold - ldrHisteresis))) {
      loadOn = false;
      digitalWrite(RELAY_PIN, LOW);
    }

    publishState();
    lastLdrCheck = millis();
  }
}

// Function to publish device state
void publishState() {

  StaticJsonDocument<192> doc;

  doc["fw_version"] = FW_VERSION;
  doc["load_status"] = loadOn ? "ON" : "OFF";
  doc["ldr_status"] = ldrEnabled ? "ENABLED" : "DISABLED";
  doc["ldr_threshold"] = ldrThreshold;
  doc["ldr_value"] = ldrReading;

  char buffer[192];
  serializeJson(doc, buffer);

  mqttClient.publish(getStateTopic.c_str(), buffer, true);
}

// Function to publish to the discovery topic
void publishDiscovery() {

  if ((millis() - lastDiscoveryPublish) > discoveryPublishInterval) {
    
    StaticJsonDocument<96> doc;

    doc["id"] = deviceId;
    doc["type"] = TYPE;

    char buffer[96];
    serializeJson(doc, buffer);

    mqttClient.publish(discoveryTopic.c_str(), buffer, false);

    lastDiscoveryPublish = millis();
  }
}

// Function to print relevant variables
void printReport() {

  if ((millis() - lastReportPrint) > reportPrintInterval) {

    Serial.print("Device type: ");
    Serial.println(TYPE);

    Serial.print("Firmware Version: ");
    Serial.println(FW_VERSION);

    Serial.print("WiFi Status: "); 
    if (WiFi.status() == WL_CONNECTED) {
      Serial.print("Connected with IP address ");
      Serial.println(WiFi.localIP());
    }
    else (Serial.println("Disconnected"));

    Serial.print("MQTT Status: ");
    if (mqttClient.connected()) Serial.println("Connected");
    else (Serial.println("Disconnected"));

    Serial.print("Switch Status: ");
    if (digitalRead(SWITCH_PIN) == LOW) Serial.println("Pressed");
    else Serial.println("Not pressed");

    Serial.print("LDR Status: ");
    if (ldrEnabled) Serial.println("Enabled");
    else Serial.println("Disabled");

    Serial.print("Light Threshold: ");
    Serial.println(ldrThreshold);

    Serial.print("Light Intensity: ");
    Serial.println(ldrReading);

    Serial.print("Load Status: ");
    if (loadOn) Serial.println("ON");
    else Serial.println("OFF");

    Serial.println("");
    lastReportPrint = millis();
  }
}