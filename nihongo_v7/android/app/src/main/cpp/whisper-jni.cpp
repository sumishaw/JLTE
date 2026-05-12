#include <jni.h>
#include <string>
#include <android/log.h>

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

    LOGI("Model: %s", model_path);

    LOGI("WAV: %s", wav_path);

    std::string result =
            "Whisper JNI placeholder working";

    env->ReleaseStringUTFChars(
            modelPath,
            model_path
    );

    env->ReleaseStringUTFChars(
            wavPath,
            wav_path
    );

    return env->NewStringUTF(
            result.c_str()
    );
}
