#include <jni.h>
#include <string.h>
#include <stdlib.h>

__attribute__((visibility("hidden"))) char* developedBy() {
    char* text = (char*)malloc(38 * sizeof(char));
    text[0] = 68;
    text[1] = 101;
    text[2] = 118;
    text[3] = 101;
    text[4] = 108;
    text[5] = 111;
    text[6] = 112;
    text[7] = 101;
    text[8] = 100;
    text[9] = 32;
    text[10] = 98;
    text[11] = 121;
    text[12] = 32;
    text[13] = 112;
    text[14] = 52;
    text[15] = 110;
    text[16] = 99;
    text[17] = 111;
    text[18] = 110;
    text[19] = 116;
    text[20] = 111;
    text[21] = 109;
    text[22] = 97;
    text[23] = 116;
    text[24] = 51;
    text[25] = 32;
    text[26] = 97;
    text[27] = 110;
    text[28] = 100;
    text[29] = 32;
    text[30] = 49;
    text[31] = 110;
    text[32] = 104;
    text[33] = 52;
    text[34] = 108;
    text[35] = 51;
    text[36] = 114;
    text[37] = '\0';

    return text;
}

__attribute__((visibility("hidden"))) char* fridaMessage() {
    char* text = (char*)malloc(28 * sizeof(char));
    text[0] = 102;
    text[1] = 114;
    text[2] = 105;
    text[3] = 100;
    text[4] = 97;
    text[5] = 123;
    text[6] = 108;
    text[7] = 49;
    text[8] = 98;
    text[9] = 114;
    text[10] = 101;
    text[11] = 114;
    text[12] = 49;
    text[13] = 97;
    text[14] = 115;
    text[15] = 95;
    text[16] = 110;
    text[17] = 52;
    text[18] = 116;
    text[19] = 105;
    text[20] = 118;
    text[21] = 97;
    text[22] = 115;
    text[23] = 63;
    text[24] = 63;
    text[25] = 63;
    text[26] = 63;
    text[27] = '}';

    return text;
}

__attribute__((visibility("hidden"))) char* maybeClose() {
    char* text = (char*)malloc(27 * sizeof(char));
    text[0] = 60;
    text[1] = 109;
    text[2] = 97;
    text[3] = 121;
    text[4] = 98;
    text[5] = 101;
    text[6] = 62;
    text[7] = 32;
    text[8] = 121;
    text[9] = 111;
    text[10] = 117;
    text[11] = 39;
    text[12] = 114;
    text[13] = 101;
    text[14] = 32;
    text[15] = 112;
    text[16] = 114;
    text[17] = 101;
    text[18] = 116;
    text[19] = 116;
    text[20] = 121;
    text[21] = 32;
    text[22] = 99;
    text[23] = 108;
    text[24] = 111;
    text[25] = 115;
    text[26] = 101;

    return text;
}

JNIEXPORT jstring JNICALL
Java_com_nivel4_Dialogs_About_nativeText(JNIEnv *env, jobject thiz, jstring inputString) {
    const char *input = (*env)->GetStringUTFChars(env, inputString, NULL);
    const char *result = NULL;

    if (strcmp(input, "yes") == 0) {
        char* part1 = developedBy();

        result = (char*)malloc(strlen(part1));
        strcpy(result, part1);

        free(part1);
    } else if (strcmp(input, "maybe") == 0) {
        result = fridaMessage();
    } else {
        result = maybeClose();
    }

    (*env)->ReleaseStringUTFChars(env, inputString, input);

    jstring jResult = (*env)->NewStringUTF(env, result);

    free((void*)result);

    return jResult;
}
