package org.example.util;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncryptionUtil {

    private EncryptionUtil() {
    }

    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algorithm not found");
            return null;
        }
    }


    public static String encodeKey(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static boolean isValidAESKey(String key) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(key);
            int keyLength = decodedKey.length;
            return keyLength == 16 || keyLength == 24 || keyLength == 32;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static SecretKey decodeKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    public static void validateKey(String key) {
        if (key != null && !isValidAESKey(key)) {
            throw new IllegalArgumentException("Invalid AES key.");
        }

    }


}
