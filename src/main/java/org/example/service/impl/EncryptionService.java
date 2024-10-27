package org.example.service.impl;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncryptionService {

    private static final EncryptionService instance = new EncryptionService();

    private EncryptionService() {
    }

    public static EncryptionService getInstance() {
        return instance;
    }

    public SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    public String encodeKey(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public boolean isValidAESKey(String key) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(key);
            int keyLength = decodedKey.length;
            return keyLength == 16 || keyLength == 24 || keyLength == 32;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public SecretKey decodeKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}
