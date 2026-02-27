package com.example.flashcardmaster.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShakeDetector(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private val SHAKE_THRESHOLD = 15.0f
    private val SHAKE_TIMEOUT_MS = 500L
    private val SHAKE_COUNT_RESET_TIME_MS = 3000L

    private var lastShakeTime: Long = 0
    private var lastForce: Float = 0f
    private var shakeCount = 0

    private val _shakeEvents = MutableStateFlow(0)
    val shakeEvents: StateFlow<Int> = _shakeEvents.asStateFlow()

    private val _sensorStatus = MutableStateFlow("Initializing...")
    val sensorStatus: StateFlow<String> = _sensorStatus.asStateFlow()

    fun startListening() {
        try {
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
                _sensorStatus.value = "✓ Accelerometer active"
            } else {
                _sensorStatus.value = "✗ No accelerometer found"
            }
        } catch (e: Exception) {
            _sensorStatus.value = "✗ Sensor error: ${e.message}"
        }
    }

    fun stopListening() {
        try {
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val force = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            if (force > SHAKE_THRESHOLD) {
                val now = System.currentTimeMillis()

                if (lastShakeTime + SHAKE_TIMEOUT_MS > now) {
                    return
                }

                if (now - lastShakeTime > SHAKE_COUNT_RESET_TIME_MS) {
                    shakeCount = 0
                }

                lastShakeTime = now
                lastForce = force
                shakeCount++

                vibrate(50)
                _shakeEvents.value = shakeCount
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun vibrate(duration: Long = 50) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (e: Exception) {
            // Vibration not available
        }
    }

    fun gentleTap() = vibrate(30)

    fun successVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 50, 30, 50)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 50, 30, 50), -1)
            }
        } catch (e: Exception) {}
    }

    fun errorVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 100, 50, 100)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 100, 50, 100), -1)
            }
        } catch (e: Exception) {}
    }

    fun triggerHapticFeedback(type: String) {
        when (type) {
            "tap" -> gentleTap()
            "success" -> successVibration()
            "error" -> errorVibration()
            "flip" -> vibrate(20)
            else -> gentleTap()
        }
    }
}