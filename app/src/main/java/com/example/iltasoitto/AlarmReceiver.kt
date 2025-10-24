package com.example.iltasoitto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "AlarmReceiver.onReceive() called. Intent: ${intent?.action}")
        val svcIntent = Intent(context, NightSoundService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(svcIntent)
            } else {
                context.startService(svcIntent)
            }
            Log.i(TAG, "Started NightSoundService from AlarmReceiver")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to start NightSoundService", t)
        }
    }

    companion object {
        private const val TAG = "Iltasoitto"
    }
}
