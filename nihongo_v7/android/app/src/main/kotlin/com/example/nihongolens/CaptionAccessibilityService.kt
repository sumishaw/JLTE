package com.example.nihongolens

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class CaptionAccessibilityService
    : AccessibilityService() {

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {

        if (event == null) return

        val text =
            extractText(
                rootInActiveWindow
            )

        if (
            text.isNotBlank()
        ) {

            MainActivity.overlayText =
                text
        }
    }

    private fun extractText(
        node: AccessibilityNodeInfo?
    ): String {

        if (node == null) {
            return ""
        }

        val builder =
            StringBuilder()

        if (
            node.text != null
        ) {

            builder.append(
                node.text.toString()
            )

            builder.append("\n")
        }

        for (
            i in 0 until
                    node.childCount
        ) {

            builder.append(

                extractText(
                    node.getChild(i)
                )
            )
        }

        return builder.toString()
    }

    override fun onInterrupt() {

    }
}
