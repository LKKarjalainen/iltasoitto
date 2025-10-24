package com.example.iltasoitto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "BootReceiver.onReceive() action=${intent?.action}")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule the daily alarm after reboot
            scheduleDailyAlarm(context, 20, 0)
            Log.i(TAG, "Rescheduled daily alarm after boot")
        }
    }

    companion object {
        private const val TAG = "Iltasoitto"
    }
}
