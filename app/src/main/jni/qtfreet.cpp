//
// Created by qtfreet on 2016/4/7.
//

#include "com_qtfreet_yunbo_jni_jni.h"

JNIEXPORT jstring JNICALL Java_com_qtfreet_yunbo_jni_jni_API
        (JNIEnv *env, jclass clazz) {

    return env->NewStringUTF("hello jni!");

}