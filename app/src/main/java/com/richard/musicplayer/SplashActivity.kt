package com.richard.musicplayer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.richard.musicplayer.ui.theme.OuterTuneTheme
import com.richard.musicplayer.db.PrivacySecurityRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    
    @Inject
    lateinit var privacySecurityRepository: PrivacySecurityRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configurar tela cheia sem barras do sistema
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )
        
        setContent {
            OuterTuneTheme {
                ModernSplashScreen {
                    // Verificar configuração de bloqueio biométrico
                    checkBiometricSettingAndNavigate()
                }
            }
        }
    }
    
    private suspend fun checkBiometricSettingAndNavigate() {
        try {
            // Verificar se o bloqueio biométrico está habilitado
            val settings = privacySecurityRepository.settingsFlow.first()
            val biometricLockEnabled = settings[PrivacySecurityRepository.getPreferencesKey("biometric_lock")] as? Boolean ?: false
            
            val targetActivity = if (biometricLockEnabled) {
                // Se bloqueio biométrico está habilitado, ir para AuthScreen via MainActivity
                MainActivity::class.java
            } else {
                // Se não está habilitado, ir direto para MainActivity
                MainActivity::class.java
            }
            
            val intent = Intent(this@SplashActivity, targetActivity).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                // Adicionar extra para indicar se deve mostrar auth screen
                putExtra("REQUIRE_AUTH", biometricLockEnabled)
            }
            
            startActivity(intent)
            
            // Transição suave
            overridePendingTransition(
                android.R.anim.fade_in, 
                android.R.anim.fade_out
            )
            
            finish()
        } catch (e: Exception) {
            // Em caso de erro, ir para MainActivity sem auth
            val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("REQUIRE_AUTH", false)
            }
            startActivity(intent)
            finish()
        }
    }
}

@Composable
fun ModernSplashScreen(onAnimationEnd: suspend () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val animationDuration = 2200L // Mais rápido e dinâmico
    
    // Detectar tema do sistema
    val isSystemInDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    
    // Cores adaptativas baseadas no tema
    val backgroundColor = if (isSystemInDarkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F0F14), // Preto azulado ultra escuro
                Color(0xFF1A1A24), // Cinza azulado escuro
                Color(0xFF252538), // Azul escuro médio
                Color(0xFF1A1A24), // Cinza azulado escuro
                Color(0xFF0F0F14)  // Preto azulado ultra escuro
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF8F9FC), // Branco azulado
                Color(0xFFE8ECF4), // Cinza claro azulado
                Color(0xFFD6DCE8), // Cinza médio azulado
                Color(0xFFE8ECF4), // Cinza claro azulado
                Color(0xFFF8F9FC)  // Branco azulado
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    }
    
    val accentColor = if (isSystemInDarkTheme) {
        Color(0xFF60A5FA) // Azul claro vibrante
    } else {
        Color(0xFF3B82F6) // Azul médio
    }
    
    val textColor = if (isSystemInDarkTheme) {
        Color.White
    } else {
        Color(0xFF1F2937) // Cinza escuro
    }
    
    // Animações principais
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )
    
    val logoRotation by animateFloatAsState(
        targetValue = if (startAnimation) 360f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "logo_rotation"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(800, delayMillis = 400),
        label = "content_alpha"
    )
    
    // Animação infinita para elementos decorativos
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(animationDuration)
        onAnimationEnd()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Elementos decorativos de fundo - Ondas sonoras abstratas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isSystemInDarkTheme) 0.1f else 0.05f)
        ) {
            drawWavePattern(
                wave1 = wave1,
                wave2 = wave2,
                color = accentColor,
                isDark = isSystemInDarkTheme
            )
        }
        
        // Partículas flutuantes
        if (startAnimation) {
            repeat(12) { index ->
                FloatingParticle(
                    delay = index * 150L,
                    accentColor = accentColor.copy(alpha = if (isSystemInDarkTheme) 0.3f else 0.2f)
                )
            }
        }
        
        // Conteúdo principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo com efeitos modernos
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(logoScale)
            ) {
                // Círculos concêntricos animados
                repeat(3) { index ->
                    val delay = index * 200L
                    val circleScale by animateFloatAsState(
                        targetValue = if (startAnimation) 1f + (index * 0.3f) else 0f,
                        animationSpec = tween(1000 + (index * 200), delayMillis = delay.toInt()),
                        label = "circle_$index"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size((120 + (index * 40)).dp)
                            .scale(circleScale)
                            .alpha(0.1f - (index * 0.03f))
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        accentColor,
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
                
                // Sombra suave do logo
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .offset(y = 4.dp)
                        .scale(pulseScale)
                        .blur(20.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.3f))
                )
                
                // Logo principal
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            rotationZ = logoRotation
                            scaleX = pulseScale
                            scaleY = pulseScale
                        },
                    shape = CircleShape,
                    color = if (isSystemInDarkTheme) Color(0xFF1E1E2E) else Color.White,
                    shadowElevation = 8.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "SonsPhere Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .clip(CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Textos com animação elegante
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(contentAlpha)
            ) {
                // Nome do app
                Text(
                    text = "SonsPhere",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Light,
                        fontSize = 42.sp,
                        letterSpacing = 3.sp
                    ),
                    color = textColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Tagline
                Text(
                    text = "Música Que Move Você",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp
                    ),
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
        
        // Indicador de progresso minimalista
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .alpha(contentAlpha)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_$index"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(dotAlpha)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            }
        }
        
        // Versão discreta
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.3f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .alpha(contentAlpha)
        )
    }
}

// Componente de partícula flutuante
@Composable
fun FloatingParticle(
    delay: Long,
    accentColor: Color
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(delay)
        isVisible = true
    }
    
    if (isVisible) {
        val infiniteTransition = rememberInfiniteTransition(label = "particle")
        
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "offsetY"
        )
        
        val offsetX by infiniteTransition.animateFloat(
            initialValue = -0.1f,
            targetValue = 0.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offsetX"
        )
        
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 8000
                    0f at 0
                    1f at 2000
                    1f at 6000
                    0f at 8000
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "alpha"
        )
        
        val randomX = remember { Random.nextFloat() }
        val randomSize = remember { Random.nextInt(4, 8) }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .offset(
                        x = ((randomX + offsetX) * 300).dp,
                        y = (offsetY * 800).dp
                    )
                    .size(randomSize.dp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }
    }
}

// Função para desenhar padrão de ondas
fun DrawScope.drawWavePattern(
    wave1: Float,
    wave2: Float,
    color: Color,
    isDark: Boolean
) {
    val width = size.width
    val height = size.height
    val centerX = width / 2
    val centerY = height / 2
    
    // Primeira onda
    for (i in 0..360 step 10) {
        val angle = Math.toRadians((i + wave1).toDouble())
        val radius = 200f + (50f * sin(angle * 3))
        val x = centerX + (radius * cos(angle)).toFloat()
        val y = centerY + (radius * sin(angle)).toFloat()
        
        drawCircle(
            color = color.copy(alpha = 0.05f),
            radius = 3f,
            center = Offset(x, y)
        )
    }
    
    // Segunda onda
    for (i in 0..360 step 15) {
        val angle = Math.toRadians((i - wave2).toDouble())
        val radius = 300f + (80f * cos(angle * 2))
        val x = centerX + (radius * cos(angle)).toFloat()
        val y = centerY + (radius * sin(angle)).toFloat()
        
        drawCircle(
            color = color.copy(alpha = 0.03f),
            radius = 5f,
            center = Offset(x, y)
        )
    }
}
