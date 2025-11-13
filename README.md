# ESPWeather — Android + ESP32 Weather Monitor

A real-time temperature & humidity monitoring system built with **Android (Kotlin)** and an **ESP32 microcontroller**, featuring a clean UI, auto-refresh, smart JSON parsing, and direct device communication over WiFi.

## Features

### ESP32 Live Sensor Data  
Fetches temperature & humidity via HTTP (`/sensor` endpoint).

### Auto-Refresh Mode  
Automatically polls the ESP32 every 10 seconds.

### Smart Error Handling  
Detects:  
- invalid IPs  
- unreachable ESP  
- malformed sensor data  
- missing values  
- network timeouts  

### Flexible Parsing  
Supports:  
- JSON (`{"temp":23.5,"hum":60}`)  
- CSV-like (`23.5,60`)  
- Raw values (`Temp: 23.5 C, Humidity: 60%`)  

## Tech Stack

### **Android**
- Kotlin  
- Material Components  
- HttpURLConnection  
- JSON parsing  
- SharedPreferences  

### **ESP32**
- Arduino IDE  
- DHT11 / DHT22 sensor  
- Web server with `/sensor` endpoint returning JSON  

