package com.example.iltasoitto

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class NightSoundService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "NightSoundService.onStartCommand() called")
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.app_name) + " - playing night sound")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        mediaPlayer = MediaPlayer.create(this, R.raw.iltasoitto)
        if (mediaPlayer == null) {
            Log.e(TAG, "MediaPlayer.create returned null — audio resource may be missing or unsupported")
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        mediaPlayer?.setOnCompletionListener { mp ->
            Log.i(TAG, "Playback completed — releasing MediaPlayer and stopping service")
            mp.release()
            stopForeground(true)
            stopSelf()
        }
        mediaPlayer?.start()
        Log.i(TAG, "MediaPlayer started playback")

        // Service doesn't need to restart if killed after completion
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "NightSoundService.onDestroy()")
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(CHANNEL_ID, "Night sound", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(chan)
        }
    }

    companion object {
        const val CHANNEL_ID = "iltasoitto_night_channel"
        const val NOTIFICATION_ID = 1
        private const val TAG = "Iltasoitto"
    }
}
