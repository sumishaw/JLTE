package com.example.nihongolens

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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

    private val handler =
        Handler(Looper.getMainLooper())

    private var hideRunnable:
        Runnable? = null

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

        textView.setTextColor(
            Color.WHITE
        )

        textView.setPadding(
            40,
            25,
            40,
            25
        )

        textView.setBackgroundColor(
            0xAA000000.toInt()
        )

        textView.alpha = 0.95f

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
                    .FLAG_NOT_FOCUSABLE
                    or
                    WindowManager.LayoutParams
                        .FLAG_LAYOUT_NO_LIMITS,

                PixelFormat.TRANSLUCENT
            )

        params.gravity =
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL

        params.x = 0

        params.y = 220

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

    fun updateSubtitle(
        text: String
    ) {

        handler.post {

            overlayText?.visibility =
                View.VISIBLE

            overlayText?.text = text

            hideRunnable?.let {

                handler.removeCallbacks(it)
            }

            hideRunnable = Runnable {

                overlayText?.visibility =
                    View.INVISIBLE
            }

            handler.postDelayed(
                hideRunnable!!,
                4000
            )
        }
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
