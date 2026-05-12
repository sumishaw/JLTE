package com.example.nihongolens

object WhisperManager {

    init {
        System.loadLibrary("whisper")
    }

    external fun transcribeAudio(
        modelPath: String,
        wavPath: String
    ): String
}
