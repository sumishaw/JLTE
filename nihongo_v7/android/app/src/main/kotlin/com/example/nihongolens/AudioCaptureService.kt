package com.example.nihongolens

import android.app.*
import android.content.Intent
import android.media.*
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var audioRecord: AudioRecord? = null
    private var isCapturing = false
    private var captureThread: Thread? = null
    private var translatorManager: TranslatorManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private val audioBuffer = mutableListOf<Short>()

    private val SAMPLE_RATE = 16000
    private val CHUNK_SAMPLES = SAMPLE_RATE * 3 // 3 seconds per chunk

    companion object {
        const val CHANNEL_ID = "nihongo_channel"
        const val NOTIF_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        translatorManager = TranslatorManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIF_ID, buildNotification())

        val resultCode = intent?.getIntExtra("resultCode", -1) ?: -1
        val data: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("data", Intent::class.java)
        } else {
            @Suppress("DEPRECATION") intent?.getParcelableExtra("data")
        }

        if (resultCode == -1 || data == null) {
            showOverlay("⚠️ Screen capture permission missing")
            return START_NOT_STICKY
        }

        val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mgr.getMediaProjection(resultCode, data)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startInternalCapture()
        } else {
            showOverlay("⚠️ Requires Android 10+")
        }

        return START_STICKY
    }

    private fun startInternalCapture() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        try {
            val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection!!)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .addMatchingUsage(AudioAttributes.USAGE_GAME)
                .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                .build()

            val minBuf = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

            audioRecord = AudioRecord.Builder()
                .setAudioPlaybackCaptureConfig(config)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBuf * 4)
                .build()

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                showOverlay("⚠️ AudioRecord init failed")
                return
            }

            audioRecord?.startRecording()
            isCapturing = true
            showOverlay("🎧 Capturing phone audio — play Japanese video!")

            captureThread = Thread {
                val buf = ShortArray(minBuf)
                while (isCapturing) {
                    val read = audioRecord?.read(buf, 0, buf.size) ?: break
                    if (read > 0) {
                        synchronized(audioBuffer) {
                            audioBuffer.addAll(buf.take(read))
                            if (audioBuffer.size >= CHUNK_SAMPLES) {
                                val chunk = audioBuffer.toShortArray()
                                audioBuffer.clear()
                                processChunk(chunk)
                            }
                        }
                    }
                }
            }
            captureThread?.start()

        } catch (e: Exception) {
            showOverlay("⚠️ Capture error: ${e.message}")
        }
    }

    private fun processChunk(samples: ShortArray) {
        // Skip silent chunks
        val rms = samples.map { it.toLong() * it }.average()
        if (rms < 300) return

        val pcmBytes = ByteBuffer.allocate(samples.size * 2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .also { buf -> samples.forEach { buf.putShort(it) } }
            .array()

        Thread {
            val japanese = callGoogleStt(pcmBytes)
            if (!japanese.isNullOrBlank()) {
                showOverlay("🔄 $japanese", isPartial = true)
                translatorManager?.translate(japanese) { english ->
                    showOverlay(english, japanese = japanese)
                }
            }
        }.start()
    }

    private fun callGoogleStt(pcmBytes: ByteArray): String? {
        return try {
            val audioB64 = Base64.encodeToString(pcmBytes, Base64.NO_WRAP)
            val body = """{"config":{"encoding":"LINEAR16","sampleRateHertz":$SAMPLE_RATE,"languageCode":"ja-JP","model":"default","maxAlternatives":1},"audio":{"content":"$audioB64"}}"""

            // Google STT REST API - uses a browser-level API key (same as Chrome speech)
            val url = URL("https://speech.googleapis.com/v1/speech:recognize?key=AIzaSyBOti4mM-6x9WDnZIjIeyEU21OpBXqWBgw")
            val conn = url.openConnection() as HttpURLConnection
            conn.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }
            OutputStreamWriter(conn.outputStream).use { it.write(body) }

            if (conn.responseCode == 200) {
                val resp = conn.inputStream.bufferedReader().readText()
                JSONObject(resp)
                    .optJSONArray("results")
                    ?.getJSONObject(0)
                    ?.optJSONArray("alternatives")
                    ?.getJSONObject(0)
                    ?.optString("transcript")
            } else {
                val err = conn.errorStream?.bufferedReader()?.readText()
                Log.e("NihongoLens", "STT error ${conn.responseCode}: $err")
                null
            }
        } catch (e: Exception) {
            Log.e("NihongoLens", "STT exception: ${e.message}")
            null
        }
    }

    private fun showOverlay(text: String, japanese: String = "", isPartial: Boolean = false) {
        handler.post {
            startService(Intent(this, OverlayService::class.java).apply {
                putExtra("subtitle", text)
                putExtra("japanese", japanese)
                putExtra("partial", isPartial)
            })
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "Nihongo Lens", NotificationManager.IMPORTANCE_LOW)
                .apply { setShowBadge(false) }
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        val stopPi = PendingIntent.getService(
            this, 0,
            Intent(this, AudioCaptureService::class.java).apply { action = "STOP" },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎌 Nihongo Lens Active")
            .setContentText("Capturing & translating Japanese audio")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .addAction(android.R.drawable.ic_delete, "Stop", stopPi)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        isCapturing = false
        captureThread?.join(1000)
        try { audioRecord?.stop(); audioRecord?.release() } catch (_: Exception) {}
        try { mediaProjection?.stop() } catch (_: Exception) {}
        translatorManager?.close()
        stopService(Intent(this, OverlayService::class.java))
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
