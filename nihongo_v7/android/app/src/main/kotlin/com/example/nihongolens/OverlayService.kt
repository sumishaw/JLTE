package com.example.nihongolens

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class OverlayService : Service() {

    companion object {

        var latestSubtitle =
            "Waiting for translation..."
    }

    private lateinit var windowManager:
            WindowManager

    private lateinit var subtitleView:
            TextView

    private var running = true

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        windowManager =
            getSystemService(WINDOW_SERVICE)
                    as WindowManager

        subtitleView = TextView(this).apply {

            text = latestSubtitle

            textSize = 24f

            setTextColor(Color.WHITE)

            setBackgroundColor(
                Color.argb(
                    170,
                    0,
                    0,
                    0
                )
            )

            gravity = Gravity.CENTER

            setPadding(
                40,
                20,
                40,
                20
            )
        }

        val params = WindowManager.LayoutParams(

            WindowManager.LayoutParams.MATCH_PARENT,

            WindowManager.LayoutParams.WRAP_CONTENT,

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

            else

                WindowManager.LayoutParams.TYPE_PHONE,

            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,

            PixelFormat.TRANSLUCENT
        )

        params.gravity =
            Gravity.BOTTOM

        params.y = 120

        windowManager.addView(
            subtitleView,
            params
        )

        Thread {

            while (running) {

                try {

                    Thread.sleep(300)

                    subtitleView.post {

                        subtitleView.text =
                            latestSubtitle
                    }

                } catch (_: Exception) {}
            }

        }.start()

        return START_STICKY
    }

    override fun onDestroy() {

        super.onDestroy()

        running = false

        try {

            windowManager.removeView(
                subtitleView
            )

        } catch (_: Exception) {}
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }
}
