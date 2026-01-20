package com.ieum.data.crypto

import android.util.Base64
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * E2EE 암호화/복호화 관리
 */
@Singleton
class CryptoManager @Inject constructor() {

    companion object {
        private const val RSA_KEY_SIZE = 2048
        private const val AES_KEY_SIZE = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }

    /**
     * RSA 키 쌍 생성 (최초 1회)
     */
    fun generateRSAKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(RSA_KEY_SIZE)
        return keyPairGenerator.generateKeyPair()
    }

    /**
     * 공개키를 Base64 문자열로 변환
     */
    fun publicKeyToString(publicKey: PublicKey): String {
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }

    /**
     * Base64 문자열을 공개키로 변환
     */
    fun stringToPublicKey(publicKeyString: String): PublicKey {
        val keyBytes = Base64.decode(publicKeyString, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    /**
     * Base64 문자열을 개인키로 변환
     */
    fun stringToPrivateKey(privateKeyString: String): PrivateKey {
        val keyBytes = Base64.decode(privateKeyString, Base64.NO_WRAP)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }

    /**
     * 개인키를 Base64 문자열로 변환
     */
    fun privateKeyToString(privateKey: PrivateKey): String {
        return Base64.encodeToString(privateKey.encoded, Base64.NO_WRAP)
    }

    /**
     * AES 세션키 생성
     */
    fun generateAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(AES_KEY_SIZE)
        return keyGenerator.generateKey()
    }

    /**
     * SecretKey를 Base64 문자열로 변환
     */
    fun secretKeyToString(secretKey: SecretKey): String {
        return Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
    }

    /**
     * Base64 문자열을 SecretKey로 변환
     */
    fun stringToSecretKey(keyString: String): SecretKey {
        val keyBytes = Base64.decode(keyString, Base64.NO_WRAP)
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * 메시지 암호화 (AES-256-GCM)
     */
    fun encryptMessage(plainText: String, secretKey: SecretKey): EncryptedData {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        return EncryptedData(
            cipherText = Base64.encodeToString(cipherText, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    /**
     * 메시지 복호화 (AES-256-GCM)
     */
    fun decryptMessage(encryptedData: EncryptedData, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = Base64.decode(encryptedData.iv, Base64.NO_WRAP)
        val cipherText = Base64.decode(encryptedData.cipherText, Base64.NO_WRAP)

        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        val plainTextBytes = cipher.doFinal(cipherText)
        return String(plainTextBytes, Charsets.UTF_8)
    }

    /**
     * AES 키를 상대방 공개키로 암호화 (RSA)
     */
    fun encryptAESKey(aesKey: SecretKey, publicKey: PublicKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedKey = cipher.doFinal(aesKey.encoded)
        return Base64.encodeToString(encryptedKey, Base64.NO_WRAP)
    }

    /**
     * 암호화된 AES 키를 내 개인키로 복호화 (RSA)
     */
    fun decryptAESKey(encryptedKey: String, privateKey: PrivateKey): SecretKey {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val keyBytes = Base64.decode(encryptedKey, Base64.NO_WRAP)
        val decryptedKeyBytes = cipher.doFinal(keyBytes)
        return SecretKeySpec(decryptedKeyBytes, "AES")
    }
}

/**
 * 암호화된 데이터
 */
data class EncryptedData(
    val cipherText: String,  // Base64 암호문
    val iv: String           // Base64 초기화 벡터
)
