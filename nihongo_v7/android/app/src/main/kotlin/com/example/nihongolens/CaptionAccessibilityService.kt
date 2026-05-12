package com.example.nihongolens

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.plugin.common.MethodChannel

class CaptionAccessibilityService : AccessibilityService() {

    companion object {
        var latestCaption: String = ""
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        val info = AccessibilityServiceInfo().apply {

            eventTypes =
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED

            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC

            flags =
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS

            notificationTimeout = 100

            packageNames = null
        }

        serviceInfo = info

        Log.d("CaptionService", "Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return

        try {

            val rootNode = rootInActiveWindow ?: return

            val detectedText = extractText(rootNode)

            if (detectedText.isBlank()) return

            if (!containsJapanese(detectedText)) return

            if (detectedText == latestCaption) return

            latestCaption = detectedText

            Log.d("CaptionService", "Detected: $detectedText")

            sendCaptionToFlutter(detectedText)

        } catch (e: Exception) {

            Log.e(
                "CaptionService",
                "Error: ${e.message}"
            )
        }
    }

    private fun extractText(node: AccessibilityNodeInfo?): String {

        if (node == null) return ""

        val builder = StringBuilder()

        val text = node.text?.toString()

        if (!text.isNullOrBlank()) {
            builder.append(text).append(" ")
        }

        val contentDescription = node.contentDescription?.toString()

        if (!contentDescription.isNullOrBlank()) {
            builder.append(contentDescription).append(" ")
        }

        for (i in 0 until node.childCount) {

            val child = node.getChild(i)

            builder.append(extractText(child))
        }

        return builder.toString().trim()
    }

    private fun containsJapanese(text: String): Boolean {

        val regex = Regex(
            "[\\u3040-\\u30FF\\u3400-\\u4DBF\\u4E00-\\u9FFF]"
        )

        return regex.containsMatchIn(text)
    }

    private fun sendCaptionToFlutter(text: String) {

        try {

            val engine =
                FlutterEngineCache
                    .getInstance()
                    .get("main_engine")

            engine?.dartExecutor?.binaryMessenger?.let { messenger ->

                MethodChannel(
                    messenger,
                    "nihongo_lens/captions"
                ).invokeMethod(
                    "onCaption",
                    text
                )
            }

        } catch (e: Exception) {

            Log.e(
                "CaptionService",
                "Flutter error: ${e.message}"
            )
        }
    }

    override fun onInterrupt() {}
}
