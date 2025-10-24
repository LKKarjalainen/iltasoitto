package com.example.iltasoitto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.iltasoitto.ui.theme.IltasoittoTheme
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalTime
import android.media.MediaPlayer


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IltasoittoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        // Keep the existing check for quick manual runs while the app is open
        checkTimeAndPlay(this)

        // Schedule a daily alarm to activate background playback at 20:00
        scheduleDailyAlarm(this, 20, 0)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IltasoittoTheme {
        Greeting("Android")
    }
}

fun checkTimeAndPlay(context: Context) {
    val now = LocalTime.now()

    // Example condition: play sound if it's 8 PM or later
    if (now.hour >= 20) {
        // Create MediaPlayer from an MP3 file in res/raw
            val mediaPlayer = MediaPlayer.create(context, R.raw.iltasoitto)

            // Guard against failures to create the player on some devices/emulators
            mediaPlayer?.let { mp ->
                // Release resources when playback finishes
                mp.setOnCompletionListener { it.release() }
                mp.start() // Start playback
            }
    }
}

/**
 * Schedules a repeating daily alarm at the given hour/minute. When the alarm fires
 * it will send a broadcast that starts the foreground service which plays the sound.
 */
fun scheduleDailyAlarm(context: Context, hour: Int, minute: Int) {
    val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager

    val intent = android.content.Intent(context, AlarmReceiver::class.java)
    val pending = android.app.PendingIntent.getBroadcast(
        context,
        0,
        intent,
        android.app.PendingIntent.FLAG_MUTABLE
    )

    val calendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, hour)
        set(java.util.Calendar.MINUTE, minute)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }

    // If time has already passed for today, schedule for tomorrow
    if (calendar.timeInMillis <= System.currentTimeMillis()) {
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
    }

    // Prefer exact alarms where allowed, but guard against SecurityException on
    // devices/OS versions that require explicit exact-alarm permission.
    try {
        // On Android 12+ the app may not be allowed to schedule exact alarms.
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pending
            )
        } else {
            // Fall back to an inexact alarm to avoid a crash. If you require exact
            // timing, prompt the user to grant exact-alarm permission via settings.
            alarmManager.set(
                android.app.AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pending
            )
        }
    } catch (se: SecurityException) {
        // If we still get a SecurityException, fall back to a non-exact alarm to
        // avoid crashing the app.
        alarmManager.set(
            android.app.AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pending
        )
    }
}


