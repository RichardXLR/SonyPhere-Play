package com.richard.musicplayer.viewmodels

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.richard.musicplayer.db.PrivacySecurityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class PrivacySecurityState(
    val incognitoMode: Boolean = false,
    val biometricLock: Boolean = false,
    val clearDataOnExit: Boolean = false,
    val encryptBackups: Boolean = true,
    val crashReportsEnabled: Boolean = true,
    val forceHttps: Boolean = true,
    val blockTrackers: Boolean = true,
    val unlockSoundEnabled: Boolean = true,
    val hasPin: Boolean = false
)

@HiltViewModel
class PrivacySecurityViewModel @Inject constructor(
    private val repository: PrivacySecurityRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(PrivacySecurityState())
    val state: StateFlow<PrivacySecurityState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settingsFlow.collect { settings ->
                _state.value = PrivacySecurityState(
                    incognitoMode = settings[PrivacySecurityRepository.getPreferencesKey("incognito_mode")] as Boolean,
                    biometricLock = settings[PrivacySecurityRepository.getPreferencesKey("biometric_lock")] as Boolean,
                    clearDataOnExit = settings[PrivacySecurityRepository.getPreferencesKey("clear_data_on_exit")] as Boolean,
                    encryptBackups = settings[PrivacySecurityRepository.getPreferencesKey("encrypt_backups")] as Boolean,
                    crashReportsEnabled = settings[PrivacySecurityRepository.getPreferencesKey("crash_reports_enabled")] as Boolean,
                    forceHttps = settings[PrivacySecurityRepository.getPreferencesKey("force_https")] as Boolean,
                    blockTrackers = settings[PrivacySecurityRepository.getPreferencesKey("block_trackers")] as Boolean,
                    unlockSoundEnabled = settings[PrivacySecurityRepository.getPreferencesKey("unlock_sound_enabled")] as Boolean,
                    hasPin = repository.hasPin()
                )
            }
        }
    }

    fun setIncognitoMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSetting(PrivacySecurityRepository.getPreferencesKey("incognito_mode"), enabled)
        }
    }

    fun setBiometricLock(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSetting(PrivacySecurityRepository.getPreferencesKey("biometric_lock"), enabled)
        }
    }

    fun setClearDataOnExit(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSetting(PrivacySecurityRepository.getPreferencesKey("clear_data_on_exit"), enabled)
        }
    }

    fun setEncryptBackups(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSetting(PrivacySecurityRepository.getPreferencesKey("encrypt_backups"), enabled)
        }
    }

    fun setCrashReportsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSetting(PrivacySecurityRepository.getPreferencesKey("crash_reports_enabled"), enabled)
        }
    }

    fun setForceHttps(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSetting(PrivacySecurityRepository.getPreferencesKey("force_https"), enabled)
        }
    }

    fun setBlockTrackers(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSetting(PrivacySecurityRepository.getPreferencesKey("block_trackers"), enabled)
        }
    }

    fun setUnlockSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSetting(PrivacySecurityRepository.getPreferencesKey("unlock_sound_enabled"), enabled)
        }
    }

    suspend fun clearHistory() {
        repository.clearHistory()
    }

    suspend fun clearAllData() {
        repository.clearAllData()
    }

    fun getAppSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", "com.richard.musicplayer", null)
        }
    }

    fun updatePin(currentPin: String, newPin: String) {
        viewModelScope.launch {
            if (!repository.hasPin() || repository.verifyPin(currentPin)) {
                repository.updatePin(newPin)
                _state.value = _state.value.copy(hasPin = true)
            } else {
                throw IllegalArgumentException("PIN atual incorreto")
            }
        }
    }
} 