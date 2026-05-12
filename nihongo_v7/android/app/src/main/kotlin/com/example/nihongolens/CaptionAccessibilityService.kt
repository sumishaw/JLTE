package com.example.nihongolens

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.plugin.common.MethodChannel

class CaptionAccessibilityService : AccessibilityService() {

    companion object {
        var latestCaption = ""
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        val info = AccessibilityServiceInfo().apply {

            eventTypes =
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED

            feedbackType =
                AccessibilityServiceInfo.FEEDBACK_GENERIC

            flags =
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS

            notificationTimeout = 50
        }

        serviceInfo = info

        Log.d(
            "CaptionService",
            "Accessibility connected"
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return

        try {

            val texts = event.text

            if (texts.isNullOrEmpty()) return

            val combinedText =
                texts.joinToString(" ")

            if (combinedText.isBlank()) return

            if (!containsJapanese(combinedText)) return

            if (combinedText == latestCaption) return

            latestCaption = combinedText

            Log.d(
                "CaptionService",
                "Detected: $combinedText"
            )

            sendToFlutter(combinedText)

        } catch (e: Exception) {

            Log.e(
                "CaptionService",
                "Error: ${e.message}"
            )
        }
    }

    private fun containsJapanese(text: String): Boolean {

        val regex =
            Regex("[\\u3040-\\u30FF\\u4E00-\\u9FFF]")

        return regex.containsMatchIn(text)
    }

    private fun sendToFlutter(text: String) {

        try {

            val engine =
                FlutterEngineCache
                    .getInstance()
                    .get("main_engine")

            engine?.dartExecutor?.binaryMessenger?.let {

                MethodChannel(
                    it,
                    "nihongo_lens/captions"
                ).invokeMethod(
                    "onCaption",
                    text
                )
            }

        } catch (e: Exception) {

            Log.e(
                "CaptionService",
                "Flutter send failed"
            )
        }
    }

    override fun onInterrupt() {}
}
