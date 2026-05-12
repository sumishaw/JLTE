package com.example.nihongolens

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class OverlayService : Service() {

    private lateinit var windowManager:
            WindowManager

    private lateinit var subtitleView:
            TextView

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }

    override fun onCreate() {

        super.onCreate()

        subtitleView = TextView(this)

        subtitleView.text =
            "Waiting for translation..."

        subtitleView.textSize = 22f

        subtitleView.setTextColor(
            android.graphics.Color.WHITE
        )

        subtitleView.setBackgroundColor(
            0x88000000.toInt()
        )

        val params =
            WindowManager.LayoutParams(

                WindowManager.LayoutParams.MATCH_PARENT,

                WindowManager.LayoutParams.WRAP_CONTENT,

                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,

                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,

                PixelFormat.TRANSLUCENT
            )

        params.gravity =
            Gravity.BOTTOM

        params.y = 200

        windowManager =
            getSystemService(
                WINDOW_SERVICE
            ) as WindowManager

        windowManager.addView(
            subtitleView,
            params
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val text =
            intent?.getStringExtra(
                "text"
            )

        if (text != null) {

            subtitleView.text = text
        }

        return START_STICKY
    }

    override fun onDestroy() {

        super.onDestroy()

        windowManager.removeView(
            subtitleView
        )
    }
}
