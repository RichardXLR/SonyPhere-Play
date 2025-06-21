package com.richard.musicplayer.ui.screens

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import com.richard.musicplayer.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    onBackPressed: () -> Unit,
    audioFeedbackManager: com.richard.musicplayer.utils.AudioFeedbackManager? = null
) {
    val context = LocalContext.current
    val activity = (context as? FragmentActivity) ?: (context as? ComponentActivity)
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isAuthenticating by remember { mutableStateOf(false) }
    var startAnimation by remember { mutableStateOf(false) }
    var unlockSuccess by remember { mutableStateOf(false) }
    
    // Usar apenas o AudioFeedbackManager injetado
    val effectiveAudioManager = audioFeedbackManager
    
    // PRÉ-CARREGAMENTO: Garantir que o som esteja pronto para reprodução instantânea
    LaunchedEffect(effectiveAudioManager) {
        android.util.Log.d("AuthScreen", "🎵 Pré-carregando som de desbloqueio para reprodução instantânea...")
        effectiveAudioManager?.initialize()
    }
    
    // Verificar disponibilidade da biometria
    val biometricManager = BiometricManager.from(context)
    val biometricStatus = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )
    
    val canUseBiometric = biometricStatus == BiometricManager.BIOMETRIC_SUCCESS
    val biometricMessage = when (biometricStatus) {
        BiometricManager.BIOMETRIC_SUCCESS -> "Biometria disponível"
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Hardware biométrico não encontrado"
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Hardware biométrico indisponível"
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Nenhuma biometria cadastrada"
        else -> "Biometria não disponível"
    }
    
    // Detectar tema do sistema
    val isSystemInDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    
    // Cores adaptativas
    val backgroundColor = if (isSystemInDarkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A0E1A), // Azul escuro profundo
                Color(0xFF0F1823), // Azul médio escuro
                Color(0xFF1A2332), // Azul acinzentado
                Color(0xFF0F1823), // Azul médio escuro
                Color(0xFF0A0E1A)  // Azul escuro profundo
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF5F7FA), // Branco azulado claro
                Color(0xFFE9EEF4), // Cinza azulado claro
                Color(0xFFDCE4ED), // Azul claro acinzentado
                Color(0xFFE9EEF4), // Cinza azulado claro
                Color(0xFFF5F7FA)  // Branco azulado claro
            )
        )
    }
    
    val accentColor = if (isSystemInDarkTheme) {
        Color(0xFF4ECCA3) // Verde menta moderno
    } else {
        Color(0xFF059669) // Verde esmeralda
    }
    
    val textColor = if (isSystemInDarkTheme) {
        Color.White
    } else {
        Color(0xFF111827) // Cinza muito escuro
    }
    
    val subtleTextColor = if (isSystemInDarkTheme) {
        Color.White.copy(alpha = 0.6f)
    } else {
        Color(0xFF6B7280) // Cinza médio
    }
    
    // Animações elegantes
    val contentScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "content_scale"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "content_alpha"
    )
    
    // Animação de sucesso
    val successScale by animateFloatAsState(
        targetValue = if (unlockSuccess) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "success_scale"
    )
    
    // Animações infinitas sutis
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // Função para mostrar prompt de biometria
    fun showBiometricPrompt() {
        val fragmentActivity = activity as? FragmentActivity
        if (fragmentActivity == null) {
            errorMessage = "Erro: Contexto incompatível com biometria"
            showError = true
            return
        }
        
        if (!canUseBiometric) {
            errorMessage = biometricMessage
            showError = true
            return
        }
        
        isAuthenticating = true
        showError = false
        
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(fragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    isAuthenticating = false
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED -> {
                            // Usuário cancelou
                            errorMessage = "Autenticação cancelada"
                            showError = false
                        }
                        else -> {
                            errorMessage = "Erro de autenticação: $errString"
                            showError = true
                        }
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    
                    // ⚡ REPRODUÇÃO INSTANTÂNEA: Som ANTES de qualquer mudança de estado/tela
                    android.util.Log.d("AuthScreen", "🔓 Autenticação validada! Reproduzindo som instantaneamente...")
                    val soundStartTime = System.currentTimeMillis()
                    
                    try {
                        // Reproduzir som IMEDIATAMENTE após validação, antes de qualquer outra ação
                        effectiveAudioManager?.playUnlockSuccessInstant()
                        val soundCallTime = System.currentTimeMillis() - soundStartTime
                        android.util.Log.d("AuthScreen", "⚡ Som chamado em ${soundCallTime}ms após validação")
                    } catch (e: Exception) {
                        android.util.Log.e("AuthScreen", "❌ Erro ao reproduzir som", e)
                    }
                    
                    // Atualizar estado após som para manter sincronização
                    isAuthenticating = false
                    unlockSuccess = true
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    errorMessage = "Não reconhecido. Tente novamente."
                    showError = true
                }
            })

        try {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("SonsPhere")
                .setSubtitle("Autenticação Necessária")
                .setDescription("Use sua biometria ou senha para continuar")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            isAuthenticating = false
            errorMessage = "Erro ao inicializar autenticação: ${e.message}"
            showError = true
        }
    }
    
    // Iniciar animação e autenticação automática
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(600)
        if (canUseBiometric) {
            showBiometricPrompt()
        } else {
            errorMessage = biometricMessage
            showError = true
        }
    }
    
    // Lidar com transição após sucesso da autenticação
    LaunchedEffect(unlockSuccess) {
        if (unlockSuccess) {
            delay(300)
            onAuthenticated()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Padrão de fundo sutil - Grade de pontos
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isSystemInDarkTheme) 0.03f else 0.02f)
        ) {
            drawDotPattern(
                color = accentColor,
                spacing = 40.dp.toPx()
            )
        }
        
        // Glow central sutil
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(glowAlpha * 0.1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .blur(120.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp)
                .scale(contentScale)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Frase de privacidade integrada sutilmente no topo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .alpha(0.7f)
                    .scale(breathingScale)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sua Privacidade Vem Primeiro",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.5.sp
                    ),
                    color = subtleTextColor
                )
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Logo e ícone principal
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Círculo de fundo animado
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(if (unlockSuccess) successScale else breathingScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                
                // Anel exterior
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(if (unlockSuccess) successScale * 0.95f else 1f)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = accentColor.copy(alpha = glowAlpha * 0.3f),
                            shape = CircleShape
                        )
                )
                
                // Container do logo
                Surface(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(if (unlockSuccess) successScale * 0.9f else 1f),
                    shape = CircleShape,
                    color = if (isSystemInDarkTheme) {
                        Color(0xFF1A2332).copy(alpha = 0.5f)
                    } else {
                        Color.White.copy(alpha = 0.8f)
                    },
                    shadowElevation = if (unlockSuccess) 16.dp else 8.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isAuthenticating) {
                            // Animação de autenticação
                            CircularProgressIndicator(
                                modifier = Modifier.size(60.dp),
                                color = accentColor,
                                strokeWidth = 2.dp
                            )
                        } else if (unlockSuccess) {
                            // Ícone de sucesso
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Sucesso",
                                tint = accentColor,
                                modifier = Modifier
                                    .size(80.dp)
                                    .scale(successScale)
                            )
                        } else {
                            // Logo do app
                            Image(
                                painter = painterResource(id = R.drawable.app_icon),
                                contentDescription = "SonsPhere Logo",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
                
                // Indicador de segurança - cadeado minimalista
                if (!isAuthenticating && !unlockSuccess) {
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = 50.dp, y = 50.dp)
                            .scale(breathingScale),
                        shape = CircleShape,
                        color = if (isSystemInDarkTheme) {
                            Color(0xFF1A2332)
                        } else {
                            Color.White
                        },
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "Protegido",
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Informações principais
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (unlockSuccess) "Bem-vindo!" else "SonsPhere",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Light,
                        fontSize = 32.sp,
                        letterSpacing = 2.sp
                    ),
                    color = textColor
                )
                
                Text(
                    text = when {
                        unlockSuccess -> "Acesso autorizado"
                        isAuthenticating -> "Verificando identidade..."
                        canUseBiometric -> "Toque para desbloquear"
                        else -> "Proteção ativa"
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Light
                    ),
                    color = subtleTextColor
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp))
            
            // Botão de ação principal
            AnimatedVisibility(
                visible = !isAuthenticating && !unlockSuccess,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Botão de desbloqueio
                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable(enabled = canUseBiometric) {
                                showBiometricPrompt()
                            }
                            .scale(breathingScale),
                        shape = CircleShape,
                        color = if (canUseBiometric) {
                            accentColor
                        } else {
                            if (isSystemInDarkTheme) Color(0xFF374151) else Color(0xFFE5E7EB)
                        },
                        shadowElevation = 12.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Fingerprint,
                                contentDescription = "Desbloquear",
                                tint = if (canUseBiometric) {
                                    Color.White
                                } else {
                                    if (isSystemInDarkTheme) Color(0xFF6B7280) else Color(0xFF9CA3AF)
                                },
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    
                    // Botão de sair centralizado
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier
                            .size(48.dp)
                            .alpha(0.6f)
                    ) {
                        Icon(
                            Icons.Rounded.PowerSettingsNew,
                            contentDescription = "Sair",
                            tint = subtleTextColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        // Mensagem de erro minimalista
        AnimatedVisibility(
            visible = showError && errorMessage.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSystemInDarkTheme) {
                    Color(0xFF1F2937).copy(alpha = 0.9f)
                } else {
                    Color(0xFF111827).copy(alpha = 0.9f)
                },
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
        
        // Dismiss error after delay
        LaunchedEffect(showError) {
            if (showError) {
                delay(3000)
                showError = false
            }
        }
    }
}

// Função para desenhar padrão de pontos
fun DrawScope.drawDotPattern(
    color: Color,
    spacing: Float
) {
    val dotsX = (size.width / spacing).toInt() + 1
    val dotsY = (size.height / spacing).toInt() + 1
    
    for (x in 0..dotsX) {
        for (y in 0..dotsY) {
            drawCircle(
                color = color,
                radius = 1.5f,
                center = Offset(x * spacing, y * spacing)
            )
        }
    }
} 