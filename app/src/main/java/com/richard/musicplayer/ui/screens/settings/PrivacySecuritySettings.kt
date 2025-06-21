package com.richard.musicplayer.ui.screens.settings

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.richard.musicplayer.LocalPlayerAwareWindowInsets
import com.richard.musicplayer.R
import com.richard.musicplayer.ui.component.*
import com.richard.musicplayer.ui.utils.backToMain
import com.richard.musicplayer.viewmodels.PrivacySecurityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecuritySettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: PrivacySecurityViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showAuthenticationDialog by remember { mutableStateOf(false) }
    var pendingBiometricChange by remember { mutableStateOf<Boolean?>(null) }
    var authErrorMessage by remember { mutableStateOf("") }
    var showAuthError by remember { mutableStateOf(false) }
    
    // Verificar disponibilidade da biometria
    val biometricManager = BiometricManager.from(context)
    val biometricStatus = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )
    val canUseBiometric = biometricStatus == BiometricManager.BIOMETRIC_SUCCESS
    
    // Função para mostrar prompt de autenticação
    fun showAuthenticationPrompt(newValue: Boolean) {
        val activity = context as? FragmentActivity ?: context as? ComponentActivity
        if (activity !is FragmentActivity) {
            authErrorMessage = "Erro: Contexto incompatível com autenticação"
            showAuthError = true
            return
        }
        
        if (!canUseBiometric) {
            authErrorMessage = "Autenticação biométrica não disponível"
            showAuthError = true
            return
        }
        
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED -> {
                            // Usuário cancelou - não fazer nada, manter configuração atual
                            authErrorMessage = "Operação cancelada pelo usuário"
                            showAuthError = false
                        }
                        else -> {
                            authErrorMessage = "Erro de autenticação: $errString"
                            showAuthError = true
                        }
                    }
                    pendingBiometricChange = null
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Autenticação bem-sucedida - aplicar a mudança
                    pendingBiometricChange?.let { newValue ->
                        viewModel.setBiometricLock(newValue)
                    }
                    pendingBiometricChange = null
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    authErrorMessage = "Autenticação falhou. Tente novamente."
                    showAuthError = true
                }
            })

        try {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("🔐 Confirmação de Segurança")
                .setSubtitle("Desativar bloqueio biométrico")
                .setDescription("Para sua segurança, confirme sua identidade antes de desativar o bloqueio biométrico")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            authErrorMessage = "Erro ao inicializar autenticação: ${e.message}"
            showAuthError = true
            pendingBiometricChange = null
        }
    }
    
    // Função para lidar com mudança do switch
    fun handleBiometricToggle(newValue: Boolean) {
        // REGRA: Só pedir autenticação ao DESABILITAR (true -> false)
        val tryingToDisable = state.biometricLock == true && newValue == false
        
        if (tryingToDisable) {
            // Exigir autenticação para desabilitar proteção
            pendingBiometricChange = newValue
            showAuthenticationPrompt(newValue)
        } else {
            // Permitir ativação ou outras mudanças sem autenticação
            viewModel.setBiometricLock(newValue)
        }
    }
    
    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
    ) {
        // Segurança - Bloqueio Biométrico com autenticação obrigatória
        PreferenceGroupTitle(title = "Segurança")
        
        SwitchPreference(
            title = { Text("Bloqueio Biométrico") },
            description = if (state.biometricLock) {
                "Ativo - Autenticação necessária para desativar"
            } else {
                "Usar impressão digital ou face para desbloquear"
            },
            icon = { Icon(Icons.Rounded.Fingerprint, null) },
            checked = state.biometricLock,
            onCheckedChange = ::handleBiometricToggle
        )
        
        // Aviso de segurança quando biometria estiver ativa
        if (state.biometricLock) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🔐 Proteção Ativa",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "O bloqueio biométrico está ativo. Para desativar esta proteção, você precisará autenticar-se novamente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Som de Desbloqueio
        SwitchPreference(
            title = { Text("Som de Desbloqueio") },
            description = if (state.unlockSoundEnabled) {
                "Reproduz um som digital quando o app é desbloqueado"
            } else {
                "Som desabilitado - desbloqueio silencioso"
            },
            icon = { Icon(Icons.Rounded.VolumeUp, null) },
            checked = state.unlockSoundEnabled,
            onCheckedChange = viewModel::setUnlockSoundEnabled
        )
        
        // Dados e Análises
        PreferenceGroupTitle(title = "Dados e Análises")
        
        Spacer(Modifier.height(16.dp))
    }
    
    // Snackbar para mensagens de erro de autenticação
    if (showAuthError && authErrorMessage.isNotEmpty()) {
        LaunchedEffect(authErrorMessage) {
            kotlinx.coroutines.delay(3000)
            showAuthError = false
            authErrorMessage = ""
        }
        
        SnackbarHost(
            hostState = remember { SnackbarHostState() }
        ) {
            Snackbar(
                action = {
                    TextButton(onClick = { showAuthError = false }) {
                        Text("OK")
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(authErrorMessage)
            }
        }
    }
    
    TopAppBar(
        title = { Text("Privacidade e Segurança") },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
} 