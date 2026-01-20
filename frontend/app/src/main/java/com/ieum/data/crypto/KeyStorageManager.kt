package com.ieum.data.crypto

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * E2EE 키 저장소 (EncryptedSharedPreferences에 안전하게 저장)
 */
@Singleton
class KeyStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "e2ee_keys",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_PRIVATE_KEY = "private_key"
        private const val KEY_PUBLIC_KEY = "public_key"
        private const val KEY_PARTNER_PUBLIC_KEY = "partner_public_key"
        private const val KEY_SHARED_AES_KEY = "shared_aes_key"
    }

    /**
     * 키 쌍이 존재하는지 확인
     */
    fun hasKeyPair(): Boolean {
        return prefs.contains(KEY_PRIVATE_KEY) && prefs.contains(KEY_PUBLIC_KEY)
    }

    /**
     * 키 쌍 저장
     */
    fun saveKeyPair(privateKey: PrivateKey, publicKey: PublicKey) {
        prefs.edit().apply {
            putString(KEY_PRIVATE_KEY, Base64.encodeToString(privateKey.encoded, Base64.NO_WRAP))
            putString(KEY_PUBLIC_KEY, Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP))
            apply()
        }
    }

    /**
     * 개인키 가져오기
     */
    fun getPrivateKey(): PrivateKey? {
        val keyString = prefs.getString(KEY_PRIVATE_KEY, null) ?: return null
        val keyBytes = Base64.decode(keyString, Base64.NO_WRAP)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(keySpec)
    }

    /**
     * 공개키 가져오기
     */
    fun getPublicKey(): PublicKey? {
        val keyString = prefs.getString(KEY_PUBLIC_KEY, null) ?: return null
        val keyBytes = Base64.decode(keyString, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    /**
     * 공개키 문자열 가져오기 (서버 전송용)
     */
    fun getPublicKeyString(): String? {
        return prefs.getString(KEY_PUBLIC_KEY, null)
    }

    /**
     * 상대방 공개키 저장
     */
    fun savePartnerPublicKey(publicKeyString: String) {
        prefs.edit().putString(KEY_PARTNER_PUBLIC_KEY, publicKeyString).apply()
    }

    /**
     * 상대방 공개키 가져오기
     */
    fun getPartnerPublicKey(): PublicKey? {
        val keyString = prefs.getString(KEY_PARTNER_PUBLIC_KEY, null) ?: return null
        val keyBytes = Base64.decode(keyString, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    /**
     * 공유 대칭키 저장 (커플 전용)
     */
    fun saveSharedKey(sharedKey: SecretKey) {
        val keyString = Base64.encodeToString(sharedKey.encoded, Base64.NO_WRAP)
        prefs.edit().putString(KEY_SHARED_AES_KEY, keyString).apply()
    }

    /**
     * 공유 대칭키 가져오기
     */
    fun getSharedKey(): SecretKey? {
        val keyString = prefs.getString(KEY_SHARED_AES_KEY, null) ?: return null
        val keyBytes = Base64.decode(keyString, Base64.NO_WRAP)
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * 공유 대칭키 존재 여부
     */
    fun hasSharedKey(): Boolean {
        return prefs.contains(KEY_SHARED_AES_KEY)
    }

    /**
     * 모든 키 삭제
     */
    fun clearKeys() {
        prefs.edit().clear().apply()
    }
}
