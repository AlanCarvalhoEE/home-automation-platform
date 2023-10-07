#include <ESP8266WiFi.h>
#include <Arduino.h>
#include <IRrecv.h>
#include <IRremoteESP8266.h>
#include <IRac.h>

#define IR_INPUT_PIN D4           // IR receiver pin
#define IR_OUTPUT_PIN D5          // IR transmiter pin
#define IR_INPUT_TIMEOUT 15       // Timeout while receiving IR input
#define IR_INPUT_MIN_SIZE 12      // IR input minimum packet size
#define IR_INPUT_MAX_SIZE 1024    // IR input maximum packet size
#define IR_INPUT_TOLERANCE 25     // IR input tolerance

// Network parameters
const char* ssid = "AAA";
const char* password = "Dracarys";
const char* host = "192.168.0.110";
const uint16_t port = 5560;

WiFiClient tcpClient;   // TCP client instance

// IR input and output instances
IRrecv irInput(IR_INPUT_PIN, IR_INPUT_MAX_SIZE, IR_INPUT_TIMEOUT, true);
IRac irOutput(IR_OUTPUT_PIN);

decode_results results; // IR input results


void setup() 
{
  Serial.begin(115200);                       // Start the serial port
  while (!Serial) delay(50);                  // Wait until the serial port is open

  WiFi.mode(WIFI_STA);                        // Configure the WiFi mode to STATION
  WiFi.begin(ssid, password);                // Configure the WiFi connection credentials
  tcpClient.setTimeout(50);

  Serial.print("Wait for WiFi... ");

  while (WiFi.status() != WL_CONNECTED)     // Wait until the WiFi interface is ready
  {
    Serial.print(".");
    delay(500);
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  while (!tcpClient.connect(host, port)) {
    Serial.println("connection failed");
    delay(5000);
  }

  delay(500);

  irInput.setUnknownThreshold(IR_INPUT_MIN_SIZE);         // Set the minimum packet size
  irInput.setTolerance(IR_INPUT_TOLERANCE);               // Set the input tolerance
  irInput.enableIRIn();                                   // Enable the IR input

  irOutput.next.protocol = decode_type_t::WHIRLPOOL_AC;   // Set the IR protocol
  irOutput.next.model = 1;                                // Choose model number (for air conditioners with multiple models)
  irOutput.next.mode = stdAc::opmode_t::kCool;            // Set the mode essa COOLING
  irOutput.next.celsius = true;                           // Set the temperature unit as °C
  irOutput.next.degrees = 25;                             // Set the temperature to 25°C
  irOutput.next.fanspeed = stdAc::fanspeed_t::kMedium;    // Set the fan to medium speed
  irOutput.next.swingv = stdAc::swingv_t::kOff;           // Turn off the vertical fun swing
  irOutput.next.swingh = stdAc::swingh_t::kOff;           // Turn off the horizontal fun swing
  irOutput.next.light = false;                            // Turn off the display
  irOutput.next.beep = false;                             // Turn off the beeps
  irOutput.next.econo = false;                            // Turn off the economic mode
  irOutput.next.filter = false;                           // Turn off any filter
  irOutput.next.turbo = false;                            // Turn off turbo mode
  irOutput.next.quiet = false;                            // Turn off quiet mode
  irOutput.next.sleep = -1;                               // Turn off sleep mode
  irOutput.next.clean = false;                            // Turn off all cleaning functions
  irOutput.next.clock = -1;                               // Turn off any clock
  irOutput.next.power = false;                            // Turn off the air conditioner
}


void loop() 
{ 
  //irOutput.next.power = true;
  //irOutput.sendAc();  
  //delay(5000);
  //irOutput.next.power = false;  
  //irOutput.sendAc();  // Send the message.
  //delay(1000);
  
  if(tcpClient.available())
  {
    String command = tcpClient.readString();
    Serial.println(command);
  }
}


// Not used for now
void configureAC()
{  
  if (irInput.decode(&results))
  {
    if (IR_INPUT_TOLERANCE != kTolerance) Serial.printf(D_STR_TOLERANCE " : %d%%\n", IR_INPUT_TOLERANCE);

    Serial.print(resultToHumanReadableBasic(&results));

    String description = IRAcUtils::resultAcToString(&results);
  
    if (description.length()) Serial.println(D_STR_MESGDESC ": " + description);
    yield();

    Serial.println(resultToSourceCode(&results));
    yield();
  }
}
