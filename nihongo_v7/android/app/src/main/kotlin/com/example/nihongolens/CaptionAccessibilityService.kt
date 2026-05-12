package com.example.nihongolens

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class CaptionAccessibilityService :
    AccessibilityService() {

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {

        if (event == null) return

        val rootNode =
            rootInActiveWindow ?: return

        val text =
            findJapaneseText(rootNode)

        if (
            text.isNotBlank() &&
            containsJapanese(text)
        ) {

            MainActivity.overlayText =
                text
        }
    }

    private fun findJapaneseText(
        node: AccessibilityNodeInfo?
    ): String {

        if (node == null) {
            return ""
        }

        if (
            node.text != null
        ) {

            val text =
                node.text.toString()

            if (
                containsJapanese(text)
            ) {

                return text
            }
        }

        for (
            i in 0 until node.childCount
        ) {

            val result =
                findJapaneseText(
                    node.getChild(i)
                )

            if (
                result.isNotBlank()
            ) {

                return result
            }
        }

        return ""
    }

    private fun containsJapanese(
        text: String
    ): Boolean {

        return text.matches(
            Regex(".*[ぁ-んァ-ン一-龯].*")
        )
    }

    override fun onInterrupt() {

    }
}
