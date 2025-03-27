// Project - HAP - Home Automation Platform 
// Code - ON-OFF module X1
// Author - Alan Carvalho
// Date - 07/03/2025


// Libraries
#include <ESP8266WiFi.h>
#include <Arduino.h>

// Pins definition
#define SWITCH_PIN 12
#define RELAY_PIN 14       

// Device designator
#define DESIGNATOR "LAMP4"

// Network parameters
const char* ssid = "WIFI_AL_5G";
const char* password = "dracarys";
IPAddress serverIP (192,168,88,11); 
IPAddress moduleIP (192,168,88,23); 
IPAddress gateway (192,168,88,1); 
IPAddress subnet (255,255,255,0);
const uint16_t serverPort = 5560;

WiFiClient tcpClient;                       // TCP client instance

// Variables
bool loadOn = false;                        // Load state (ON = true, OFF = false)
unsigned long lastConnectionCheck = 0;      // Last time the server connection was checked (ms)
const long connectionCheckInterval = 1000;  // Interval to check the server connection (ms)
unsigned long lastSwitchChange = 0;         // Last time the switch state has changed (ms)
const long debounceInterval = 500;          // Interval to debounce the switch state changing (ms)


// Initial setup function
void setup() 
{
  // Configure the GPIO
  pinMode(SWITCH_PIN,INPUT_PULLUP);
  pinMode(RELAY_PIN, OUTPUT);

  // Start the serial port
  Serial.begin(115200);                 

  // Configure and start the WiFi client
  WiFi.mode(WIFI_STA);                        // Set the WiFi mode as station
  WiFi.config(moduleIP, gateway, subnet);     // Configure the client network parameters
  WiFi.begin(ssid, password);                 // Configure the WiFi network credentials
  tcpClient.setTimeout(50);                   // Set the WiFi client timeout    

  digitalWrite(RELAY_PIN, loadOn);            // Starts with the load OFF

  delay(500);
}


// Loop function
void loop() 
{   
  // Check the server connection status
  if (millis() - lastConnectionCheck > connectionCheckInterval)
  {
    // If the client is not connected to the server, try to connect
    if (!tcpClient.connected()) tcpClient.connect(serverIP, serverPort);
    lastConnectionCheck = millis();
  }

  // If a message from the server has been received...
  if (tcpClient.available())
  {
    String command = tcpClient.readString();

    // Turn the load ON
    if (command.indexOf("ON") > -1) 
    {
      digitalWrite(RELAY_PIN, HIGH);
      loadOn = true;
    }
    
    // Turn the load OFF
    else if (command.indexOf("OFF") > -1)
    {
      digitalWrite(RELAY_PIN, LOW);
      loadOn = false;
    }
  }

  // If the switch has been pressed...
  if (digitalRead(SWITCH_PIN) == LOW && ((millis() - lastSwitchChange) > debounceInterval))
  {
    // Change the load state
    loadOn = !loadOn;
    digitalWrite(RELAY_PIN, loadOn);

    // Report the manual action to the server
    String message = "MANUAL-";
    message += DESIGNATOR;
    if (loadOn) message += "_ON";
    else message += "_OFF";
    tcpClient.print(message);

    lastSwitchChange = millis();
  }
}