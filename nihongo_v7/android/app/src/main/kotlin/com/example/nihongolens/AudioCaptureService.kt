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
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream

class AudioCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var audioRecord: AudioRecord? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        createNotification()

        val resultCode =
            intent?.getIntExtra("resultCode", -1) ?: -1

        val data =
            intent?.getParcelableExtra<Intent>("data")

        if (resultCode == -1 || data == null) {

            stopSelf()
            return START_NOT_STICKY
        }

        val projectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE)
                    as MediaProjectionManager

        mediaProjection =
            projectionManager.getMediaProjection(
                resultCode,
                data
            )

        startCapture()

        return START_STICKY
    }

    private fun startCapture() {

        try {

            val config =
                AudioPlaybackCaptureConfiguration.Builder(
                    mediaProjection!!
                )
                    .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                    .addMatchingUsage(AudioAttributes.USAGE_GAME)
                    .build()

            val sampleRate = 44100

            val bufferSize =
                AudioRecord.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

            audioRecord =
                AudioRecord.Builder()
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(
                                AudioFormat.ENCODING_PCM_16BIT
                            )
                            .setSampleRate(sampleRate)
                            .setChannelMask(
                                AudioFormat.CHANNEL_IN_MONO
                            )
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize * 2)
                    .setAudioPlaybackCaptureConfig(config)
                    .build()

            audioRecord?.startRecording()

            Thread {

                val buffer = ByteArray(bufferSize)

                val outputDir =
                    getExternalFilesDir(null)

                val outputFile =
                    File(outputDir, "capture.pcm")

                val outputStream =
                    FileOutputStream(outputFile)

                Log.d(
                    "PCM_CAPTURE",
                    "Saving PCM to: ${outputFile.absolutePath}"
                )

                var totalBytes = 0

                while (totalBytes < 44100 * 2 * 20) {

                    val read =
                        audioRecord?.read(
                            buffer,
                            0,
                            buffer.size
                        ) ?: 0

                    if (read > 0) {

                        outputStream.write(
                            buffer,
                            0,
                            read
                        )

                        totalBytes += read

                        Log.d(
                            "PCM_CAPTURE",
                            "Captured: $totalBytes bytes"
                        )
                    }
                }

                outputStream.flush()
                outputStream.close()

                Log.d(
                    "PCM_CAPTURE",
                    "PCM capture complete"
                )

            }.start()

        } catch (e: Exception) {

            Log.e(
                "PCM_CAPTURE",
                "Capture error: ${e.message}"
            )
        }
    }

    private fun createNotification() {

        val channelId = "audio_capture"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Audio Capture",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager =
                getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)
        }

        val notification: Notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("Nihongo Lens")
                .setContentText("Capturing internal audio...")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
