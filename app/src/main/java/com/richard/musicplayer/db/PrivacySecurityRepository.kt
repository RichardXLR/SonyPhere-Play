package com.richard.musicplayer.db

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "privacy_security_settings")

@Singleton
class PrivacySecurityRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val INCOGNITO_MODE = booleanPreferencesKey("incognito_mode")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
        val CLEAR_DATA_ON_EXIT = booleanPreferencesKey("clear_data_on_exit")
        val ENCRYPT_BACKUPS = booleanPreferencesKey("encrypt_backups")
        val CRASH_REPORTS_ENABLED = booleanPreferencesKey("crash_reports_enabled")
        val FORCE_HTTPS = booleanPreferencesKey("force_https")
        val BLOCK_TRACKERS = booleanPreferencesKey("block_trackers")
        val ENCRYPTED_PIN = stringPreferencesKey("encrypted_pin")
        val PIN_IV = stringPreferencesKey("pin_iv")
    }

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "outertune_pin_key"

    init {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    val settingsFlow: Flow<Map<Preferences.Key<*>, Boolean>> = context.dataStore.data
        .map { preferences ->
            buildMap {
                put(PreferencesKeys.INCOGNITO_MODE, preferences[PreferencesKeys.INCOGNITO_MODE] ?: false)
                put(PreferencesKeys.BIOMETRIC_LOCK, preferences[PreferencesKeys.BIOMETRIC_LOCK] ?: false)
                put(PreferencesKeys.CLEAR_DATA_ON_EXIT, preferences[PreferencesKeys.CLEAR_DATA_ON_EXIT] ?: false)
                put(PreferencesKeys.ENCRYPT_BACKUPS, preferences[PreferencesKeys.ENCRYPT_BACKUPS] ?: true)
                put(PreferencesKeys.CRASH_REPORTS_ENABLED, preferences[PreferencesKeys.CRASH_REPORTS_ENABLED] ?: true)
                put(PreferencesKeys.FORCE_HTTPS, preferences[PreferencesKeys.FORCE_HTTPS] ?: true)
                put(PreferencesKeys.BLOCK_TRACKERS, preferences[PreferencesKeys.BLOCK_TRACKERS] ?: true)
            }
        }

    suspend fun updateSetting(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun clearHistory() {
        // TODO: Implementar limpeza do histórico
    }

    suspend fun clearAllData() {
        context.dataStore.edit { it.clear() }
        // TODO: Implementar limpeza de outros dados do aplicativo
    }

    suspend fun hasPin(): Boolean {
        return context.dataStore.data.first()[PreferencesKeys.ENCRYPTED_PIN] != null
    }

    suspend fun verifyPin(pin: String): Boolean {
        val preferences = context.dataStore.data.first()
        val encryptedPin = preferences[PreferencesKeys.ENCRYPTED_PIN] ?: return false
        val iv = Base64.getDecoder().decode(preferences[PreferencesKeys.PIN_IV])
        
        val decryptedPin = decryptPin(Base64.getDecoder().decode(encryptedPin), iv)
        return decryptedPin == pin
    }

    suspend fun updatePin(newPin: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            keyStore.getKey(keyAlias, null) as SecretKey
        )

        val encrypted = cipher.doFinal(newPin.toByteArray())
        val iv = cipher.iv

        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENCRYPTED_PIN] = Base64.getEncoder().encodeToString(encrypted)
            preferences[PreferencesKeys.PIN_IV] = Base64.getEncoder().encodeToString(iv)
        }
    }

    private fun decryptPin(encryptedPin: ByteArray, iv: ByteArray): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(
            Cipher.DECRYPT_MODE,
            keyStore.getKey(keyAlias, null),
            spec
        )

        return String(cipher.doFinal(encryptedPin))
    }

    companion object {
        fun getPreferencesKey(name: String): Preferences.Key<Boolean> {
            return when (name) {
                "incognito_mode" -> PreferencesKeys.INCOGNITO_MODE
                "biometric_lock" -> PreferencesKeys.BIOMETRIC_LOCK
                "clear_data_on_exit" -> PreferencesKeys.CLEAR_DATA_ON_EXIT
                "encrypt_backups" -> PreferencesKeys.ENCRYPT_BACKUPS
                "crash_reports_enabled" -> PreferencesKeys.CRASH_REPORTS_ENABLED
                "force_https" -> PreferencesKeys.FORCE_HTTPS
                "block_trackers" -> PreferencesKeys.BLOCK_TRACKERS
                else -> throw IllegalArgumentException("Chave de preferência desconhecida: $name")
            }
        }
    }
} 