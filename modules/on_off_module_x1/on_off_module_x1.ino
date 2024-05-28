// Project - HAP - Home Automation Platform 
// Code - ON-OFF module
// Author - Alan Carvalho
// Date - 17/05/2024


// Libraries
#include <ESP8266WiFi.h>
#include <Arduino.h>
#include <ButtonDebounce.h>

// Pins definition
#define RELAY_PIN 13       
#define SWITCH_PIN 12

// Device designator
#define DESIGNATOR "LAMP1"

// Network parameters
const char* ssid = "Mhnet - Luana_2.4G";
const char* password = "dracarys";
IPAddress serverIP (192,168,88,11); 
IPAddress moduleIP (192,168,88,20); 
IPAddress gateway (192,168,88,1); 
IPAddress subnet (255,255,255,0);
const uint16_t serverPort = 5560;

WiFiClient tcpClient;                       // TCP client instance

// Variables
bool loadOn = false;                        // Load state (ON = true, OFF = false)
bool oldSwitchState = false;                // Switch state (OPEN = true, CLOSED = false)
unsigned long lastConnectionCheck = 0;      // Last tiime the server connection was checked (ms)
const long connectionCheckInterval = 1000;  // Interval to check the server connection (ms)

// ButtonDebounce instance for the switch pin
ButtonDebounce switchPin(SWITCH_PIN, 100);


// Initial setup function
void setup() 
{
  // Configure the GPIO
  pinMode(RELAY_PIN, OUTPUT);

  // Start the serial port
  Serial.begin(115200);             
  while (!Serial) delay(50);          

  // Configure and start the WiFi client
  WiFi.mode(WIFI_STA);                        // Set the WiFi mode as station
  WiFi.config(moduleIP, gateway, subnet);     // Configure the client network parameters
  WiFi.begin(ssid, password);                 // Configure the WiFi network credentials
  tcpClient.setTimeout(50);                   // Set the WiFi client timeout

  // Wait until the WiFi interface is ready
  while (WiFi.status() != WL_CONNECTED) delay(100);     

  switchPin.setCallback(switchChanged);

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

  // Update the state of the switch
  switchPin.update();

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
}


// Callback function to handle switch state changes
void switchChanged(const int switchState) {

  // If the switch state has changed
  if (switchState != oldSwitchState) {
    oldSwitchState = switchState;
    loadOn = switchState;
    digitalWrite(RELAY_PIN, loadOn);

    // Report the manual action to the server
    String message = "MANUAL-";
    message += DESIGNATOR;
    if (loadOn) message += "_ON";
    else message += "_OFF";
    tcpClient.print(message);
  }
}