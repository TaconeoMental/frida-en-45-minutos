package com.nivel4.Cipher;

import static com.nivel4.Cipher.EncryptionUtils.concatArrays;
import static com.nivel4.Cipher.EncryptionUtils.hexToBytes;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptDecrypt {

    public static SecretKey secretKey;

    public static String encrypt(String plaintext, SecretKey secretKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
                Cipher cipher = Cipher.getInstance("DESede");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] ciphertextBytes = cipher.doFinal(plaintext.getBytes());
                return Base64.getEncoder().encodeToString(ciphertextBytes);
            }


    public static String decrypt(String ciphertext, SecretKey secretKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
                Cipher cipher = Cipher.getInstance("DESede");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);
                byte[] plaintextBytes = cipher.doFinal(ciphertextBytes);
                return new String(plaintextBytes);
            }

    public static SecretKey generatePartialKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede");
        keyGenerator.init(168);
        SecretKey partialKey = keyGenerator.generateKey();
        byte[] keyBytes = partialKey.getEncoded();
        byte[] truncatedKeyBytes = Arrays.copyOf(keyBytes, 12);
        SecretKey truncatedKey = new SecretKeySpec(truncatedKeyBytes, "DESede");
        return truncatedKey;
    }

    public static SecretKey setSecretKey(String serverKeyStr) {
        byte[] serverKeyBytes = hexToBytes(serverKeyStr);
        SecretKey secretKey = new SecretKeySpec(serverKeyBytes, "DESede");
        return secretKey;
    }



}
