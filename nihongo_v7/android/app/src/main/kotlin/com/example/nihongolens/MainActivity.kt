package com.example.nihongolens

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val CHANNEL =
        "nihongo_lens/capture"

    private val REQUEST_CODE = 1001

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        MethodChannel(
            flutterEngine!!
                .dartExecutor
                .binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->

            if (call.method == "startCapture") {

                val manager =
                    getSystemService(
                        MEDIA_PROJECTION_SERVICE
                    ) as MediaProjectionManager

                startActivityForResult(
                    manager.createScreenCaptureIntent(),
                    REQUEST_CODE
                )

                result.success(true)

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

        if (
            requestCode == REQUEST_CODE &&
            resultCode == Activity.RESULT_OK &&
            data != null
        ) {

            ScreenCaptureHolder.resultCode =
                resultCode

            ScreenCaptureHolder.data =
                data

            val intent = Intent(
                this,
                ScreenCaptureService::class.java
            )

            startForegroundService(intent)
        }
    }
}
