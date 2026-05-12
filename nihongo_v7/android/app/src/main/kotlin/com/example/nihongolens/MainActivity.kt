package com.example.nihongolens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    companion object {

        var overlayText = "Waiting..."
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

                "startOverlay" -> {

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

                        result.success(
                            "Overlay permission requested"
                        )

                    } else {

                        result.success(
                            "Overlay already granted"
                        )
                    }
                }

                "updateOverlay" -> {

                    val text =
                        call.argument<String>(
                            "text"
                        ) ?: ""

                    overlayText = text

                    result.success(
                        "Updated"
                    )
                }

                else -> {

                    result.notImplemented()
                }
            }
        )
    }
}
