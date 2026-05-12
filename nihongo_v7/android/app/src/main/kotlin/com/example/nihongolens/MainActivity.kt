package com.example.nihongolens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    companion object {

        var overlayText = ""
    }

    private val CHANNEL =
        "overlay_channel"

    override fun configureFlutterEngine(
        flutterEngine: FlutterEngine
    ) {

        super.configureFlutterEngine(
            flutterEngine
        )

        MethodChannel(
            flutterEngine
                .dartExecutor
                .binaryMessenger,
            CHANNEL
        ).setMethodCallHandler {

            call,
            result ->

            when (call.method) {

                "getSubtitleText" -> {

                    result.success(
                        overlayText
                    )
                }

                "requestOverlayPermission" -> {

                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.M &&
                        !Settings.canDrawOverlays(
                            this
                        )
                    ) {

                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse(
                                "package:$packageName"
                            )
                        )

                        startActivity(intent)
                    }

                    result.success(true)
                }

                "showOverlay" -> {

                    val text =
                        call.argument<String>(
                            "text"
                        ) ?: ""

                    val intent =
                        Intent(
                            this,
                            OverlayService::class.java
                        )

                    intent.putExtra(
                        "text",
                        text
                    )

                    startService(intent)

                    result.success(true)
                }

                else -> {

                    result.notImplemented()
                }
            }
        }
    }
}
