package com.example.espweather

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var ipInput: EditText
    private lateinit var refreshBtn: Button
    private lateinit var autoRefreshCheck: CheckBox
    private lateinit var tempText: TextView
    private lateinit var humText: TextView
    private lateinit var statusText: TextView
    private lateinit var lastUpdatedText: TextView
    private lateinit var weatherIcon: TextView   // 👈 new

    private val handler = Handler(Looper.getMainLooper())
    private var autoRefreshRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // View bindings
        ipInput = findViewById(R.id.ipInput)
        refreshBtn = findViewById(R.id.refreshBtn)
        autoRefreshCheck = findViewById(R.id.autoRefreshCheck)
        tempText = findViewById(R.id.tempText)
        humText = findViewById(R.id.humText)
        statusText = findViewById(R.id.statusText)
        lastUpdatedText = findViewById(R.id.lastUpdatedText)
        weatherIcon = findViewById(R.id.weatherIcon)   // 👈 new

        // Load saved IP if any
        val prefs = getSharedPreferences("esp", Context.MODE_PRIVATE)
        val savedIp = prefs.getString("ip", "") ?: ""
        ipInput.setText(savedIp)

        refreshBtn.setOnClickListener {
            val ip = ipInput.text.toString().trim()
            if (ip.isEmpty()) {
                setStatus("Please enter the ESP32 IP address", true)
            } else {
                prefs.edit().putString("ip", ip).apply()
                fetchSensor(ip)
            }
        }

        autoRefreshCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) startAutoRefresh() else stopAutoRefresh()
        }
    }

    // -------------------- Auto refresh --------------------

    private fun startAutoRefresh() {
        val ip = ipInput.text.toString().trim()
        if (ip.isEmpty()) {
            setStatus("Auto-refresh disabled: no IP set", true)
            autoRefreshCheck.isChecked = false
            return
        }

        if (autoRefreshRunnable == null) {
            autoRefreshRunnable = object : Runnable {
                override fun run() {
                    fetchSensor(ipInput.text.toString().trim())
                    handler.postDelayed(this, 10_000) // every 10 seconds
                }
            }
        }
        handler.post(autoRefreshRunnable!!)
    }

    private fun stopAutoRefresh() {
        autoRefreshRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onPause() {
        super.onPause()
        stopAutoRefresh()
    }

    // -------------------- Networking --------------------

    private fun fetchSensor(ip: String) {
        if (ip.isEmpty()) return

        setStatus("Contacting ESP32 at $ip …", false)

        Thread {
            try {
                // IMPORTANT: hit /sensor (this matches the typical ESP32 JSON endpoint)
                val url = URL("http://$ip/sensor")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                    requestMethod = "GET"
                }

                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val reader = BufferedReader(InputStreamReader(stream))
                val sb = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append('\n')
                }

                reader.close()
                conn.disconnect()

                val body = sb.toString().trim()

                runOnUiThread {
                    // Show raw response preview so we SEE what ESP is sending
                    val preview = if (body.length > 160) body.substring(0, 160) + "…" else body
                    statusText.text = "Raw ESP32 response:\n$preview"

                    parseESPData(body)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    setStatus("Network error: ${e.message}", true)
                }
            }
        }.start()
    }

    // -------------------- Parsing --------------------

    private fun parseESPData(data: String) {
        if (data.isBlank()) {
            setStatus("Empty response from ESP32", true)
            return
        }

        val trimmed = data.trim()

        // Case 1: JSON like {"temp":23.5,"hum":60.0}
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                val json = JSONObject(trimmed)
                val temp = json.optDouble("temp", Double.NaN)
                val hum = json.optDouble("hum", Double.NaN)

                if (!temp.isNaN() && !hum.isNaN()) {
                    updateUiWithValues(
                        String.format("%.1f", temp),
                        String.format("%.1f", hum)
                    )
                    return
                }
            } catch (_: Exception) {
                // fall through to other methods
            }
        }

        // Case 2: simple "23.4,55.2" style
        if (data.contains(",")) {
            val parts = data.split(",")
            if (parts.size >= 2) {
                val temp = parts[0].trim()
                val hum = parts[1].trim()
                if (temp.isNotEmpty() && hum.isNotEmpty()) {
                    updateUiWithValues(temp, hum)
                    return
                }
            }
        }

        // Case 3: anything that has at least two numbers in it
        val numberRegex = Regex("[-+]?\\d*\\.?\\d+")
        val nums = numberRegex.findAll(data).map { it.value }.toList()

        if (nums.size >= 2) {
            val temp = nums[0]
            val hum = nums[1]
            updateUiWithValues(temp, hum)
        } else {
            setStatus("Invalid response from ESP32 (no numbers found)", true)
        }
    }

    private fun updateUiWithValues(temp: String, hum: String) {
        // Basic text
        tempText.text = "Temp: $temp °C"
        humText.text = "Humidity: $hum %"
        lastUpdatedText.text = "Last updated: just now"

        // Try to parse temperature as a number to style UI
        val tempValue = temp.toDoubleOrNull()

        if (tempValue != null) {
            when {
                tempValue < 15 -> {
                    // Cold
                    tempText.setTextColor(0xFF64B5F6.toInt())   // light blue
                    weatherIcon.text = "❄"
                }
                tempValue < 28 -> {
                    // Comfortable
                    tempText.setTextColor(0xFF81C784.toInt())   // soft green
                    weatherIcon.text = "☁"
                }
                tempValue < 35 -> {
                    // Warm
                    tempText.setTextColor(0xFFFFF176.toInt())   // yellow
                    weatherIcon.text = "☀"
                }
                else -> {
                    // Hot
                    tempText.setTextColor(0xFFE57373.toInt())   // red
                    weatherIcon.text = "🔥"
                }
            }
        } else {
            // If parsing fails, fall back to white + neutral icon
            tempText.setTextColor(0xFFFFFFFF.toInt())
            weatherIcon.text = "☀"
        }

        setStatus("Updated successfully", false)
    }

    // -------------------- Helpers --------------------

    private fun setStatus(text: String, isError: Boolean) {
        statusText.text = text
        statusText.setTextColor(
            if (isError) 0xFFFFB74D.toInt() else 0xFF90CAF9.toInt()
        )
    }
}
