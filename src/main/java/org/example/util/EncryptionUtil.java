package org.example.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Base64;

public class EncryptionUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private EncryptionUtil() { }

    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "BC");
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.out.println("Algorithm not found❗");
            return null;
        }
    }

    public static String encodeKey(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static boolean isValidAESKey(String key) {
        if (key == null) {
            return false;
        }
        try {
            byte[] decodedKey = Base64.getDecoder().decode(key);
            int keyLength = decodedKey.length;
            return keyLength == 16 || keyLength == 24 || keyLength == 32;
        } catch (IllegalArgumentException e) {
            System.out.println("Failed to decode key❗: " + e.getMessage());
            return false;
        }
    }

    public static SecretKey decodeKey(String encodedKey) {
        if (!isValidAESKey(encodedKey)) {
            throw new IllegalArgumentException("Invalid AES key❗.");
        }
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, "AES");
    }

    public static void validateKey(String key) {
        if (!isValidAESKey(key)) {
            throw new IllegalArgumentException("Invalid AES key. Ensure it is 128, 192, or 256 bits.");
        }
    }
}
