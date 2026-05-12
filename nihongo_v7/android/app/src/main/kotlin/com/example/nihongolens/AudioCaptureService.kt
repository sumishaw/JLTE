package com.example.nihongolens

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class AudioCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var audioRecord: AudioRecord? = null
    private var isRunning = false

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        startForegroundNotification()

        val resultCode =
            intent?.getIntExtra("resultCode", -1) ?: -1

        val data =
            intent?.getParcelableExtra<Intent>("data")

        if (resultCode == -1 || data == null) {

            Log.e(
                "AUDIO_CAPTURE",
                "MediaProjection permission missing"
            )

            stopSelf()
            return START_NOT_STICKY
        }

        try {

            val projectionManager =
                getSystemService(MEDIA_PROJECTION_SERVICE)
                        as MediaProjectionManager

            mediaProjection =
                projectionManager.getMediaProjection(
                    resultCode,
                    data
                )

            startAudioCapture()

        } catch (e: Exception) {

            Log.e(
                "AUDIO_CAPTURE",
                "Projection error: ${e.message}"
            )

            stopSelf()
        }

        return START_STICKY
    }

    private fun startAudioCapture() {

        try {

            val config =
                AudioPlaybackCaptureConfiguration.Builder(
                    mediaProjection!!
                )
                    .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                    .addMatchingUsage(AudioAttributes.USAGE_GAME)
                    .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                    .build()

            val sampleRate = 44100

            val channelConfig =
                AudioFormat.CHANNEL_IN_MONO

            val audioFormat =
                AudioFormat.ENCODING_PCM_16BIT

            val bufferSize =
                AudioRecord.getMinBufferSize(
                    sampleRate,
                    channelConfig,
                    audioFormat
                )

            if (bufferSize == AudioRecord.ERROR ||
                bufferSize == AudioRecord.ERROR_BAD_VALUE
            ) {

                Log.e(
                    "AUDIO_CAPTURE",
                    "Invalid buffer size"
                )

                stopSelf()
                return
            }

            audioRecord =
                AudioRecord.Builder()
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(audioFormat)
                            .setSampleRate(sampleRate)
                            .setChannelMask(channelConfig)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize * 2)
                    .setAudioPlaybackCaptureConfig(config)
                    .build()

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {

                Log.e(
                    "AUDIO_CAPTURE",
                    "AudioRecord initialization failed"
                )

                stopSelf()
                return
            }

            try {

                audioRecord?.startRecording()

                if (
                    audioRecord?.recordingState
                    != AudioRecord.RECORDSTATE_RECORDING
                ) {

                    Log.e(
                        "AUDIO_CAPTURE",
                        "Recording failed to start"
                    )

                    stopSelf()
                    return
                }

            } catch (e: Exception) {

                Log.e(
                    "AUDIO_CAPTURE",
                    "startRecording crash: ${e.message}"
                )

                stopSelf()
                return
            }

            Log.d(
                "AUDIO_CAPTURE",
                "Audio capture started successfully"
            )

            isRunning = true

            Thread {

                val buffer = ByteArray(bufferSize)

                while (
                    isRunning &&
                    audioRecord != null &&
                    audioRecord?.recordingState ==
                    AudioRecord.RECORDSTATE_RECORDING
                ) {

                    try {

                        val read =
                            audioRecord?.read(
                                buffer,
                                0,
                                buffer.size
                            ) ?: 0

                        if (read > 0) {

                            Log.d(
                                "AUDIO_LEVEL",
                                "VOICE DETECTED: $read bytes"
                            )

                            // TODO:
                            // Speech recognition pipeline here
                        }

                    } catch (e: Exception) {

                        Log.e(
                            "AUDIO_CAPTURE",
                            "Read error: ${e.message}"
                        )

                        break
                    }
                }

            }.start()

        } catch (e: Exception) {

            Log.e(
                "AUDIO_CAPTURE",
                "Audio capture error: ${e.message}"
            )

            e.printStackTrace()

            stopSelf()
        }
    }

    private fun startForegroundNotification() {

        val channelId = "nihongo_audio_capture"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Audio Capture Service",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager =
                getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)
        }

        val notification: Notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("Nihongo Lens")
                .setContentText("Listening to internal audio...")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .build()

        startForeground(101, notification)
    }

    override fun onDestroy() {

        super.onDestroy()

        isRunning = false

        try {

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            mediaProjection?.stop()
            mediaProjection = null

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
