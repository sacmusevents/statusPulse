package com.signalapp

import android.util.Base64
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val ENCRYPTION_PREFIX = "ENC:"
    private const val RSA_ALGORITHM = "RSA/ECB/PKCS1Padding"
    private const val AES_ALGORITHM = "AES/GCM/NoPadding"
    private const val AES_KEY_SIZE = 256
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    // Hardcoded public key (safe to include in APK)
    private const val PUBLIC_KEY_BASE64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtuKo+NSv+eYv6i02JOuc" +
            "mf/oUkd5+VVYtwT3xz0ipkNsR2oDCLTVrb08F3QFz7XbwUpghJq6jfCc9aw+jRpq" +
            "6g3cusQ8oS9u7HQDqv99XhInGWaIMoQlJRFqvB9LtwKsQ4oKPvy7YvAYGuoh7Q4k" +
            "4LhuyKRNl1f3jDtNtho2f82cGVHP7+T5tUtsV/swGp5ktkw15EWDEgyTwPxFJvTW" +
            "UEboxUauAobBePrxkhoUaDSzOy8R9bSKgcWOkBRGDsmcg/DyOc5JGEb7bskJ4YG9" +
            "R8rmDaWGNGx/er+8CZhx+9G+Rwe6BBl6UtrnqbrL6ltQSM6Ojj07OvKtoWWym8CP" +
            "gQIDAQAB"

    private fun getPublicKey(): java.security.PublicKey? {
        return try {
            val decoded = Base64.decode(PUBLIC_KEY_BASE64, Base64.DEFAULT)
            val spec = X509EncodedKeySpec(decoded)
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePublic(spec)
        } catch (e: Exception) {
            android.util.Log.e("CryptoManager", "Error loading public key", e)
            null
        }
    }

    private fun getPrivateKey(): java.security.PrivateKey? {
        return try {
            val privateKeyPem = BuildConfig.RSA_PRIVATE_KEY
            if (privateKeyPem.isEmpty()) {
                android.util.Log.e("CryptoManager", "Private key not configured in BuildConfig")
                null
            } else {
                val cleanKey = privateKeyPem
                    .replace("\\n", "\n")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .trim()

                val decoded = Base64.decode(cleanKey, Base64.DEFAULT)
                val spec = PKCS8EncodedKeySpec(decoded)
                val keyFactory = KeyFactory.getInstance("RSA")
                keyFactory.generatePrivate(spec)
            }
        } catch (e: Exception) {
            android.util.Log.e("CryptoManager", "Error loading private key", e)
            null
        }
    }

    /**
     * Encrypts a key using hybrid encryption (AES + RSA)
     * Returns encrypted key with "ENC:" prefix
     * Format: ENC:base64(encryptedAESKey)::base64(encryptedKeyData)::base64(iv)
     */
    fun encryptKey(plainKey: String): String? {
        return try {
            val publicKey = getPublicKey() ?: return null

            // Generate random AES key
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(AES_KEY_SIZE, SecureRandom())
            val aesKey = keyGen.generateKey()

            // Generate random IV for GCM
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)

            // Encrypt the plaintext with AES-GCM
            val aesCipher = Cipher.getInstance(AES_ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec)
            val encryptedKeyData = aesCipher.doFinal(plainKey.toByteArray())

            // Encrypt the AES key with RSA
            val rsaCipher = Cipher.getInstance(RSA_ALGORITHM)
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)
            val encryptedAESKey = rsaCipher.doFinal(aesKey.encoded)

            // Combine: ENC:base64(encryptedAESKey)::base64(encryptedKeyData)::base64(iv)
            val encryptedAESKeyB64 = Base64.encodeToString(encryptedAESKey, Base64.NO_WRAP)
            val encryptedKeyDataB64 = Base64.encodeToString(encryptedKeyData, Base64.NO_WRAP)
            val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)

            ENCRYPTION_PREFIX + encryptedAESKeyB64 + "::" + encryptedKeyDataB64 + "::" + ivB64
        } catch (e: Exception) {
            android.util.Log.e("CryptoManager", "Error encrypting key", e)
            null
        }
    }

    /**
     * Decrypts a key using hybrid encryption (AES + RSA)
     * Expects encrypted key with "ENC:" prefix and format:
     * ENC:base64(encryptedAESKey)::base64(encryptedKeyData)::base64(iv)
     */
    fun decryptKey(encryptedKey: String): String? {
        return try {
            if (!encryptedKey.startsWith(ENCRYPTION_PREFIX)) {
                return null // Not encrypted
            }

            val privateKey = getPrivateKey() ?: return null
            val keyWithoutPrefix = encryptedKey.substring(ENCRYPTION_PREFIX.length)
            val parts = keyWithoutPrefix.split("::")

            if (parts.size != 3) {
                android.util.Log.e("CryptoManager", "Invalid encrypted key format")
                return null
            }

            val encryptedAESKeyB64 = parts[0]
            val encryptedKeyDataB64 = parts[1]
            val ivB64 = parts[2]

            // Decode from base64
            val encryptedAESKey = Base64.decode(encryptedAESKeyB64, Base64.NO_WRAP)
            val encryptedKeyData = Base64.decode(encryptedKeyDataB64, Base64.NO_WRAP)
            val iv = Base64.decode(ivB64, Base64.NO_WRAP)

            // Decrypt AES key with RSA
            val rsaCipher = Cipher.getInstance(RSA_ALGORITHM)
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
            val aesKeyBytes = rsaCipher.doFinal(encryptedAESKey)
            val aesKey = SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.size, "AES")

            // Decrypt the plaintext with AES-GCM
            val aesCipher = Cipher.getInstance(AES_ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec)
            val decryptedBytes = aesCipher.doFinal(encryptedKeyData)

            String(decryptedBytes)
        } catch (e: Exception) {
            android.util.Log.e("CryptoManager", "Error decrypting key", e)
            null
        }
    }

    /**
     * Checks if a key is encrypted
     */
    fun isEncrypted(key: String): Boolean {
        return key.startsWith(ENCRYPTION_PREFIX)
    }
}
