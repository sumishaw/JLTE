package com.example.nihongolens

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val CHANNEL = "nihongo_lens/capture"

    private val REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        MethodChannel(
            flutterEngine!!.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->

            if (call.method == "startCapture") {

                try {

                    val manager =
                        getSystemService(
                            MEDIA_PROJECTION_SERVICE
                        ) as MediaProjectionManager

                    val captureIntent =
                        manager.createScreenCaptureIntent()

                    startActivityForResult(
                        captureIntent,
                        REQUEST_CODE
                    )

                    result.success(true)

                } catch (e: Exception) {

                    Log.e(
                        "MainActivity",
                        "Error: ${e.message}"
                    )

                    result.error(
                        "CAPTURE_ERROR",
                        e.message,
                        null
                    )
                }

            } else {

                result.notImplemented()
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

        if (requestCode == REQUEST_CODE) {

            if (
                resultCode == Activity.RESULT_OK &&
                data != null
            ) {

                try {

                    ScreenCaptureHolder.resultCode =
                        resultCode

                    ScreenCaptureHolder.data =
                        data

                    val serviceIntent = Intent(
                        this,
                        ScreenCaptureService::class.java
                    )

                    startForegroundService(
                        serviceIntent
                    )

                    Log.d(
                        "MainActivity",
                        "Capture started"
                    )

                } catch (e: Exception) {

                    Log.e(
                        "MainActivity",
                        "Service error: ${e.message}"
                    )
                }

            } else {

                Log.e(
                    "MainActivity",
                    "Permission denied"
                )
            }
        }
    }
}
