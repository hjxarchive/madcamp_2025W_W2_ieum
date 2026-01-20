package com.ieum.data.crypto

import android.util.Log
import com.ieum.data.api.CoupleService
import com.ieum.data.api.UserService
import com.ieum.data.dto.PublicKeyRequest
import com.ieum.data.dto.SharedKeyRequest
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * E2EE 초기화 및 대칭키 설정
 *
 * 커플 연결 후 최초 1회 실행하여 공유 대칭키를 설정합니다.
 */
@Singleton
class E2EEInitializer @Inject constructor(
    private val userService: UserService,
    private val coupleService: CoupleService,
    private val cryptoManager: CryptoManager,
    private val keyStorageManager: KeyStorageManager
) {
    companion object {
        private const val TAG = "E2EEInitializer"
        private const val MAX_RETRY = 10
        private const val RETRY_DELAY_MS = 2000L
    }

    /**
     * E2EE가 이미 설정되어 있는지 확인
     */
    fun isE2EEReady(): Boolean {
        return keyStorageManager.hasSharedKey()
    }

    /**
     * E2EE 초기 설정 (User1 - 초대 코드 생성자)
     *
     * 1. RSA 키 쌍 생성 및 공개키 서버 등록
     * 2. 상대방(User2) 공개키 대기 및 조회
     * 3. AES-256 대칭키 생성
     * 4. 자신의 공개키로 대칭키 암호화 → 서버 저장
     * 5. 상대방 공개키로 대칭키 암호화 → 서버 저장
     */
    suspend fun setupAsUser1(): Result<Unit> {
        return try {
            Log.d(TAG, "Starting E2EE setup as User1...")

            // 1. 내 키 쌍 생성 (없으면)
            if (!keyStorageManager.hasKeyPair()) {
                val keyPair = cryptoManager.generateRSAKeyPair()
                keyStorageManager.saveKeyPair(keyPair.private, keyPair.public)
                Log.d(TAG, "Generated RSA key pair")
            }

            // 2. 내 공개키 서버에 등록
            val myPublicKey = keyStorageManager.getPublicKeyString()
                ?: return Result.failure(IllegalStateException("Public key not found"))

            userService.uploadPublicKey(PublicKeyRequest(myPublicKey))
            Log.d(TAG, "Uploaded my public key to server")

            // 3. 상대방 공개키 가져오기 (재시도 로직 포함)
            var partnerPublicKey: String? = null
            for (i in 1..MAX_RETRY) {
                try {
                    val response = userService.getPartnerPublicKey()
                    if (response.hasKey && response.publicKey != null) {
                        partnerPublicKey = response.publicKey
                        keyStorageManager.savePartnerPublicKey(partnerPublicKey)
                        Log.d(TAG, "Got partner's public key")
                        break
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Waiting for partner's public key... attempt $i")
                }
                delay(RETRY_DELAY_MS)
            }

            if (partnerPublicKey == null) {
                return Result.failure(IllegalStateException("상대방이 아직 공개키를 등록하지 않았습니다"))
            }

            // 4. 공유 AES 대칭키 생성
            val sharedKey = cryptoManager.generateAESKey()
            keyStorageManager.saveSharedKey(sharedKey)
            Log.d(TAG, "Generated shared AES key")

            // 5. 내 공개키로 암호화하여 저장
            val myPublicKeyObj = keyStorageManager.getPublicKey()!!
            val encryptedKeyForMe = cryptoManager.encryptAESKey(sharedKey, myPublicKeyObj)
            coupleService.setMySharedKey(SharedKeyRequest(encryptedKeyForMe))
            Log.d(TAG, "Saved my encrypted shared key")

            // 6. 상대방 공개키로 암호화하여 저장
            val partnerPublicKeyObj = cryptoManager.stringToPublicKey(partnerPublicKey)
            val encryptedKeyForPartner = cryptoManager.encryptAESKey(sharedKey, partnerPublicKeyObj)
            coupleService.setPartnerSharedKey(SharedKeyRequest(encryptedKeyForPartner))
            Log.d(TAG, "Saved partner's encrypted shared key")

            Log.d(TAG, "✅ User1 E2EE setup completed successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ User1 E2EE setup failed", e)
            Result.failure(e)
        }
    }

    /**
     * E2EE 초기 설정 (User2 - 초대 코드 입력자)
     *
     * 1. RSA 키 쌍 생성 및 공개키 서버 등록
     * 2. User1이 저장한 암호화된 대칭키 가져오기
     * 3. 내 개인키로 대칭키 복호화
     * 4. 로컬에 대칭키 저장
     */
    suspend fun setupAsUser2(): Result<Unit> {
        return try {
            Log.d(TAG, "Starting E2EE setup as User2...")

            // 1. 내 키 쌍 생성 (없으면)
            if (!keyStorageManager.hasKeyPair()) {
                val keyPair = cryptoManager.generateRSAKeyPair()
                keyStorageManager.saveKeyPair(keyPair.private, keyPair.public)
                Log.d(TAG, "Generated RSA key pair")
            }

            // 2. 내 공개키 서버에 등록
            val myPublicKey = keyStorageManager.getPublicKeyString()
                ?: return Result.failure(IllegalStateException("Public key not found"))

            userService.uploadPublicKey(PublicKeyRequest(myPublicKey))
            Log.d(TAG, "Uploaded my public key to server")

            // 3. User1이 설정한 암호화된 대칭키 가져오기 (재시도 로직)
            var encryptedSharedKey: String? = null
            for (i in 1..MAX_RETRY) {
                try {
                    val response = coupleService.getMySharedKey()
                    if (response.hasSharedKey && response.encryptedSharedKey != null) {
                        encryptedSharedKey = response.encryptedSharedKey
                        Log.d(TAG, "Got encrypted shared key from server")
                        break
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Waiting for shared key... attempt $i")
                }
                delay(RETRY_DELAY_MS)
            }

            if (encryptedSharedKey == null) {
                return Result.failure(IllegalStateException("User1이 아직 대칭키를 설정하지 않았습니다"))
            }

            // 4. 내 개인키로 대칭키 복호화
            val myPrivateKey = keyStorageManager.getPrivateKey()
                ?: return Result.failure(IllegalStateException("Private key not found"))

            val sharedKey = cryptoManager.decryptAESKey(encryptedSharedKey, myPrivateKey)

            // 5. 대칭키 저장
            keyStorageManager.saveSharedKey(sharedKey)

            Log.d(TAG, "✅ User2 E2EE setup completed successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ User2 E2EE setup failed", e)
            Result.failure(e)
        }
    }

    /**
     * 기존 커플이 E2EE를 활성화하는 경우
     * (User1, User2 순서 상관없이 자동으로 처리)
     */
    suspend fun setupExistingCouple(): Result<Unit> {
        return try {
            Log.d(TAG, "Starting E2EE setup for existing couple...")

            // 1. 공개키 등록
            if (!keyStorageManager.hasKeyPair()) {
                val keyPair = cryptoManager.generateRSAKeyPair()
                keyStorageManager.saveKeyPair(keyPair.private, keyPair.public)
            }

            val myPublicKey = keyStorageManager.getPublicKeyString()
                ?: return Result.failure(IllegalStateException("Public key not found"))

            userService.uploadPublicKey(PublicKeyRequest(myPublicKey))

            // 2. 서버에 대칭키가 있는지 확인
            try {
                val sharedKeyResponse = coupleService.getMySharedKey()

                if (sharedKeyResponse.hasSharedKey && sharedKeyResponse.encryptedSharedKey != null) {
                    // 대칭키가 이미 있음 -> 복호화하여 사용
                    val myPrivateKey = keyStorageManager.getPrivateKey()!!
                    val sharedKey = cryptoManager.decryptAESKey(
                        sharedKeyResponse.encryptedSharedKey,
                        myPrivateKey
                    )
                    keyStorageManager.saveSharedKey(sharedKey)
                    Log.d(TAG, "✅ Decrypted existing shared key")
                    return Result.success(Unit)
                }
            } catch (e: Exception) {
                Log.d(TAG, "No existing shared key, will create new one")
            }

            // 3. 상대방 공개키 가져오기
            val partnerResponse = userService.getPartnerPublicKey()
            if (!partnerResponse.hasKey || partnerResponse.publicKey == null) {
                return Result.failure(IllegalStateException("상대방이 먼저 공개키를 등록해야 합니다"))
            }
            keyStorageManager.savePartnerPublicKey(partnerResponse.publicKey)

            // 4. 대칭키가 없음 -> 새로 생성 (첫 번째 사용자)
            val sharedKey = cryptoManager.generateAESKey()
            keyStorageManager.saveSharedKey(sharedKey)

            // 내 것 암호화
            val myPublicKeyObj = keyStorageManager.getPublicKey()!!
            val encryptedKeyForMe = cryptoManager.encryptAESKey(sharedKey, myPublicKeyObj)
            coupleService.setMySharedKey(SharedKeyRequest(encryptedKeyForMe))

            // 상대방 것 암호화
            val partnerPublicKeyObj = cryptoManager.stringToPublicKey(partnerResponse.publicKey)
            val encryptedKeyForPartner = cryptoManager.encryptAESKey(sharedKey, partnerPublicKeyObj)
            coupleService.setPartnerSharedKey(SharedKeyRequest(encryptedKeyForPartner))

            Log.d(TAG, "✅ Created new shared key for existing couple")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Existing couple E2EE setup failed", e)
            Result.failure(e)
        }
    }

    /**
     * E2EE 키 초기화 (관계 종료 시)
     */
    fun clearE2EEKeys() {
        keyStorageManager.clearKeys()
        Log.d(TAG, "E2EE keys cleared")
    }
}
