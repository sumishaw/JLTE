package com.example.nihongolens

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val CHANNEL = "overlay_channel"

    private val REQUEST_MEDIA_PROJECTION = 1001

    private lateinit var mediaProjectionManager:
            MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE)
                    as MediaProjectionManager

        // Download Whisper model automatically
        Thread {

            try {

                ModelDownloader.downloadModelIfNeeded(this)

            } catch (e: Exception) {

                e.printStackTrace()
            }

        }.start()
    }

    override fun configureFlutterEngine(
        @NonNull flutterEngine: FlutterEngine
    ) {

        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->

            when (call.method) {

                "startOverlay" -> {

                    if (!Settings.canDrawOverlays(this)) {

                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )

                        startActivity(intent)

                        result.success(false)
                        return@setMethodCallHandler
                    }

                    val overlayIntent =
                        Intent(this, OverlayService::class.java)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        startForegroundService(overlayIntent)

                    } else {

                        startService(overlayIntent)
                    }

                    result.success(true)
                }

                "startInternalAudioCapture" -> {

                    val captureIntent =
                        mediaProjectionManager
                            .createScreenCaptureIntent()

                    startActivityForResult(
                        captureIntent,
                        REQUEST_MEDIA_PROJECTION
                    )

                    result.success(true)
                }

                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == REQUEST_MEDIA_PROJECTION &&
            resultCode == Activity.RESULT_OK &&
            data != null
        ) {

            val serviceIntent =
                Intent(this, AudioCaptureService::class.java)

            serviceIntent.putExtra(
                "resultCode",
                resultCode
            )

            serviceIntent.putExtra(
                "data",
                data
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                startForegroundService(serviceIntent)

            } else {

                startService(serviceIntent)
            }
        }
    }
}
