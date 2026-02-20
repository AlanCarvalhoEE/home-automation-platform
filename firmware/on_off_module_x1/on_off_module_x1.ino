// Project - HAP - Home Automation Platform 
// Code - ON-OFF module X1
// Author - Alan Carvalho
// Date - 10/01/2026

// Libraries
#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <EEPROM.h>
#include <ArduinoJson.h>
#include "credentials.h"

// Device Parameters
#define TYPE "lamp"
#define HAP_TOPIC "hap"
#define DEVICE_TOPIC "device"
#define SET_STATE_TOPIC "set_state"
#define GET_STATE_TOPIC "get_state"
#define STATUS_TOPIC "status"
#define ENABLE_LDR_TOPIC "enable_ldr"
#define ADJUST_LDR_TOPIC "adjust_ldr"
#define DISCOVERY_TOPIC "discovery"

// Pins
#define SWITCH_PIN 12
#define RELAY_PIN 14
#define LDR_PIN A0

// Client instances
WiFiClient tcpClient;
PubSubClient mqttClient(tcpClient);

// Variables
bool loadOn = false;                          // Load state (ON = true, OFF = false)
bool ldrEnabled = false;                      // Defines whether the LDR is enabled or not
bool wifiConnected = false;                   // WiFi connection status (Connected = true, Disconnected = false)
bool brokerConnected = false;                 // Broker connection status (Connected = true, Disconnected = false)
unsigned int ldrReading = 0;                  // LDR reading value (0 to 1023)
unsigned int ldrThreshold = 600;              // Threshold to turn the output ON or OFF based on LDR input 
const unsigned int ldrHisteresis = 50;        // Histeresis to avoid output flickering
unsigned long lastWifiCheck = 0;              // Last time the WiFi connection was checked (ms)
const long wifiCheckInterval = 5000;          // Interval to check the WiFi connection (ms)
unsigned long lastBrokerCheck = 0;            // Last time the broker connection was checked (ms)
const long brokerCheckInterval = 5000;        // Interval to check the broker connection (ms)
unsigned long lastReportPrint = 0;            // Last time the report was printed (ms)
const long reportPrintInterval = 500;         // Interval to print the report (ms)
unsigned long lastSwitchChange = 0;           // Last time the switch state has changed (ms)
const long debounceInterval = 500;            // Interval to debounce the switch state changing (ms)
unsigned long lastLdrCheck = 0;               // Last time the LDR input was checked (ms)
const long ldrCheckInterval = 10000;          // Interval to check the LDR input (ms)
String deviceId;                              // The device's ID
String setStateTopic;                         // Topic for setting the module state
String getStateTopic;                         // Topic for retrieving the module state
String statusTopic;                           // Topic for online/offline status
String enableLdrTopic;                        // Topic for enabling and disabling the LDR
String adjustLdrTopic;                        // Topic for adjusting the LDR threshold
String discoveryTopic;                        // Topic for device discovery capability

// Initial setup function
void setup() {

  // Start the serial port
  Serial.begin(115200);
  delay(5000);

  // Configure the GPIO
  pinMode(SWITCH_PIN,INPUT_PULLUP);
  pinMode(RELAY_PIN, OUTPUT);
  pinMode(LDR_PIN,INPUT);

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

  delay(500);
}

void loop() {

  checkWifiConnection();            // Check the WiFi connection status
  checkBrokerConnection();          // Check the MQTT connection status

  if (mqttClient.connected()) mqttClient.loop();    // Whatch MQTT topics

  if (!ldrEnabled) checkSwitch();    // Check switch changes
  if (ldrEnabled) checkLDR();        // Check LDR changes
  
  printReport();    // Print relevant variables
}

// Function to generate the device ID
void generateDeviceId() {
  
  deviceId = WiFi.macAddress();
  deviceId.replace(":", "");
  deviceId.toLowerCase();
}

// Function to build the topics
void buildTopics() 
{
  String deviceTopic = String(HAP_TOPIC) + "/" + String(DEVICE_TOPIC) + "/" + deviceId + "/";

  setStateTopic = deviceTopic + SET_STATE_TOPIC;
  getStateTopic = deviceTopic + GET_STATE_TOPIC;
  statusTopic = deviceTopic + STATUS_TOPIC;
  enableLdrTopic = deviceTopic + ENABLE_LDR_TOPIC;
  adjustLdrTopic = deviceTopic + ADJUST_LDR_TOPIC;

  discoveryTopic = String(HAP_TOPIC) + "/" + String(DISCOVERY_TOPIC) + "/" + deviceId;
}

// Function to setup the WiFi
void checkWifiConnection() {
  
  if ((millis() - lastWifiCheck) > wifiCheckInterval) {

    if (WiFi.status() == WL_CONNECTED) wifiConnected = true;
    else {
      WiFi.begin(ssid, password);   // Connect to the WiFi network
      wifiConnected = false;
    }

  lastWifiCheck = millis();
  }
}

// Function to setup the WiFi
void checkBrokerConnection() {
  
  if (wifiConnected && ((millis() - lastBrokerCheck) > brokerCheckInterval)) {

    if (mqttClient.connected()) brokerConnected = true;
    
    else {
      if (mqttClient.connect(deviceId.c_str(), statusTopic.c_str(), 1, true, "OFFLINE")) {

        // Subscribe to the appropriate topics
        mqttClient.subscribe(setStateTopic.c_str());
        mqttClient.subscribe(enableLdrTopic.c_str());
        mqttClient.subscribe(adjustLdrTopic.c_str());

        mqttClient.publish(statusTopic.c_str(), "ONLINE", true);

        publishDiscovery();   // Publish to the discovery topic
        publishState();       // Publish the device state
      }
      brokerConnected = false;
    }

  lastBrokerCheck = millis();
  }
}

// Function to handle received commands
void callback(char* topic, byte* payload, unsigned int length) {
  
  payload[length] = '\0';
  String message = String((char*)payload);
  Serial.printf("Message arrived [%s] %s\n", topic, message.c_str());

  if (strcmp(topic, setStateTopic.c_str()) == 0) {
    if (message == "ON") {
      digitalWrite(RELAY_PIN, HIGH);
      loadOn = true;
    } 
    else if (message == "OFF") {
      digitalWrite(RELAY_PIN, LOW);
      loadOn = false;
    }
  }
  
  if (strcmp(topic, enableLdrTopic.c_str()) == 0) {
    if (message == "ENABLE") ldrEnabled = true;
    else if (message == "DISABLE") ldrEnabled = false;

    EEPROM.put(2, ldrEnabled);
    EEPROM.commit();
  }

  if (strcmp(topic, adjustLdrTopic.c_str()) == 0) {
    ldrThreshold = message.toInt();
    EEPROM.put(0, ldrThreshold);
    EEPROM.commit();
  }
  publishState();   // Publish the state to the broker
}

// Function to check the switch state
void checkSwitch() {

  // If the switch has been pressed...
  if (digitalRead(SWITCH_PIN) == LOW && ((millis() - lastSwitchChange) > debounceInterval)) {
    
    // Change the load state
    loadOn = !loadOn;
    digitalWrite(RELAY_PIN, loadOn);

    publishState();   // Publish the state to the broker

    lastSwitchChange = millis();
  }
}

// Function to check the LDR state
void checkLDR() {
  
  // Check every few seconds to avoid flickering
  if ((millis() - lastLdrCheck) > ldrCheckInterval) {
  
    ldrReading = analogRead(LDR_PIN);   // Read the LDR

    // If LDR threshold has been crossed...
    if (!loadOn && (ldrReading > (ldrThreshold + ldrHisteresis))) {
      digitalWrite(RELAY_PIN, HIGH);
      loadOn = true;
    }
    else if (loadOn && (ldrReading < (ldrThreshold - ldrHisteresis))) {
      digitalWrite(RELAY_PIN, LOW);
      loadOn = false;
    }

    publishState();   // Publish the state to the broker
    lastLdrCheck = millis();
  } 
}

// Function to publish the load state
void publishState() {
  
  StaticJsonDocument<128> doc;

  doc["load_status"] = loadOn ? "ON" : "OFF";
  doc["ldr_status"] = ldrEnabled ? "ENABLED" : "DISABLED";
  doc["ldr_threshold"] = ldrThreshold;
  doc["ldr_value"] = ldrReading;

  char buffer[128];
  size_t n = serializeJson(doc, buffer);

  mqttClient.publish(getStateTopic.c_str(), buffer, true);
}

// Function to publish to the discovery topic
void publishDiscovery() {

  StaticJsonDocument<96> doc;

  doc["id"] = deviceId;
  doc["type"] = TYPE;

  char buffer[96];
  serializeJson(doc, buffer);

  mqttClient.publish(discoveryTopic.c_str(), buffer, true);
}

// Function to print relevant variables
void printReport() {

  if ((millis() - lastReportPrint) > reportPrintInterval) {

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