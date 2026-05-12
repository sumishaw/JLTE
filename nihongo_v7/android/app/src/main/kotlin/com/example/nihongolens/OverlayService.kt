package com.example.nihongolens

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayText: TextView

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        windowManager =
            getSystemService(WINDOW_SERVICE)
                    as WindowManager

        overlayText = TextView(this).apply {

            text = "Japanese captions will appear here"
            textSize = 20f

            setTextColor(android.graphics.Color.WHITE)

            setBackgroundColor(
                android.graphics.Color.argb(
                    180,
                    0,
                    0,
                    0
                )
            )

            setPadding(30, 20, 30, 20)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,

            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,

            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        params.y = 200

        windowManager.addView(overlayText, params)

        return START_STICKY
    }

    override fun onDestroy() {

        super.onDestroy()

        try {
            windowManager.removeView(overlayText)
        } catch (_: Exception) {
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
