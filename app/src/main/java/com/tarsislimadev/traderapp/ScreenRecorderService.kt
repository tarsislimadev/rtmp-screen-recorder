package com.tarsislimadev.traderapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.util.Log
import android.media.projection.MediaProjectionManager
import com.pedro.library.rtmp.RtmpDisplay
import com.pedro.common.ConnectChecker


class ScreenRecorderService : Service(), ConnectChecker {

    private var rtmpDisplay: RtmpDisplay? = null
    private var streamingUrl: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification()
        startForeground(1, notification)
        
        val resultCode = intent?.getIntExtra("RESULT_CODE", -1) ?: -1
        val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("RESULT_DATA", Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra("RESULT_DATA")
        }
        streamingUrl = intent?.getStringExtra("STREAM_URL")

        if (resultCode != -1 && resultData != null && streamingUrl != null) {
            startStreaming(resultCode, resultData!!)
        }
        
        return START_NOT_STICKY
    }

    private fun startStreaming(resultCode: Int, resultData: Intent) {
        rtmpDisplay = RtmpDisplay(this, true, this)
        rtmpDisplay?.setIntentResult(resultCode, resultData)
        
        // Basic configuration (720p, 2Mbps)
        if (rtmpDisplay?.prepareVideo(1280, 720, 30, 2000 * 1024, 0, 320) == true &&
            rtmpDisplay?.prepareAudio(128 * 1024, 44100, true, false, false) == true) {
            rtmpDisplay?.startStream(streamingUrl!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rtmpDisplay?.stopStream()
    }

    // ConnectChecker Implementation
    override fun onConnectionStarted(url: String) { Log.d("Stream", "Started: $url") }
    override fun onConnectionSuccess() { Log.d("Stream", "Success") }
    override fun onConnectionFailed(reason: String) { Log.e("Stream", "Failed: $reason") }
    override fun onNewBitrate(bitrate: Long) { }
    override fun onDisconnect() { Log.d("Stream", "Disconnected") }
    override fun onAuthError() { }
    override fun onAuthSuccess() { }


    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Streaming Live")
            .setContentText("Screen recording is active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Screen Recorder Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "ScreenRecorderServiceChannel"
    }
}
