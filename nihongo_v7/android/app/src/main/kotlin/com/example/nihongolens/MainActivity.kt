package com.example.nihongolens

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.View
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    companion object {

        var subtitleSink:
            EventChannel.EventSink? = null
    }

    private val METHOD_CHANNEL =
        "nihongo_lens/capture"

    private val EVENT_CHANNEL =
        "nihongo_lens/subtitles"

    private val REQUEST_CODE = 1001

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        MethodChannel(
            flutterEngine!!
                .dartExecutor
                .binaryMessenger,
            METHOD_CHANNEL
        ).setMethodCallHandler { call, result ->

            when (call.method) {

                "startCapture" -> {

                    val manager =
                        getSystemService(
                            MEDIA_PROJECTION_SERVICE
                        ) as MediaProjectionManager

                    startActivityForResult(
                        manager.createScreenCaptureIntent(),
                        REQUEST_CODE
                    )

                    result.success(true)
                }

                "startOverlay" -> {

                    val intent = Intent(
                        this,
                        OverlayService::class.java
                    )

                    startService(intent)

                    result.success(true)
                }

                "updateOverlay" -> {

                    val text =
                        call.argument<String>("text")

                    OverlayService.overlayText
                        ?.post {

                            OverlayService.overlayText
                                ?.text = text

                            OverlayService.overlayText
                                ?.visibility =
                                    View.VISIBLE
                        }

                    result.success(true)
                }

                else -> {

                    result.notImplemented()
                }
            }
        }

        EventChannel(
            flutterEngine!!
                .dartExecutor
                .binaryMessenger,
            EVENT_CHANNEL
        ).setStreamHandler(

            object :
                EventChannel.StreamHandler {

                override fun onListen(
                    arguments: Any?,
                    events:
                        EventChannel.EventSink?
                ) {

                    subtitleSink = events
                }

                override fun onCancel(
                    arguments: Any?
                ) {

                    subtitleSink = null
                }
            }
        )
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
