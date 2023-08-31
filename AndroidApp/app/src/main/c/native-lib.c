#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include<math.h>

__attribute__((visibility("hidden"))) char* dummy1() {
    char* text = (char*)malloc(4 * sizeof(char));
    text[0] = 68;
    text[1] = 68;
    text[2] = 68;
    text[3] = 68;
    return text;
}

__attribute__((visibility("hidden"))) char* dummy2() {
    char* text = (char*)malloc(4 * sizeof(char));
    text[0] = 68;
    text[1] = 68;
    text[2] = 68;
    text[3] = 68;
    return text;
}

__attribute__((visibility("hidden"))) char* dummy3() {
    char* text = (char*)malloc(4 * sizeof(char));
    text[0] = 68;
    text[1] = 68;
    text[2] = 68;
    text[3] = 68;
    return text;
}

__attribute__((visibility("hidden"))) char* dummy4() {
    char* text = (char*)malloc(4 * sizeof(char));
    text[0] = 68;
    text[1] = 68;
    text[2] = 68;
    text[3] = 68;
    return text;
}

__attribute__((visibility("hidden"))) char* dummy5() {
    char* text = (char*)malloc(4 * sizeof(char));
    text[0] = 68;
    text[1] = 68;
    text[2] = 68;
    text[3] = 68;
    return text;
}

__attribute__((visibility("hidden"))) char* dummy6() {
    char* text = (char*)malloc(4 * sizeof(char));
    text[0] = 68;
    text[1] = 68;
    text[2] = 68;
    text[3] = 68;
    return text;
}

__attribute__((visibility("hidden"))) char* dummy7() {
    char* text = (char*)malloc(4 * sizeof(char));
    text[0] = 68;
    text[1] = 68;
    text[2] = 68;
    text[3] = 68;
    return text;
}

__attribute__((visibility("hidden"))) char* dummy8() {
    char* text = (char*)malloc(4 * sizeof(char));
    text[0] = 68;
    text[1] = 68;
    text[2] = 68;
    text[3] = 68;
    return text;
}

__attribute__((visibility("hidden"))) int dummy9(){
    char buf[] = "xdxd";
    return 0;
}

__attribute__((visibility("hidden"))) int dummy10(){
    char buf[] = "asdf";
    return 0;
}

__attribute__((visibility("hidden"))) int dummy11(){
    char buf[] = "fdsa";
    return 0;
}

__attribute__((visibility("hidden"))) int dummy12(){
    char buf[] = "xd";
    return 0;
}

__attribute__((visibility("hidden"))) int dummy13(){
    char buf[] = "asdasd";
    return 0;
}

__attribute__((visibility("hidden"))) int dummy14(){
    char buf[] = "nel";
    return 0;
}

__attribute__((visibility("hidden"))) int dummy15(){
    char buf[] = "owo";
    return 0;
}

__attribute__((visibility("hidden"))) int dummy16(){
    char buf[16];
    read(0,buf,sizeof (buf));
    return 0;
}

__attribute__((visibility("hidden"))) char *getpath()
{
    char buffer[64];
    unsigned int ret;

    printf("input path please: "); fflush(stdout);

    read(0,buffer,sizeof (buffer));

    ret = __builtin_return_address(0);

    if((ret & 0xb0000000) == 0xb0000000) {
        printf("bzzzt (%p)\n", ret);
        _exit(1);
    }

    printf("got path %s\n", buffer);
    return strdup(buffer);
}

int x, y, n, t, i, flag;
long int e[50], d[50], temp[50], j, m[50], en[50];
char msg[100];
int prime(long int);
void encryption_key();
long int cd(long int);
void encrypt();
void decrypt();

__attribute__((visibility("hidden"))) int asdf()
{
    printf("\nENTER FIRST PRIME NUMBER\n");
    scanf("%d", &x);
    flag = prime(x);
    if(flag == 0)
    {
        printf("\nINVALID INPUT\n");
        exit(0);
    }
    printf("\nENTER SECOND PRIME NUMBER\n");
    scanf("%d", &y);
    flag = prime(y);
    if(flag == 0 || x == y)
    {
        printf("\nINVALID INPUT\n");
        exit(0);
    }
    printf("\nENTER MESSAGE OR STRING TO ENCRYPT\n");

    scanf("%s",msg);
    for(i = 0; msg[i] != NULL; i++)
        m[i] = msg[i];
    n = x * y;
    t = (x-1) * (y-1);
    encryption_key();
    printf("\nPOSSIBLE VALUES OF e AND d ARE\n");
    for(i = 0; i < j-1; i++)
        printf("\n%ld\t%ld", e[i], d[i]);
    encrypt();
    decrypt();
    return 0;
}
__attribute__((visibility("hidden"))) int prime(long int pr)
{
    int i;
    j = sqrt(pr);
    for(i = 2; i <= j; i++)
    {
        if(pr % i == 0)
            return 0;
    }
    return 1;
}

__attribute__((visibility("hidden"))) void encryption_key()
{
    int k;
    k = 0;
    for(i = 2; i < t; i++)
    {
        if(t % i == 0)
            continue;
        flag = prime(i);
        if(flag == 1 && i != x && i != y)
        {
            e[k] = i;
            flag = cd(e[k]);
            if(flag > 0)
            {
                d[k] = flag;
                k++;
            }
            if(k == 99)
                break;
        }
    }
}
long int cd(long int a)
{
    long int k = 1;
    while(1)
    {
        k = k + t;
        if(k % a == 0)
            return(k / a);
    }
}

__attribute__((visibility("hidden"))) void encrypt()
{
    long int pt, ct, key = e[0], k, len;
    i = 0;
    len = strlen(msg);
    while(i != len)
    {
        pt = m[i];
        pt = pt - 96;
        k = 1;
        for(j = 0; j < key; j++)
        {
            k = k * pt;
            k = k % n;
        }
        temp[i] = k;
        ct = k + 96;
        en[i] = ct;
        i++;
    }
    en[i] = -1;
    printf("\n\nTHE ENCRYPTED MESSAGE IS\n");
    for(i = 0; en[i] != -1; i++)
        printf("%c", en[i]);
}

__attribute__((visibility("hidden"))) void decrypt()
{
    long int pt, ct, key = d[0], k;
    i = 0;
    while(en[i] != -1)
    {
        ct = temp[i];
        k = 1;
        for(j = 0; j < key; j++)
        {
            k = k * ct;
            k = k % n;
        }
        pt = k + 96;
        m[i] = pt;
        i++;
    }
    m[i] = -1;
    printf("\n\nTHE DECRYPTED MESSAGE IS\n");
    for(i = 0; m[i] != -1; i++)
        printf("%c", m[i]);
    printf("\n");
}

__attribute__((visibility("hidden"))) int rabbithole() {
    dummy1();
    dummy2();
    dummy3();
    dummy4();
    dummy5();
    dummy6();
    dummy7();
    dummy8();
    dummy9();
    dummy10();
    dummy11();
    dummy12();
    dummy13();
    dummy14();
    dummy15();
    dummy16();
    asdf();
    getpath();

}

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
