# ESPWeather — Android + ESP32 Weather Monitor

A real-time temperature & humidity monitoring system built with Android (Kotlin) and an ESP32 microcontroller, featuring a clean UI, auto-refresh, smart JSON parsing, and direct device communication over WiFi.

---

## Features

### ESP32 Live Sensor Data
Fetches temperature & humidity via HTTP (/sensor endpoint).

### Auto-Refresh Mode
Automatically polls the ESP32 every 10 seconds.

### Modern Android UI
Dark theme, smooth layout, Material CardView, color-coded status messages.

### Smart Error Handling
Detects:
- invalid IPs
- unreachable ESP
- malformed sensor data
- missing values
- network timeouts

### Flexible Parsing
Supports:
- JSON → {"temp":23.5,"hum":60}
- CSV → 23.5,60
- Raw text → Temp: 23.5 C, Humidity: 60%

### Tech Stack
Android:
- Kotlin
- Material Components
- HttpURLConnection
- SharedPreferences

ESP32:
- Arduino IDE
- DHT11/DHT22 sensor
- Web server returning JSON

---

## ESP32 API Example

Response from the ESP32 at:

    http://<your-ip>/sensor

Example JSON response:

    {
      "temp": 23.7,
      "hum": 58.2
    }

---

## How It Works

1. User enters the ESP32’s IP address.
2. The Android app sends an HTTP GET request to /sensor.
3. ESP32 returns JSON or raw text.
4. App extracts temperature + humidity.
5. UI updates instantly.
6. Optional auto-refresh repeats every 10 seconds.

---

## Project Structure

    ESPWeather/
     ├── app/
     │   ├── src/main/
     │   │   ├── java/com/example/espweather/MainActivity.kt
     │   │   ├── res/layout/activity_main.xml
     │   │   ├── res/values/
     │   │   └── AndroidManifest.xml
     ├── ESP32/
     │   └── esp32_sensor_code.ino   (optional)

---

## Future Improvements

- Data history charts
- Bluetooth fallback mode
- User-selectable auto-refresh intervals
- Logging + analytics page
- Notifications for extreme temperatures

---

## Author

**Layan Barakat**  
Computer Engineering Student  
University of Birmingham Dubai
