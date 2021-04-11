// Download library from https://github.com/adafruit/Adafruit_DRV2605_Library

#include <ArduinoBLE.h>
#include <Adafruit_DRV2605.h>
#include <Wire.h>
 
Adafruit_DRV2605 drv;
 
uint8_t effect = 7;
uint32_t wait = 10;

BLEService newService("180A");

BLEWordCharacteristic switchChar("2A57", BLERead | BLEWrite);
const int ledPin = 2;
long previousMillis = 0;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);    // initialize serial communication
  //starts the program if we open the serial monitor.
  
  // Setup for motor driver
  drv.begin();
  drv.selectLibrary(1);
  drv.setMode(DRV2605_MODE_INTTRIG); 

  pinMode(LED_BUILTIN, OUTPUT); // initialize the built-in LED pin to indicate when a central is connected

  //initialize BLE library
  if (!BLE.begin()) {
    Serial.println("starting BLE failed!");
    while (1);
  }
  
 
  BLE.setLocalName("CHDS");
  BLE.setDeviceName("CHDS");
  BLE.setAdvertisedServiceUuid("19B1Af78-E821-596E-4F6C-D10412BC1214");
   //Setting a name that will appear when scanning for bluetooth devices
  BLE.setAdvertisedService(newService);

  newService.addCharacteristic(switchChar); //add characteristics to a service

  BLE.addService(newService);  // adding the service

  switchChar.writeValue(0); //set initial value for characteristics


  BLE.advertise(); //start advertising the service
  Serial.println("Bluetooth device active, waiting for connections...");
}

void loop() {
  // put your main code here, to run repeatedly:
  BLEDevice central = BLE.central(); // wait for a BLE central

  if (central) {  // if a central is connected to the peripheral
    Serial.print("Connected to central: ");

    Serial.println(central.address()); // print the central's BT address

    digitalWrite(LED_BUILTIN, HIGH); // turn on the LED to indicate the connection



    while (central.connected()) { // while the central is connected:
      long currentMillis = millis();

      if (currentMillis - previousMillis >= 200) {
        previousMillis = currentMillis;


        if (switchChar.written()) {

          // Serial.println(switchChar.value(), HEX);
          // Parse vibration signal
          if(switchChar.value() == 0x3256){
              drv.setWaveform(0, 14);  // 14 = Strong Buzz
              drv.setWaveform(1, 0);   // end waveform
              // play the effect!
              drv.go();
             
          }else if(switchChar.value() == 0x3156){
              drv.setWaveform(0, 13);  // 13 = Soft buzz
              drv.setWaveform(1, 0);   // end waveform
              // play the effect!
              drv.go();
             
          }
          
        }

      }
    }

    digitalWrite(LED_BUILTIN, LOW); // when the central disconnects, turn off the LED
    Serial.print("Disconnected from central: ");
    Serial.println(central.address());
  }
}
