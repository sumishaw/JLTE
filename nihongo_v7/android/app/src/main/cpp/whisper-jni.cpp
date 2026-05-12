#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>

#include "whisper.h"

#define LOGI(...) \
__android_log_print(ANDROID_LOG_INFO, "WHISPER_JNI", __VA_ARGS__)

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

    whisper_context *ctx =
            whisper_init_from_file(model_path);

    if (!ctx) {

        return env->NewStringUTF(
                "Failed to load Whisper model"
        );
    }

    // TODO:
    // Load WAV samples properly
    // Convert PCM to float array

    std::vector<float> samples;

    whisper_full_params params =
            whisper_full_default_params(
                    WHISPER_SAMPLING_GREEDY
            );

    params.print_progress = false;
    params.print_special = false;
    params.print_realtime = false;
    params.print_timestamps = false;

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

    return env->NewStringUTF(
            transcription.c_str()
    );
}
