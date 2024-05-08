#include <ESP8266WiFi.h>
#include <Arduino.h>

#define DESIGNATOR "LAMP1"

#define RELAY_PIN 13       
#define SWITCH_PIN 12


// Network parameters
const char* ssid = "Mhnet - Luana_2.4G";
const char* password = "dracarys";
IPAddress serverIP (192,168,88,11); 
IPAddress moduleIP (192,168,88,20); 
IPAddress gateway (192,168,88,1); 
IPAddress subnet (255,255,255,0);
const uint16_t serverPort = 5560;

WiFiClient tcpClient;   // TCP client instance

bool lampOn = false;
bool switchState = false;
unsigned long lastSwitchCheck = 0;        
const long switchCheckInterval = 200;  


void setup() 
{
  pinMode(RELAY_PIN, OUTPUT);
  pinMode(SWITCH_PIN, INPUT_PULLUP);

  Serial.begin(115200);                       // Start the serial port
  while (!Serial) delay(50);                  // Wait until the serial port is open

  WiFi.mode(WIFI_STA);                        // Configure the WiFi mode to STATION
  WiFi.config(moduleIP, gateway, subnet);
  WiFi.begin(ssid, password);                // Configure the WiFi connection credentials
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

  switchState = !digitalRead(SWITCH_PIN);
  delay(500);
}


void loop() 
{   
  if (tcpClient.available())
  {
    String command = tcpClient.readString();

    if (command.indexOf("ON") > -1) 
    {
      digitalWrite(RELAY_PIN, HIGH);
      lampOn = true;
    }
    
    else if (command.indexOf("OFF") > -1)
    {
      digitalWrite(RELAY_PIN, LOW);
      lampOn = false;
    }
  }

  if ((millis() - lastSwitchCheck) > switchCheckInterval)
  {
    if (digitalRead(SWITCH_PIN) != switchState)
    {
      switchState = digitalRead(SWITCH_PIN);
      lampOn = !lampOn;
      digitalWrite(RELAY_PIN, lampOn);

      String message = "MANUAL-";
      message += DESIGNATOR;
      if (lampOn) message += "_ON";
      else message += "_OFF";
      tcpClient.print(message);

      lastSwitchCheck = millis();
    }
  }
}