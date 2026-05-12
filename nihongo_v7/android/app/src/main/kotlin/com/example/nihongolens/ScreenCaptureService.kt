package com.example.nihongolens

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null

    private var virtualDisplay: VirtualDisplay? = null

    private var imageReader: ImageReader? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        startForeground(
            1,
            createNotification()
        )

        startCapture()

        return START_STICKY
    }

    private fun startCapture() {

        try {

            val manager =
                getSystemService(
                    Context.MEDIA_PROJECTION_SERVICE
                ) as MediaProjectionManager

            mediaProjection =
                manager.getMediaProjection(
                    ScreenCaptureHolder.resultCode,
                    ScreenCaptureHolder.data!!
                )

            val metrics = DisplayMetrics()

            val windowManager =
                getSystemService(
                    WINDOW_SERVICE
                ) as WindowManager

            windowManager.defaultDisplay
                .getRealMetrics(metrics)

            imageReader = ImageReader.newInstance(
                metrics.widthPixels,
                metrics.heightPixels,
                PixelFormat.RGBA_8888,
                2
            )

            virtualDisplay =
                mediaProjection?.createVirtualDisplay(
                    "NihongoLensCapture",
                    metrics.widthPixels,
                    metrics.heightPixels,
                    metrics.densityDpi,
                    DisplayManager
                        .VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader?.surface,
                    null,
                    null
                )

            Log.d(
                "ScreenCapture",
                "Capture started"
            )

        } catch (e: Exception) {

            Log.e(
                "ScreenCapture",
                "Error: ${e.message}"
            )
        }
    }

    private fun createNotification(): Notification {

        val channelId = "capture_channel"

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel = NotificationChannel(
                channelId,
                "Screen Capture",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                channel
            )
        }

        return Notification.Builder(
            this,
            channelId
        )
            .setContentTitle("Nihongo Lens")
            .setContentText(
                "Live OCR capture running"
            )
            .setSmallIcon(
                android.R.drawable.ic_menu_camera
            )
            .build()
    }

    override fun onDestroy() {

        virtualDisplay?.release()

        imageReader?.close()

        mediaProjection?.stop()

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null
}
