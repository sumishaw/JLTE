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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
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

    private val recognizer =
        TextRecognition.getClient(
            JapaneseTextRecognizerOptions.Builder()
                .build()
        )

    private var lastSubtitle = ""

    private var processing = false

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

                    if (!processing) {

                        processing = true

                        captureFrame()
                    }

                    handler.postDelayed(
                        this,
                        1000
                    )
                }
            },
            1000
        )
    }

    private fun captureFrame() {

        try {

            val image: Image =
                imageReader
                    ?.acquireLatestImage()
                    ?: run {

                        processing = false

                        return
                    }

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

            image.close()

            processOCR(bitmap)

        } catch (e: Exception) {

            processing = false

            Log.e(
                "CaptureService",
                "Frame error: ${e.message}"
            )
        }
    }

    private fun processOCR(
        bitmap: Bitmap
    ) {

        try {

            val cropTop =
                (bitmap.height * 0.72).toInt()

            val cropHeight =
                (bitmap.height * 0.22).toInt()

            val cropped =
                Bitmap.createBitmap(
                    bitmap,
                    0,
                    cropTop,
                    bitmap.width,
                    cropHeight
                )

            val inputImage =
                InputImage.fromBitmap(
                    cropped,
                    0
                )

            recognizer.process(inputImage)

                .addOnSuccessListener {

                    processing = false

                    var text =
                        it.text.trim()

                    if (
                        text.isEmpty()
                    ) return@addOnSuccessListener

                    text =
                        cleanupJapanese(text)

                    if (
                        text.isEmpty()
                    ) return@addOnSuccessListener

                    if (
                        text == lastSubtitle
                    ) return@addOnSuccessListener

                    lastSubtitle = text

                    MainActivity
                        .subtitleSink
                        ?.success(text)

                    Log.d(
                        "CaptureService",
                        "Subtitle: $text"
                    )
                }

                .addOnFailureListener {

                    processing = false

                    Log.e(
                        "CaptureService",
                        "OCR failed"
                    )
                }

        } catch (e: Exception) {

            processing = false

            Log.e(
                "CaptureService",
                "OCR error: ${e.message}"
            )
        }
    }

    private fun cleanupJapanese(
        text: String
    ): String {

        val lines =
            text
                .split("\n")
                .map {
                    it.trim()
                }
                .filter {

                    if (it.length < 2)
                        return@filter false

                    val hasJapanese =
                        Regex(
                            "[\\u3040-\\u30ff\\u4e00-\\u9faf]"
                        ).containsMatchIn(it)

                    if (!hasJapanese)
                        return@filter false

                    if (
                        it.contains("http") ||
                        it.contains("www")
                    ) {
                        return@filter false
                    }

                    true
                }

        return lines.joinToString("\n")
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
                "Live subtitle OCR active"
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
