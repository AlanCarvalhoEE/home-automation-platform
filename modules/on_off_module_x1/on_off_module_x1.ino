// Project - HAP - Home Automation Platform 
// Code - ON-OFF module X1
// Author - Alan Carvalho
// Date - 11/08/2025

// Libraries
#include <ESP8266WiFi.h>
#include <PubSubClient.h>

// Device identification
#define ID "23"
#define ROOM "office"
#define TOPIC "lamp"
#define SET_TOPIC "set_state"
#define GET_TOPIC "get_state"

// Pins
#define SWITCH_PIN 12
#define RELAY_PIN 14

// Network parameters
const char* ssid = "WIFI_AL_2.4G";
const char* password = "dracarys";
const char* brokerIP = "192.168.88.11";
const uint16_t brokerPort = 1883;
IPAddress moduleIP (192,168,88,21); 
IPAddress gateway (192,168,88,1); 
IPAddress subnet (255,255,255,0);

// Client instances
WiFiClient tcpClient;
PubSubClient mqttClient(tcpClient);

// Variables
bool loadOn = false;                       // Load state (ON = true, OFF = false)
unsigned long lastSwitchChange = 0;        // Last time the switch state has changed (ms)
const long debounceInterval = 500;         // Interval to debounce the switch state changing (ms)
unsigned long lastConnectionTry = 0;       // Last time the server connection was checked (ms)
const long connectionTryInterval = 1000;   // Interval to check the server connection (ms)
String setTopic;                           // Topic for setting the module state
String getTopic;                           // Topic for retrieving the module state


// Initial setup function
void setup() 
{
  // Configure the GPIO
  pinMode(SWITCH_PIN,INPUT_PULLUP);
  pinMode(RELAY_PIN, OUTPUT);

  // Start the serial port
  Serial.begin(115200);
  digitalWrite(RELAY_PIN, LOW);

  // Configure the WiFi
  setupWifi();

  // Configure the MQTT client
  mqttClient.setServer(brokerIP, brokerPort);
  mqttClient.setCallback(callback);

  // Starts with the load turned OFF
  digitalWrite(RELAY_PIN, loadOn);

  // Build the set topic
  setTopic = "hap/";
  setTopic += ROOM;
  setTopic += "/";
  setTopic += TOPIC;
  setTopic += "/";
  setTopic += SET_TOPIC;

  // Build the get topic
  getTopic = "hap/";
  getTopic += ROOM;
  getTopic += "/";
  getTopic += TOPIC;
  getTopic += "/";
  getTopic += GET_TOPIC;

  // Publish the initial state to the broker
  publishState();

  delay(500);
}

// Loop function
void loop() 
{
  if (!mqttClient.connected()) connectMQTT();
  
  mqttClient.loop();

  // If the switch has been pressed...
  if (digitalRead(SWITCH_PIN) == LOW && ((millis() - lastSwitchChange) > debounceInterval))
  {
    // Change the load state
    loadOn = !loadOn;
    digitalWrite(RELAY_PIN, loadOn);

    // Report the manual action to the broker
    publishState();

    lastSwitchChange = millis();
  }
}

// Function to setup the WiFi
void setupWifi() 
{
  WiFi.mode(WIFI_STA);
  WiFi.config(moduleIP, gateway, subnet);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) 
  {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nWiFi connected, IP address: ");
  Serial.println(WiFi.localIP());
}

// Function to connect to MQTT broker
void connectMQTT() 
{
  if (!mqttClient.connected() && ((millis() - lastConnectionTry) > connectionTryInterval)) 
  {
    Serial.print("Attempting MQTT connection...");

    if (mqttClient.connect(ID)) 
    {
      Serial.println("Connected");
      mqttClient.subscribe(setTopic.c_str());
    } 

    else Serial.printf("failed, rc=%d\n", mqttClient.state());

    lastConnectionTry = millis();
  }
}

// Function to handle received commands
void callback(char* topic, byte* payload, unsigned int length) 
{
  payload[length] = '\0';
  String message = String((char*)payload);
  Serial.printf("Message arrived [%s] %s\n", topic, message.c_str());

  if (message == "ON") 
  {
    digitalWrite(RELAY_PIN, HIGH);
    loadOn = true;
  } 
  else if (message == "OFF") 
  {
    digitalWrite(RELAY_PIN, LOW);
    loadOn = false;
  }

  publishState();
}

// Function to publish the load state
void publishState()
{
  String stateMessage = loadOn ? "ON" : "OFF";
  mqttClient.publish(getTopic.c_str(), stateMessage.c_str(), true);
}