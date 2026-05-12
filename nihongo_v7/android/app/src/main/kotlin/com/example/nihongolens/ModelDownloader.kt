package com.example.nihongolens

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object ModelDownloader {

    private const val MODEL_URL =
        "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin"

    fun downloadModelIfNeeded(
        context: Context
    ): String {

        val modelFile =
            File(
                context.filesDir,
                "ggml-tiny.bin"
            )

        if (modelFile.exists()) {
            return modelFile.absolutePath
        }

        val connection =
            URL(MODEL_URL).openConnection()

        connection.connect()

        val inputStream =
            connection.getInputStream()

        val outputStream =
            FileOutputStream(modelFile)

        val buffer = ByteArray(8192)

        while (true) {

            val bytesRead =
                inputStream.read(buffer)

            if (bytesRead == -1) break

            outputStream.write(
                buffer,
                0,
                bytesRead
            )
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()

        return modelFile.absolutePath
    }
}
