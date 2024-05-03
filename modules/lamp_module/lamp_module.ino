#include <ESP8266WiFi.h>
#include <Arduino.h>


#define ID 1

#define RELAY_S_PIN 12             // Series relay pin
#define RELAY_P_PIN 13             // Parallel relay pin
#define FEEDBACK_PIN 14            // Voltage feedback pin


// Network parameters
const char* ssid = "Mhnet - Luana_2.4G";
const char* password = "dracarys";
IPAddress serverIP (192,168,88,11); 
IPAddress moduleIP (192,168,88,20); 
IPAddress gateway (192,168,88,1); 
IPAddress subnet (255,255,255,0);
const uint16_t serverPort = 5560;

WiFiClient tcpClient;   // TCP client instance


void setup() 
{
  pinMode(RELAY_S_PIN, OUTPUT);
  pinMode(RELAY_P_PIN, OUTPUT);
  pinMode(FEEDBACK_PIN, INPUT);

  Serial.begin(115200);                       // Start the serial port
  while (!Serial) delay(50);                  // Wait until the serial port is open

  WiFi.mode(WIFI_STA);                        // Configure the WiFi mode to STATION
  WiFi.begin(ssid, password);                // Configure the WiFi connection credentials
  WiFi.config(moduleIP, gateway, subnet);
  tcpClient.setTimeout(50);

  Serial.print("Waiting for WiFi... ");

  while (WiFi.status() != WL_CONNECTED)     // Wait until the WiFi interface is ready
  {
    Serial.print(".");
    delay(500);
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  while (!tcpClient.connect(serverIP, serverPort)) {
    Serial.println("connection failed");
    delay(5000);
  }

  delay(500);
}


void loop() 
{   
  if (tcpClient.available())
  {
    String command = tcpClient.readString();
    Serial.println(command);

    if (command.indexOf("ON") > -1) digitalWrite(RELAY_S_PIN, HIGH);
    else if (command.indexOf("OFF") > -1) digitalWrite(RELAY_S_PIN, LOW);
  }
}