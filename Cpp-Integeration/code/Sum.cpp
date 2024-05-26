#include "Sum.h"
#include <stdio.h>

JNIEXPORT jdouble JNICALL Java_Sum_myNativeSum(JNIEnv *env, jclass obj, jdouble a, jdouble b) {
    return a+b;
}
