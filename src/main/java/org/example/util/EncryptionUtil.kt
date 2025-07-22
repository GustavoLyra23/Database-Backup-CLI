package org.example.util

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.Security
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object EncryptionUtil {
    init {
        Security.addProvider(BouncyCastleProvider())
    }

    fun generateKey(): SecretKey? {
        try {
            val keyGenerator = KeyGenerator.getInstance("AES", "BC")
            keyGenerator.init(128)
            return keyGenerator.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            println("Algorithm not found❗")
            return null
        } catch (e: NoSuchProviderException) {
            println("Algorithm not found❗")
            return null
        }
    }

    fun encodeKey(key: SecretKey): String? {
        return Base64.getEncoder().encodeToString(key.getEncoded())
    }

    fun isValidAESKey(key: String?): Boolean {
        if (key == null) {
            return false
        }
        try {
            val decodedKey = Base64.getDecoder().decode(key)
            val keyLength = decodedKey.size
            return keyLength == 16 || keyLength == 24 || keyLength == 32
        } catch (e: IllegalArgumentException) {
            println("Failed to decode key❗: " + e.message)
            return false
        }
    }

    @JvmStatic
    fun decodeKey(encodedKey: String?): SecretKey {
        require(isValidAESKey(encodedKey)) { "Invalid AES key❗." }
        val decodedKey = Base64.getDecoder().decode(encodedKey)
        return SecretKeySpec(decodedKey, "AES")
    }

    @JvmStatic
    fun validateKey(key: String?) {
        require(isValidAESKey(key)) { "Invalid AES key. Ensure it is 128, 192, or 256 bits." }
    }
}
