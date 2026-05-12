#include <jni.h>
#include <string>
#include <vector>
#include <fstream>
#include <android/log.h>

#include "whisper.h"

#define LOGI(...) \
__android_log_print(ANDROID_LOG_INFO, "WHISPER_JNI", __VA_ARGS__)

static bool load_wav_file(
        const std::string & filename,
        std::vector<float> & pcmf32
) {

    std::ifstream file(
            filename,
            std::ios::binary
    );

    if (!file.is_open()) {
        return false;
    }

    // Skip WAV header
    file.seekg(44);

    std::vector<int16_t> pcm16;

    while (!file.eof()) {

        int16_t sample;

        file.read(
                reinterpret_cast<char*>(&sample),
                sizeof(int16_t)
        );

        if (file.gcount() == sizeof(int16_t)) {
            pcm16.push_back(sample);
        }
    }

    pcmf32.resize(pcm16.size());

    for (size_t i = 0; i < pcm16.size(); i++) {

        pcmf32[i] =
                pcm16[i] / 32768.0f;
    }

    return true;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_nihongolens_WhisperManager_transcribeAudio(
        JNIEnv *env,
        jobject,
        jstring modelPath,
        jstring wavPath
) {

    const char *model_path =
            env->GetStringUTFChars(modelPath, 0);

    const char *wav_path =
            env->GetStringUTFChars(wavPath, 0);

    LOGI("Loading model...");

    whisper_context *ctx =
            whisper_init_from_file(model_path);

    if (!ctx) {

        return env->NewStringUTF(
                "Failed to load Whisper model"
        );
    }

    std::vector<float> samples;

    LOGI("Loading WAV...");

    if (!load_wav_file(
            wav_path,
            samples
    )) {

        whisper_free(ctx);

        return env->NewStringUTF(
                "Failed to load WAV"
        );
    }

    LOGI(
            "Loaded %d samples",
            (int)samples.size()
    );

    whisper_full_params params =
            whisper_full_default_params(
                    WHISPER_SAMPLING_GREEDY
            );

    params.print_progress = false;
    params.print_special = false;
    params.print_realtime = false;
    params.print_timestamps = false;

    params.language = "ja";

    LOGI("Running Whisper...");

    int result =
            whisper_full(
                    ctx,
                    params,
                    samples.data(),
                    samples.size()
            );

    if (result != 0) {

        whisper_free(ctx);

        return env->NewStringUTF(
                "Whisper transcription failed"
        );
    }

    std::string transcription;

    const int n_segments =
            whisper_full_n_segments(ctx);

    for (int i = 0; i < n_segments; ++i) {

        transcription +=
                whisper_full_get_segment_text(
                        ctx,
                        i
                );
    }

    whisper_free(ctx);

    env->ReleaseStringUTFChars(
            modelPath,
            model_path
    );

    env->ReleaseStringUTFChars(
            wavPath,
            wav_path
    );

    LOGI(
            "Transcription: %s",
            transcription.c_str()
    );

    return env->NewStringUTF(
            transcription.c_str()
    );
}
