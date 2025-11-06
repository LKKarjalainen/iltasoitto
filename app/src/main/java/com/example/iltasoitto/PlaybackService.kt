package com.example.iltasoitto

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerNotificationManager

/**
 * A service for playing audio in the background using ExoPlayer.
 * This service uses PlayerNotificationManager to handle the foreground state
 * and media-style notification correctly, which is the robust, modern approach.
 */
@UnstableApi
class PlaybackService : Service() {

    private var player: ExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null

    companion object {
        private const val CHANNEL_ID = "iltasoitto_playback_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val context = this

        // Create a MediaItem from the raw audio resource
        val mediaItem = MediaItem.fromUri("android.resource://$packageName/${R.raw.iltasoitto}")

        // Set up the PlayerNotificationManager, which will create and manage the media notification
        playerNotificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
            .setChannelNameResourceId(R.string.channel_name)
            .setChannelDescriptionResourceId(R.string.channel_description)
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                // This listener handles the service's foreground state.
                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    if (ongoing) {
                        // If the notification is ongoing, the service must be in the foreground.
                        startForeground(notificationId, notification)
                    } else {
                        // If the notification is not ongoing, the service can be removed from the foreground.
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    }
                }

                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    // Stop the service if the user dismisses the notification.
                    stopSelf()
                }
            })
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                // This adapter provides the text and icon for the notification.
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return "Iltasoitto"
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    // This intent is fired when the user taps the notification.
                    val openAppIntent = Intent(context, MainActivity::class.java)
                    return PendingIntent.getActivity(
                        context,
                        0,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }

                override fun getCurrentContentText(player: Player): CharSequence {
                    return "Playing evening signal"
                }

                override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): android.graphics.Bitmap? {
                    // Returning null uses a default icon.
                    return null
                }
            })
            .build()

        // Attach the player to the notification manager
        playerNotificationManager?.setPlayer(player)

        // Set the media item on the player and start playback
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true

        // Add a listener to stop the service when playback completes
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    stopSelf()
                }
            }
        })

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release all resources when the service is destroyed
        playerNotificationManager?.setPlayer(null)
        player?.release()
        player = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
