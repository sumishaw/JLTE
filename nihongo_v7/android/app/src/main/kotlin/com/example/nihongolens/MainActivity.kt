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

    private val CHANNEL = "com.example.nihongolens/services"
    private val REQUEST_MEDIA_PROJECTION = 1001

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var pendingResult: MethodChannel.Result? = null

    override fun configureFlutterEngine(
        @NonNull flutterEngine: FlutterEngine
    ) {
        super.configureFlutterEngine(flutterEngine)

        mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE)
                    as MediaProjectionManager

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

                    pendingResult = result

                    val captureIntent =
                        mediaProjectionManager.createScreenCaptureIntent()

                    startActivityForResult(
                        captureIntent,
                        REQUEST_MEDIA_PROJECTION
                    )
                }

                else -> result.notImplemented()
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_MEDIA_PROJECTION) {

            if (resultCode == Activity.RESULT_OK && data != null) {

                // START AUDIO CAPTURE SERVICE
                val audioIntent =
                    Intent(this, AudioCaptureService::class.java)

                audioIntent.putExtra("resultCode", resultCode)
                audioIntent.putExtra("data", data)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(audioIntent)
                } else {
                    startService(audioIntent)
                }

                // START OVERLAY SERVICE
                val overlayIntent =
                    Intent(this, OverlayService::class.java)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(overlayIntent)
                } else {
                    startService(overlayIntent)
                }

                pendingResult?.success(true)

            } else {
                pendingResult?.success(false)
            }
        }
    }
}
