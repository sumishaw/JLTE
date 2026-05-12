package com.example.nihongolens

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class OverlayService : Service() {

    companion object {

        var overlayText:
            TextView? = null
    }

    private lateinit var windowManager:
        WindowManager

    private lateinit var textView:
        TextView

    override fun onCreate() {

        super.onCreate()

        windowManager =
            getSystemService(
                WINDOW_SERVICE
            ) as WindowManager

        textView = TextView(this)

        textView.text =
            "Waiting for subtitles..."

        textView.textSize = 24f

        textView.setPadding(
            30,
            20,
            30,
            20
        )

        textView.setTextColor(
            android.graphics.Color.WHITE
        )

        textView.setBackgroundColor(
            0x88000000.toInt()
        )

        val params =
            WindowManager.LayoutParams(

                WindowManager.LayoutParams
                    .WRAP_CONTENT,

                WindowManager.LayoutParams
                    .WRAP_CONTENT,

                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.O
                )

                    WindowManager.LayoutParams
                        .TYPE_APPLICATION_OVERLAY

                else

                    WindowManager.LayoutParams
                        .TYPE_PHONE,

                WindowManager.LayoutParams
                    .FLAG_NOT_FOCUSABLE,

                PixelFormat.TRANSLUCENT
            )

        params.gravity =
            Gravity.TOP or Gravity.CENTER_HORIZONTAL

        params.x = 0

        params.y = 200

        textView.setOnTouchListener(

            object : View.OnTouchListener {

                private var initialX = 0

                private var initialY = 0

                private var initialTouchX =
                    0f

                private var initialTouchY =
                    0f

                override fun onTouch(
                    v: View?,
                    event: MotionEvent
                ): Boolean {

                    when (event.action) {

                        MotionEvent.ACTION_DOWN -> {

                            initialX = params.x

                            initialY = params.y

                            initialTouchX =
                                event.rawX

                            initialTouchY =
                                event.rawY

                            return true
                        }

                        MotionEvent.ACTION_MOVE -> {

                            params.x =
                                initialX +
                                (event.rawX -
                                    initialTouchX)
                                    .toInt()

                            params.y =
                                initialY +
                                (event.rawY -
                                    initialTouchY)
                                    .toInt()

                            windowManager
                                .updateViewLayout(
                                    textView,
                                    params
                                )

                            return true
                        }
                    }

                    return false
                }
            }
        )

        windowManager.addView(
            textView,
            params
        )

        overlayText = textView
    }

    override fun onDestroy() {

        super.onDestroy()

        if (::textView.isInitialized) {

            windowManager.removeView(
                textView
            )
        }
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null
}
