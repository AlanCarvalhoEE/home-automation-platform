// Project - HAP - Home Automation Platform 
// Code - ON-OFF module
// Author - Alan Carvalho
// Date - 08/05/2024


// Libraries
#include <ESP8266WiFi.h>
#include <Arduino.h>

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

// TCP client instance
WiFiClient tcpClient;   

// Variables
bool loadOn = false;                        // Load state (ON = true, OFF = false)
bool switchState = false;                   // Switch state (OPEN = true, CLOSED = false)
unsigned long lastSwitchCheck = 0;          // Last time the switch state was checked (ms)  
const long switchCheckInterval = 200;       // Interval to check the switch state (ms)
unsigned long lastConnectionCheck = 0;      // Last tiime the server connection was checked (ms)
const long connectionCheckInterval = 1000;  // Interval to check the server connection (ms)


// Initial setup function
void setup() 
{
  // Configure the GPIO
  pinMode(RELAY_PIN, OUTPUT);
  pinMode(SWITCH_PIN, INPUT_PULLUP);

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

  // Read the actual switch state
  switchState = !digitalRead(SWITCH_PIN);
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

  // Check the switch status
  if ((millis() - lastSwitchCheck) > switchCheckInterval)
  {
    // If the switch state has changed, change the load state
    if (digitalRead(SWITCH_PIN) == switchState)
    {
      switchState = !digitalRead(SWITCH_PIN);
      loadOn = switchState;
      digitalWrite(RELAY_PIN, loadOn);

      // Report the manual action to the server
      String message = "MANUAL-";
      message += DESIGNATOR;
      if (loadOn) message += "_ON";
      else message += "_OFF";
      tcpClient.print(message);

      lastSwitchCheck = millis();
    }
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
}