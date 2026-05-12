package com.example.nihongolens

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class ScreenCaptureService : Service() {

    private var mediaProjection:
        MediaProjection? = null

    private var virtualDisplay:
        VirtualDisplay? = null

    private var imageReader:
        ImageReader? = null

    private val handler =
        Handler(Looper.getMainLooper())

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

            val wm =
                getSystemService(
                    WINDOW_SERVICE
                ) as WindowManager

            wm.defaultDisplay
                .getRealMetrics(metrics)

            imageReader = ImageReader.newInstance(
                metrics.widthPixels,
                metrics.heightPixels,
                PixelFormat.RGBA_8888,
                2
            )

            virtualDisplay =
                mediaProjection?.createVirtualDisplay(
                    "NihongoLens",
                    metrics.widthPixels,
                    metrics.heightPixels,
                    metrics.densityDpi,
                    DisplayManager
                        .VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader!!.surface,
                    null,
                    handler
                )

            startFrameLoop()

        } catch (e: Exception) {

            Log.e(
                "CaptureService",
                "Start error: ${e.message}"
            )
        }
    }

    private fun startFrameLoop() {

        handler.postDelayed(
            object : Runnable {

                override fun run() {

                    captureFrame()

                    handler.postDelayed(
                        this,
                        2000
                    )
                }
            },
            2000
        )
    }

    private fun captureFrame() {

        try {

            val image: Image =
                imageReader
                    ?.acquireLatestImage()
                    ?: return

            val plane =
                image.planes[0]

            val buffer: ByteBuffer =
                plane.buffer

            val pixelStride =
                plane.pixelStride

            val rowStride =
                plane.rowStride

            val rowPadding =
                rowStride -
                    pixelStride *
                    image.width

            val bitmap =
                Bitmap.createBitmap(
                    image.width +
                        rowPadding /
                        pixelStride,
                    image.height,
                    Bitmap.Config.ARGB_8888
                )

            bitmap.copyPixelsFromBuffer(
                buffer
            )

            saveBitmap(bitmap)

            image.close()

            Log.d(
                "CaptureService",
                "Live frame captured"
            )

        } catch (e: Exception) {

            Log.e(
                "CaptureService",
                "Frame error: ${e.message}"
            )
        }
    }

    private fun saveBitmap(
        bitmap: Bitmap
    ) {

        try {

            val dir =
                File(
                    filesDir,
                    "captures"
                )

            if (!dir.exists()) {
                dir.mkdirs()
            }

            val file =
                File(
                    dir,
                    "latest_frame.png"
                )

            val stream =
                FileOutputStream(file)

            bitmap.compress(
                Bitmap.CompressFormat.PNG,
                100,
                stream
            )

            stream.flush()

            stream.close()

            Log.d(
                "CaptureService",
                "Live frame saved successfully"
            )

        } catch (e: Exception) {

            Log.e(
                "CaptureService",
                "Save error: ${e.message}"
            )
        }
    }

    private fun createNotification():
            Notification {

        val channelId =
            "capture_channel"

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    channelId,
                    "Capture",
                    NotificationManager
                        .IMPORTANCE_LOW
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
            .setContentTitle(
                "Nihongo Lens"
            )
            .setContentText(
                "Live subtitle capture running"
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
