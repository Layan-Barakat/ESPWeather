#include <WiFi.h>
#include <WebServer.h>
#include "DHT.h"

#define DHTPIN 4     
#define DHTTYPE DHT11

const char* ssid     = "[Internet name]";
const char* password = "[Put your password]";

DHT dht(DHTPIN, DHTTYPE);
WebServer server(80);

void handleRoot() {
  server.send(200, "text/plain", "ESP32 is alive. Try /sensor");
}

void handleSensor() {
  float h = dht.readHumidity();
  float t = dht.readTemperature(); // Celsius

  if (isnan(h) || isnan(t)) {
    server.send(500, "application/json", "{\"error\":\"DHT read failed\"}");
    return;
  }

  String json = String("{\"temp\":") + String(t, 1) + ",\"hum\":" + String(h, 1) + "}";
  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.send(200, "application/json", json);
}

void setup() {
  Serial.begin(115200);
  delay(1000);
  dht.begin();

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();
  Serial.print("Connected! IP: ");
  Serial.println(WiFi.localIP());

  server.on("/", handleRoot);
  server.on("/sensor", handleSensor);
  server.begin();
  Serial.println("HTTP server started");
}

void loop() {
  server.handleClient();
}
