package com.richard.musicplayer

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.richard.musicplayer.ui.theme.OuterTuneTheme
import com.richard.musicplayer.ui.theme.SoraFontFamily
import com.richard.musicplayer.ui.theme.InterFontFamily
import com.richard.musicplayer.ui.theme.GradientSystem
import com.richard.musicplayer.utils.ApplyPerformanceOptimizations
import com.richard.musicplayer.utils.rememberOptimizedDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log
import kotlin.math.*
import kotlin.random.Random
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.richard.musicplayer.db.PrivacySecurityRepository
import kotlinx.coroutines.flow.first
import com.richard.musicplayer.R
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import android.media.MediaPlayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import java.util.concurrent.ConcurrentLinkedQueue

// Estrutura para partículas 3D com física avançada
data class Particle3D(
    var x: Float,
    var y: Float,
    var z: Float,
    var vx: Float,
    var vy: Float,
    var vz: Float,
    var size: Float,
    var color: Color,
    var life: Float,
    var type: ParticleType,
    var rotation: Float = 0f,
    var rotationSpeed: Float = Random.nextFloat() * 0.1f - 0.05f,
    var pulsePhase: Float = Random.nextFloat() * PI.toFloat() * 2f,
    var glowIntensity: Float = Random.nextFloat() * 0.5f + 0.5f,
    var trail: MutableList<Offset> = mutableListOf()
)

enum class ParticleType {
    STAR, ENERGY, HOLOGRAM, QUANTUM, PLASMA, NEBULA, CRYSTAL, VOID,
    SOUNDWAVE, MUSICAL_NOTE, FREQUENCY_ORB, BASS_PULSE, MELODY_STREAM,
    HARMONIC_RING, ACOUSTIC_BUBBLE, RHYTHM_CUBE
}

// Sistema de câmera 3D aprimorado
data class Camera3D(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 1000f,
    var rotationX: Float = 0f,
    var rotationY: Float = 0f,
    var fov: Float = 60f,
    var shake: Float = 0f,
    var zoom: Float = 1f
)

// Sistema de luzes dinâmicas
data class DynamicLight(
    var position: Offset,
    var color: Color,
    var intensity: Float,
    var radius: Float,
    var pulse: Boolean = false
)

class SplashActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "SplashActivity"
        
        @JvmStatic
        fun vibrateDevice(vibrator: Vibrator?) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            } catch (e: Exception) {
                // Ignorar erros de vibração
            }
        }
        
        @JvmStatic
        fun vibrateSuccessPattern(vibrator: Vibrator?) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 100, 50, 100, 50, 200),
                            -1
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 100, 50, 100, 50, 200), -1)
                }
            } catch (e: Exception) {
                // Ignorar erros de vibração
            }
        }
    }
    
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val preloadProgress = MutableStateFlow(0f)
    private val preloadingComplete = MutableStateFlow(false)
    private val privacyRepo by lazy { PrivacySecurityRepository(applicationContext) }
    private var vibrator: Vibrator? = null
    private var mediaPlayer: MediaPlayer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
        startPreloadMonitoring()
        
        setContent {
            OuterTuneTheme {
                EpicUltra3DSplashScreen(
                    preloadProgressFlow = preloadProgress,
                    preloadingCompleteFlow = preloadingComplete,
                    onLoadingComplete = {
                        coroutineScope.launch {
                            maybeAuthenticateAndStart()
                        }
                    },
                    vibrator = vibrator
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
    
    private fun startPreloadMonitoring() {
        coroutineScope.launch {
            Log.d(TAG, "🎬 Iniciando experiência cinematográfica ULTIMATE do SonsPhere")
            
            if (App.instance.isPreloadComplete()) {
                preloadProgress.value = 1.0f
                delay(3000) // Tempo extra para apreciar os efeitos ULTIMATE
                preloadingComplete.value = true
                return@launch
            }
            
            // Progressão cinematográfica com momentos épicos
            val cinematicSteps = listOf(
                0.05f to 1500L,  // Abertura dramática
                0.12f to 1200L,  // Primeira onda sonora
                0.23f to 1000L,  // Explosão de partículas musicais
                0.35f to 1100L,  // Construção holográfica
                0.48f to 900L,   // Sincronização quântica
                0.62f to 1200L,  // Transformação dimensional
                0.75f to 800L,   // Aceleração de frequências
                0.88f to 1000L,  // Convergência final
                0.95f to 900L,   // Suspense máximo
                1.0f to 2000L    // Apoteose audiovisual
            )
            
            for ((progress, delay) in cinematicSteps) {
                preloadProgress.value = progress
                Log.d(TAG, "🎥 Cena cinematográfica SonsPhere: ${(progress * 100).toInt()}%")
                
                // Vibração em momentos chave
                if (progress in listOf(0.23f, 0.48f, 0.75f, 1.0f)) {
                    vibratePattern()
                }
                
                if (App.instance.isPreloadComplete()) {
                    preloadProgress.value = 1.0f
                    break
                }
                
                delay(delay)
            }
            
            App.instance.awaitPreloadCompletion()
            preloadProgress.value = 1.0f
            delay(3000) // Momento épico final do SonsPhere
            preloadingComplete.value = true
        }
    }
    
    private fun vibratePattern() {
        try {
            vibrateDevice(vibrator)
        } catch (e: Exception) {
            // Ignorar erros de vibração silenciosamente
        }
    }
    
    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        // Transição suave personalizada
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private suspend fun maybeAuthenticateAndStart() {
        val settings = privacyRepo.settingsFlow.first()
        val biometricEnabled = settings[PrivacySecurityRepository.getPreferencesKey("biometric_lock")] == true

        if (!biometricEnabled) {
            startMainActivity()
            return
        }

        val biometricManager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL

        if (biometricManager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            startMainActivity()
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_prompt_title))
            .setSubtitle(getString(R.string.biometric_prompt_subtitle))
            .setAllowedAuthenticators(authenticators)
            .build()

        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                startMainActivity()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                finish()
            }
        })

        biometricPrompt.authenticate(promptInfo)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EpicUltra3DSplashScreen(
    preloadProgressFlow: StateFlow<Float>,
    preloadingCompleteFlow: StateFlow<Boolean>,
    onLoadingComplete: () -> Unit,
    vibrator: Vibrator?
) {
    // Otimização máxima para 120 FPS
    ApplyPerformanceOptimizations()
    
    val hapticFeedback = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current.density
    
    // Estados principais
    var progressValue by remember { mutableFloatStateOf(0f) }
    var currentPhase by remember { mutableStateOf("INITIALIZATION") }
    var showFinalSequence by remember { mutableStateOf(false) }
    
    // Sistema de partículas 3D melhorado
    val particles = remember { ConcurrentLinkedQueue<Particle3D>() }
    val camera = remember { mutableStateOf(Camera3D()) }
    val lights = remember { mutableStateListOf<DynamicLight>() }
    
    // Mensagens épicas do carregamento do SonsPhere
    val epicMessages = listOf(
        "⚡ ATIVANDO NÚCLEO SONSPHERE",
        "🎵 SINCRONIZANDO ONDAS SONORAS MULTIDIMENSIONAIS",
        "🌌 DECODIFICANDO O UNIVERSO MUSICAL",
        "🎭 MATERIALIZANDO EXPERIÊNCIA ACÚSTICA SUPREMA",
        "💫 CALIBRANDO MOTORES DE ÁUDIO QUÂNTICO",
        "🌟 TRANSCENDENDO BARREIRAS SONORAS",
        "🚀 ENTRANDO NO HIPERSPAÇO MUSICAL",
        "✨ SONSPHERE ULTIMATE ATIVADO!"
    )
    
    // Sistema de animações master
    val infiniteTransition = rememberInfiniteTransition(label = "masterUltimate")
    
    // Rotação épica da câmera 3D
    val cameraRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = CubicBezierEasing(0.4f, 0.2f, 0.6f, 0.8f)),
            repeatMode = RepeatMode.Restart
        ),
        label = "epicCameraRotation"
    )
    
    // Pulsação quântica suprema
    val quantumPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = CubicBezierEasing(0.25f, 0.1f, 0.75f, 0.9f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "supremeQuantumPulse"
    )
    
    // Distorção espacial musical
    val spaceDistortion by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "musicalSpaceDistortion"
    )
    
    // Brilho holográfico supremo
    val hologramGlow by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "supremeHologramGlow"
    )
    
    // Onda sonora mestre
    val soundWave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "masterSoundWave"
    )
    
    // Atualizar câmera 3D com movimento cinematográfico
    LaunchedEffect(cameraRotation, progressValue) {
        val dynamicRadius = 1000f + sin(cameraRotation * PI / 180f).toFloat() * 300f
        val heightVariation = cos(cameraRotation * PI / 90f).toFloat() * 200f
        
        camera.value = camera.value.copy(
            x = cos(cameraRotation * PI / 180f).toFloat() * dynamicRadius * 0.3f,
            y = heightVariation + sin(progressValue * PI).toFloat() * 100f,
            z = dynamicRadius + sin(cameraRotation * PI / 120f).toFloat() * 200f,
            rotationY = cameraRotation,
            rotationX = sin(cameraRotation * PI / 180f).toFloat() * 20f,
            zoom = 1f + progressValue * 0.5f,
            shake = if (showFinalSequence) Random.nextFloat() * 10f else 0f
        )
    }
    
    // Gerar partículas 3D musicais
    LaunchedEffect(progressValue, soundWave) {
        if (particles.size < 800) {
            repeat(30) {
                particles.add(
                    Particle3D(
                        x = Random.nextFloat() * 3000f - 1500f,
                        y = Random.nextFloat() * 3000f - 1500f,
                        z = Random.nextFloat() * 3000f - 1500f,
                        vx = Random.nextFloat() * 4f - 2f,
                        vy = Random.nextFloat() * 4f - 2f,
                        vz = Random.nextFloat() * 4f - 2f,
                        size = Random.nextFloat() * 15f + 3f,
                        color = when (Random.nextInt(8)) {
                            0 -> Color(0xFF6200EE)
                            1 -> Color(0xFFBB86FC)
                            2 -> Color(0xFF03DAC6)
                            3 -> Color(0xFF00BCD4)
                            4 -> Color(0xFFE91E63)
                            5 -> Color(0xFF9C27B0)
                            6 -> Color(0xFFFF6B6B)
                            else -> Color(0xFF4ECDC4)
                        },
                        life = 1.0f,
                        type = when (Random.nextInt(16)) {
                            in 0..7 -> ParticleType.values()[Random.nextInt(8)]
                            else -> ParticleType.values()[8 + Random.nextInt(8)]
                        },
                        glowIntensity = Random.nextFloat() * 0.8f + 0.2f
                    )
                )
            }
        }
        
        // Atualizar física avançada das partículas
        val particlesList = particles.toList()
        particlesList.forEach { particle ->
            // Física baseada no tipo de partícula
            when (particle.type) {
                ParticleType.SOUNDWAVE -> {
                    particle.x += particle.vx * quantumPulse * 1.5f
                    particle.y += sin(soundWave + particle.x * 0.01f) * 2f
                    particle.z += particle.vz * quantumPulse
                }
                ParticleType.MUSICAL_NOTE -> {
                    val noteFreq = particle.pulsePhase * 2f
                    particle.x += cos(noteFreq) * 3f
                    particle.y += particle.vy + sin(soundWave * 2f) * 1.5f
                    particle.z += sin(noteFreq) * 3f
                }
                ParticleType.BASS_PULSE -> {
                    particle.size = particle.size * (1f + sin(soundWave * 4f) * 0.3f)
                    particle.x += particle.vx * quantumPulse * 0.5f
                    particle.y += particle.vy * quantumPulse * 0.5f
                    particle.z += particle.vz * quantumPulse * 0.5f
                }
                else -> {
                    particle.x += particle.vx * quantumPulse
                    particle.y += particle.vy * quantumPulse
                    particle.z += particle.vz * quantumPulse
                }
            }
            
            particle.rotation += particle.rotationSpeed * 2f
            particle.life -= 0.008f
            
            // Efeito de atração gravitacional musical
            val distanceToCenter = sqrt(particle.x * particle.x + particle.y * particle.y + particle.z * particle.z)
            if (distanceToCenter > 150f) {
                val gravity = 0.08f * progressValue * (1f + sin(soundWave) * 0.3f)
                particle.vx -= (particle.x / distanceToCenter) * gravity
                particle.vy -= (particle.y / distanceToCenter) * gravity
                particle.vz -= (particle.z / distanceToCenter) * gravity
            }
            
            // Adicionar trilha para partículas especiais
            if (particle.type in listOf(ParticleType.ENERGY, ParticleType.MUSICAL_NOTE, ParticleType.MELODY_STREAM)) {
                particle.trail.add(Offset(particle.x, particle.y))
                if (particle.trail.size > 10) {
                    particle.trail.removeAt(0)
                }
            }
        }
        
        // Remover partículas mortas e adicionar novas
        particles.removeAll { it.life <= 0f }
    }
    
    // Sistema de luzes dinâmicas
    LaunchedEffect(progressValue) {
        if (lights.size < 5) {
            repeat(5) {
                lights.add(
                    DynamicLight(
                        position = Offset(
                            Random.nextFloat() * screenWidth.value,
                            Random.nextFloat() * screenHeight.value
                        ),
                        color = when (it) {
                            0 -> Color(0xFF6200EE)
                            1 -> Color(0xFFBB86FC)
                            2 -> Color(0xFF03DAC6)
                            3 -> Color(0xFFE91E63)
                            else -> Color(0xFF4ECDC4)
                        },
                        intensity = Random.nextFloat() * 0.5f + 0.5f,
                        radius = Random.nextFloat() * 200f + 100f,
                        pulse = true
                    )
                )
            }
        }
        
        // Animar luzes
        lights.forEachIndexed { index, light ->
            val angle = (index * 72f + cameraRotation) * PI / 180f
            lights[index] = light.copy(
                position = Offset(
                    screenWidth.value / 2 + cos(angle).toFloat() * 200f,
                    screenHeight.value / 2 + sin(angle).toFloat() * 200f
                ),
                intensity = (0.5f + sin(soundWave + index).toFloat() * 0.5f) * hologramGlow
            )
        }
    }
    
    // Monitorar progresso
    LaunchedEffect(preloadProgressFlow) {
        preloadProgressFlow.collect { progress ->
            progressValue = progress
            
            // Feedback háptico em momentos chave
            when ((progress * 100).toInt()) {
                10, 25, 40, 55, 70, 85, 100 -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    try {
                        SplashActivity.vibrateDevice(vibrator)
                    } catch (e: Exception) {
                        // Ignorar erros de vibração silenciosamente
                    }
                }
            }
            
            // Atualizar fase baseada no progresso
            currentPhase = when {
                progress < 0.15f -> "INITIALIZATION"
                progress < 0.30f -> "SOUND_SYNC"
                progress < 0.45f -> "FREQUENCY_BUILD"
                progress < 0.60f -> "DIMENSION_SHIFT"
                progress < 0.75f -> "HARMONIC_FUSION"
                progress < 0.90f -> "QUANTUM_CALIBRATION"
                progress < 1.0f -> "FINAL_PREPARATION"
                else -> "SONSPHERE_READY"
            }
        }
    }
    
    LaunchedEffect(preloadingCompleteFlow) {
        preloadingCompleteFlow.collect { complete ->
            if (complete) {
                showFinalSequence = true
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                try {
                    SplashActivity.vibrateSuccessPattern(vibrator)
                } catch (e: Exception) {
                    // Ignorar erros de vibração silenciosamente
                }
                delay(4000) // Sequência final épica do SonsPhere
                onLoadingComplete()
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Renderizar cena 3D ULTIMATE
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background com gradiente dinâmico supremo
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0A001F).copy(alpha = 0.9f),
                        Color(0xFF1A0033).copy(alpha = 0.95f),
                        Color(0xFF0D0015).copy(alpha = 0.98f),
                        Color.Black
                    ),
                    center = Offset(
                        size.width * (0.5f + sin(spaceDistortion).toFloat() * 0.1f),
                        size.height * (0.5f + cos(spaceDistortion).toFloat() * 0.1f)
                    ),
                    radius = size.minDimension * quantumPulse * 1.2f
                )
            )
            
            // Renderizar sistema de luzes dinâmicas
            lights.forEach { light ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            light.color.copy(alpha = light.intensity * 0.8f),
                            light.color.copy(alpha = light.intensity * 0.4f),
                            light.color.copy(alpha = light.intensity * 0.1f),
                            Color.Transparent
                        ),
                        center = light.position,
                        radius = light.radius * (1f + sin(soundWave).toFloat() * 0.2f)
                    ),
                    radius = light.radius,
                    center = light.position
                )
            }
            
            // Renderizar grade 3D futurista musical
            renderUltra3DGrid(
                size = size,
                camera = camera.value,
                distortion = spaceDistortion,
                glow = hologramGlow,
                soundWave = soundWave
            )
            
            // Renderizar partículas 3D musicais
            renderUltraParticles3D(
                particles = particles.toList(),
                camera = camera.value,
                screenSize = size,
                quantumPulse = quantumPulse,
                soundWave = soundWave
            )
            
            // Efeitos de pós-processamento supremos
            renderUltraPostProcessingEffects(
                size = size,
                progress = progressValue,
                hologramGlow = hologramGlow,
                spaceDistortion = spaceDistortion,
                soundWave = soundWave,
                showFinal = showFinalSequence
            )
        }
        
        // UI Principal ULTIMATE
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo 3D Ultra-Cinematográfico do SonsPhere
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(
                    initialScale = 0f,
                    animationSpec = spring(
                        dampingRatio = 0.3f,
                        stiffness = Spring.StiffnessVeryLow
                    )
                ) + fadeIn(tween(2500))
            ) {
                SonsPhereUltra3DLogo(
                    quantumPulse = quantumPulse,
                    hologramGlow = hologramGlow,
                    spaceDistortion = spaceDistortion,
                    soundWave = soundWave,
                    progress = progressValue,
                    showFinal = showFinalSequence
                )
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Título Holográfico SONSPHERE
            SonsPhereHolographicTitle(
                showFinal = showFinalSequence,
                hologramGlow = hologramGlow,
                soundWave = soundWave
            )
            
            Spacer(modifier = Modifier.height(80.dp))
            
            // Interface de Carregamento Futurista SUPREME
            SonsPhereFuturisticLoadingInterface(
                progress = progressValue,
                currentPhase = currentPhase,
                currentMessage = epicMessages[(progressValue * (epicMessages.size - 1)).toInt()],
                quantumPulse = quantumPulse,
                soundWave = soundWave,
                showFinal = showFinalSequence
            )
        }
        
        // HUD Cinematográfico ULTIMATE
        SonsPhereCinematicHUD(
            progress = progressValue,
            phase = currentPhase,
            soundWave = soundWave,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // Informações do Sistema SONSPHERE
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(2500)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            SonsPhereSystemInfo(
                hologramGlow = hologramGlow,
                soundWave = soundWave
            )
        }
    }
}

// Função para renderizar grade 3D ULTIMATE
fun DrawScope.renderUltra3DGrid(
    size: Size,
    camera: Camera3D,
    distortion: Float,
    glow: Float,
    soundWave: Float
) {
    val gridSize = 50
    val cellSize = 100f
    val centerX = size.width * 0.5f
    val centerY = size.height * 0.5f
    
    for (x in -gridSize..gridSize step 5) {
        for (z in -gridSize..gridSize step 5) {
            val point3D = project3DPoint(
                x = x * cellSize,
                y = -500f + sin(x * 0.1f + z * 0.1f + distortion * PI.toFloat()) * 100f,
                z = z * cellSize,
                camera = camera,
                screenCenter = Offset(centerX, centerY),
                fov = camera.fov
            )
            
            if (point3D.z > 0) { // Apenas renderizar pontos na frente da câmera
                val alpha = (1f - point3D.z / 2000f).coerceIn(0f, 1f) * glow * 0.5f
                val pointSize = (5f / (point3D.z / 500f)).coerceIn(1f, 8f)
                
                drawCircle(
                    color = Color(0xFF00BCD4).copy(alpha = alpha),
                    radius = pointSize,
                    center = Offset(point3D.x, point3D.y)
                )
                
                // Linhas conectoras
                if (x < gridSize && z < gridSize) {
                    val nextPointX = project3DPoint(
                        x = (x + 5) * cellSize,
                        y = -500f + sin((x + 5) * 0.1f + z * 0.1f + distortion * PI.toFloat()) * 100f,
                        z = z * cellSize,
                        camera = camera,
                        screenCenter = Offset(centerX, centerY),
                        fov = camera.fov
                    )
                    
                    val nextPointZ = project3DPoint(
                        x = x * cellSize,
                        y = -500f + sin(x * 0.1f + (z + 5) * 0.1f + distortion * PI.toFloat()) * 100f,
                        z = (z + 5) * cellSize,
                        camera = camera,
                        screenCenter = Offset(centerX, centerY),
                        fov = camera.fov
                    )
                    
                    if (nextPointX.z > 0) {
                        drawLine(
                            color = Color(0xFF00BCD4).copy(alpha = alpha * 0.3f),
                            start = Offset(point3D.x, point3D.y),
                            end = Offset(nextPointX.x, nextPointX.y),
                            strokeWidth = 1f
                        )
                    }
                    
                    if (nextPointZ.z > 0) {
                        drawLine(
                            color = Color(0xFF00BCD4).copy(alpha = alpha * 0.3f),
                            start = Offset(point3D.x, point3D.y),
                            end = Offset(nextPointZ.x, nextPointZ.y),
                            strokeWidth = 1f
                        )
                    }
                }
            }
        }
    }
}

// Função para renderizar partículas 3D ULTIMATE
fun DrawScope.renderUltraParticles3D(
    particles: List<Particle3D>,
    camera: Camera3D,
    screenSize: Size,
    quantumPulse: Float,
    soundWave: Float
) {
    val centerX = screenSize.width * 0.5f
    val centerY = screenSize.height * 0.5f
    
    particles.sortedByDescending { it.z }.forEach { particle ->
        val projected = project3DPoint(
            x = particle.x,
            y = particle.y,
            z = particle.z,
            camera = camera,
            screenCenter = Offset(centerX, centerY),
            fov = camera.fov
        )
        
        if (projected.z > 0 && projected.z < 2000f) {
            val scale = (1000f / projected.z).coerceIn(0.1f, 3f)
            val alpha = particle.life * (1f - projected.z / 2000f).coerceIn(0f, 1f)
            val size = particle.size * scale * (1f + sin(particle.pulsePhase + quantumPulse * PI.toFloat()) * 0.3f)
            
            when (particle.type) {
                ParticleType.STAR -> {
                    // Estrela brilhante
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = alpha),
                                particle.color.copy(alpha = alpha * 0.7f),
                                particle.color.copy(alpha = alpha * 0.3f),
                                Color.Transparent
                            ),
                            radius = size * 2f
                        ),
                        radius = size,
                        center = Offset(projected.x, projected.y)
                    )
                    
                    // Raios de luz
                    repeat(4) { i ->
                        val angle = particle.rotation + i * PI.toFloat() / 2f
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    particle.color.copy(alpha = alpha * 0.8f),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(projected.x, projected.y),
                            end = Offset(
                                projected.x + cos(angle) * size * 3f,
                                projected.y + sin(angle) * size * 3f
                            ),
                            strokeWidth = 1f
                        )
                    }
                }
                
                ParticleType.ENERGY -> {
                    // Orbe de energia
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                particle.color.copy(alpha = alpha),
                                particle.color.copy(alpha = alpha * 0.5f),
                                Color.Transparent
                            )
                        ),
                        radius = size,
                        center = Offset(projected.x, projected.y)
                    )
                    
                    // Anel de energia
                    drawCircle(
                        color = particle.color.copy(alpha = alpha * 0.5f),
                        radius = size * 1.5f,
                        center = Offset(projected.x, projected.y),
                        style = Stroke(width = 2f)
                    )
                }
                
                ParticleType.HOLOGRAM -> {
                    // Fragmento holográfico
                    val path = Path().apply {
                        moveTo(projected.x - size, projected.y)
                        lineTo(projected.x, projected.y - size)
                        lineTo(projected.x + size, projected.y)
                        lineTo(projected.x, projected.y + size)
                        close()
                    }
                    
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00FFFF).copy(alpha = alpha),
                                Color(0xFF00BCD4).copy(alpha = alpha * 0.5f)
                            )
                        )
                    )
                    
                    drawPath(
                        path = path,
                        color = Color(0xFF00FFFF).copy(alpha = alpha * 0.3f),
                        style = Stroke(width = 1f)
                    )
                }
                
                ParticleType.QUANTUM -> {
                    // Partícula quântica
                    repeat(3) { ring ->
                        val ringSize = size * (1f + ring * 0.3f)
                        val ringAlpha = alpha * (1f - ring * 0.3f)
                        
                        drawCircle(
                            color = particle.color.copy(alpha = ringAlpha * 0.3f),
                            radius = ringSize,
                            center = Offset(projected.x, projected.y),
                            style = Stroke(
                                width = 1f,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(5f, 5f),
                                    phase = particle.rotation * 10f
                                )
                            )
                        )
                    }
                    
                    drawCircle(
                        color = particle.color.copy(alpha = alpha),
                        radius = size * 0.5f,
                        center = Offset(projected.x, projected.y)
                    )
                }
                
                ParticleType.PLASMA -> {
                    // Plasma fluido
                    val plasmaPath = Path().apply {
                        val points = 8
                        for (i in 0 until points) {
                            val angle = (i.toFloat() / points) * 2f * PI.toFloat() + particle.rotation
                            val radius = size * (1f + sin(angle * 3f + particle.pulsePhase) * 0.3f)
                            val x = projected.x + cos(angle) * radius
                            val y = projected.y + sin(angle) * radius
                            
                            if (i == 0) {
                                moveTo(x.toFloat(), y.toFloat())
                            } else {
                                lineTo(x.toFloat(), y.toFloat())
                            }
                        }
                        close()
                    }
                    
                    drawPath(
                        path = plasmaPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF00FF).copy(alpha = alpha),
                                Color(0xFF9C27B0).copy(alpha = alpha * 0.5f),
                                Color.Transparent
                            )
                        )
                    )
                }
                
                ParticleType.NEBULA -> {
                    // Nuvem nebulosa
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                particle.color.copy(alpha = alpha * 0.2f),
                                particle.color.copy(alpha = alpha * 0.1f),
                                Color.Transparent
                            ),
                            radius = size * 3f
                        ),
                        radius = size * 3f,
                        center = Offset(projected.x, projected.y)
                    )
                }
                
                ParticleType.CRYSTAL -> {
                    // Cristal geométrico
                    val crystalPath = Path().apply {
                        moveTo(projected.x, projected.y - size * 1.5f)
                        lineTo(projected.x + size, projected.y)
                        lineTo(projected.x, projected.y + size * 1.5f)
                        lineTo(projected.x - size, projected.y)
                        close()
                    }
                    
                    drawPath(
                        path = crystalPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                particle.color.copy(alpha = alpha),
                                particle.color.copy(alpha = alpha * 0.3f)
                            ),
                            start = Offset(projected.x, projected.y - size),
                            end = Offset(projected.x, projected.y + size)
                        )
                    )
                    
                    drawPath(
                        path = crystalPath,
                        color = Color.White.copy(alpha = alpha * 0.5f),
                        style = Stroke(width = 1f)
                    )
                }
                
                ParticleType.VOID -> {
                    // Buraco negro miniatura
                    repeat(3) { layer ->
                        val layerSize = size * (1f + layer * 0.5f)
                        val layerAlpha = alpha * (1f - layer * 0.3f)
                        
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = layerAlpha),
                                    Color(0xFF1A0033).copy(alpha = layerAlpha * 0.5f),
                                    Color.Transparent
                                ),
                                radius = layerSize
                            ),
                            radius = layerSize,
                            center = Offset(projected.x, projected.y)
                        )
                    }
                }
                
                ParticleType.SOUNDWAVE -> {
                    // Onda sonora visual
                    val waveAmplitude = size * (1f + sin(soundWave * 2f) * 0.5f)
                    val wavePath = Path().apply {
                        moveTo(projected.x - size * 2f, projected.y)
                        for (i in 0..20) {
                            val x = projected.x - size * 2f + (i * size * 0.2f)
                            val y = projected.y + sin(i * 0.5f + soundWave) * waveAmplitude * 0.5f
                            lineTo(x, y)
                        }
                    }
                    drawPath(
                        path = wavePath,
                        color = particle.color.copy(alpha = alpha),
                        style = Stroke(width = 2f)
                    )
                }
                
                ParticleType.MUSICAL_NOTE -> {
                    // Nota musical 3D
                    val notePath = Path().apply {
                        // Cabeça da nota
                        addOval(
                            Rect(
                                Offset(projected.x - size * 0.7f, projected.y),
                                Size(size * 1.4f, size)
                            )
                        )
                        // Haste
                        moveTo(projected.x + size * 0.7f, projected.y)
                        lineTo(projected.x + size * 0.7f, projected.y - size * 3f)
                    }
                    drawPath(
                        path = notePath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                particle.color.copy(alpha = alpha),
                                particle.color.copy(alpha = alpha * 0.5f)
                            )
                        )
                    )
                }
                
                ParticleType.FREQUENCY_ORB -> {
                    // Orbe de frequência
                    repeat(4) { freq ->
                        val freqRadius = size * (1f + freq * 0.3f)
                        val freqAlpha = alpha * (1f - freq * 0.2f)
                        drawCircle(
                            color = particle.color.copy(alpha = freqAlpha * 0.3f),
                            radius = freqRadius * (1f + sin(soundWave * (freq + 1)) * 0.1f),
                            center = Offset(projected.x, projected.y),
                            style = Stroke(width = 1f)
                        )
                    }
                }
                
                ParticleType.BASS_PULSE -> {
                    // Pulso de graves
                    val bassSize = size * (1.5f + sin(soundWave * 4f) * 0.5f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF1744).copy(alpha = alpha),
                                Color(0xFFD50000).copy(alpha = alpha * 0.5f),
                                Color.Transparent
                            ),
                            radius = bassSize
                        ),
                        radius = bassSize,
                        center = Offset(projected.x, projected.y)
                    )
                }
                
                ParticleType.MELODY_STREAM -> {
                    // Fluxo melódico
                    if (particle.trail.isNotEmpty()) {
                        val trailPath = Path().apply {
                            moveTo(particle.trail.first().x, particle.trail.first().y)
                            particle.trail.forEach { point ->
                                lineTo(point.x, point.y)
                            }
                        }
                        drawPath(
                            path = trailPath,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    particle.color.copy(alpha = 0f),
                                    particle.color.copy(alpha = alpha * 0.5f),
                                    particle.color.copy(alpha = alpha)
                                )
                            ),
                            style = Stroke(width = 2f)
                        )
                    }
                }
                
                ParticleType.HARMONIC_RING -> {
                    // Anel harmônico
                    val harmonicPath = Path().apply {
                        addOval(
                            Rect(
                                Offset(projected.x - size * 2f, projected.y - size),
                                Size(size * 4f, size * 2f)
                            )
                        )
                    }
                    drawPath(
                        path = harmonicPath,
                        color = particle.color.copy(alpha = alpha * 0.7f),
                        style = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(10f, 5f),
                                phase = particle.rotation * 20f
                            )
                        )
                    )
                }
                
                ParticleType.ACOUSTIC_BUBBLE -> {
                    // Bolha acústica
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = alpha * 0.3f),
                                particle.color.copy(alpha = alpha * 0.2f),
                                Color.Transparent
                            )
                        ),
                        radius = size,
                        center = Offset(projected.x, projected.y)
                    )
                    drawCircle(
                        color = particle.color.copy(alpha = alpha * 0.8f),
                        radius = size,
                        center = Offset(projected.x, projected.y),
                        style = Stroke(width = 1f)
                    )
                }
                
                ParticleType.RHYTHM_CUBE -> {
                    // Cubo rítmico 3D
                    val cubeSize = size * 0.8f
                    val cubePath = Path().apply {
                        // Face frontal
                        moveTo(projected.x - cubeSize, projected.y - cubeSize)
                        lineTo(projected.x + cubeSize, projected.y - cubeSize)
                        lineTo(projected.x + cubeSize, projected.y + cubeSize)
                        lineTo(projected.x - cubeSize, projected.y + cubeSize)
                        close()
                    }
                    
                    drawPath(
                        path = cubePath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                particle.color.copy(alpha = alpha),
                                particle.color.copy(alpha = alpha * 0.5f)
                            )
                        )
                    )
                    
                    // Arestas 3D
                    val depthOffset = cubeSize * 0.5f
                    drawLine(
                        color = particle.color.copy(alpha = alpha * 0.5f),
                        start = Offset(projected.x - cubeSize, projected.y - cubeSize),
                        end = Offset(projected.x - cubeSize + depthOffset, projected.y - cubeSize - depthOffset),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = particle.color.copy(alpha = alpha * 0.5f),
                        start = Offset(projected.x + cubeSize, projected.y - cubeSize),
                        end = Offset(projected.x + cubeSize + depthOffset, projected.y - cubeSize - depthOffset),
                        strokeWidth = 1f
                    )
                }
            }
        }
    }
}

// Efeitos de pós-processamento ULTIMATE
fun DrawScope.renderUltraPostProcessingEffects(
    size: Size,
    progress: Float,
    hologramGlow: Float,
    spaceDistortion: Float,
    soundWave: Float,
    showFinal: Boolean
) {
    // Scanlines holográficas
    val scanlineCount = 100
    val scanlineAlpha = 0.1f * hologramGlow
    
    for (i in 0 until scanlineCount) {
        val y = (i.toFloat() / scanlineCount) * size.height
        val offset = sin(y * 0.01f + spaceDistortion * PI.toFloat() * 2f) * 10f
        
        drawLine(
            color = Color(0xFF00FFFF).copy(alpha = scanlineAlpha),
            start = Offset(0f + offset, y),
            end = Offset(size.width + offset, y),
            strokeWidth = 1f
        )
    }
    
    // Aberração cromática nas bordas
    val vignetteStrength = 0.3f * progress
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = vignetteStrength)
            ),
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = size.minDimension * 0.8f
        )
    )
    
    // Glitch effect
    if (Random.nextFloat() < 0.05f * progress) {
        val glitchY = Random.nextFloat() * size.height
        val glitchHeight = Random.nextFloat() * 50f + 10f
        
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFFF0080).copy(alpha = 0.3f),
                    Color(0xFF00FFFF).copy(alpha = 0.3f),
                    Color(0xFFFF0080).copy(alpha = 0.3f)
                )
            ),
            topLeft = Offset(0f, glitchY),
            size = Size(size.width, glitchHeight)
        )
    }
}

// Projeção 3D para 2D
data class Point3D(val x: Float, val y: Float, val z: Float)

fun project3DPoint(
    x: Float,
    y: Float,
    z: Float,
    camera: Camera3D,
    screenCenter: Offset,
    fov: Float
): Point3D {
    // Aplicar rotação da câmera
    val cosY = cos(camera.rotationY * PI / 180f).toFloat()
    val sinY = sin(camera.rotationY * PI / 180f).toFloat()
    val cosX = cos(camera.rotationX * PI / 180f).toFloat()
    val sinX = sin(camera.rotationX * PI / 180f).toFloat()
    
    // Rotação Y
    var x1 = x * cosY - z * sinY
    val z1 = x * sinY + z * cosY
    
    // Rotação X
    val y1 = y * cosX - z1 * sinX
    val z2 = y * sinX + z1 * cosX
    
    // Translação da câmera
    x1 -= camera.x
    val y2 = y1 - camera.y
    val z3 = z2 - camera.z
    
    // Projeção perspectiva
    if (z3 <= 0) return Point3D(screenCenter.x, screenCenter.y, -1f)
    
    val scale = (screenCenter.x * 2f) / (tan(fov * PI / 360f).toFloat() * 2f)
    val projectedX = (x1 / z3) * scale + screenCenter.x
    val projectedY = (y2 / z3) * scale + screenCenter.y
    
    return Point3D(projectedX, projectedY, z3)
}

@Composable
fun SonsPhereUltra3DLogo(
    quantumPulse: Float,
    hologramGlow: Float,
    spaceDistortion: Float,
    soundWave: Float,
    progress: Float,
    showFinal: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo3D")
    
    val logoRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "logoRotation"
    )
    
    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Campo de energia de fundo
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = logoRotation * 0.3f
                    scaleX = quantumPulse
                    scaleY = quantumPulse
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            
            // Anéis de energia concêntricos
            repeat(5) { ring ->
                val radius = size.minDimension * (0.3f + ring * 0.1f)
                val alpha = (0.3f - ring * 0.05f) * hologramGlow
                
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF6200EE).copy(alpha = alpha),
                            Color(0xFFBB86FC).copy(alpha = alpha * 0.7f),
                            Color(0xFF00BCD4).copy(alpha = alpha * 0.5f),
                            Color(0xFF6200EE).copy(alpha = alpha)
                        ),
                        center = center
                    ),
                    radius = radius,
                    center = center,
                    style = Stroke(
                        width = 3f,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(20f, 10f),
                            phase = logoRotation * (ring + 1)
                        )
                    )
                )
            }
            
            // Hexágono holográfico
            val hexPath = Path().apply {
                val hexRadius = size.minDimension * 0.4f
                for (i in 0..6) {
                    val angle = (i * 60f - 30f) * PI / 180f
                    val x = center.x + hexRadius * cos(angle).toFloat()
                    val y = center.y + hexRadius * sin(angle).toFloat()
                    
                    if (i == 0) {
                        moveTo(x, y)
                    } else {
                        lineTo(x, y)
                    }
                }
            }
            
            drawPath(
                path = hexPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00FFFF).copy(alpha = 0.5f * hologramGlow),
                        Color(0xFF00BCD4).copy(alpha = 0.3f * hologramGlow)
                    )
                ),
                style = Stroke(width = 2f)
            )
        }
        
        // Logo principal 3D
        Image(
            painter = painterResource(id = R.drawable.music_note),
            contentDescription = null,
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer {
                    rotationY = logoRotation
                    rotationX = sin(logoRotation * PI / 180f).toFloat() * 20f
                    rotationZ = cos(logoRotation * PI / 180f).toFloat() * 10f
                    scaleX = if (showFinal) 1.5f else 1f
                    scaleY = if (showFinal) 1.5f else 1f
                    shadowElevation = 40.dp.toPx()
                    // Removendo as cores de sombra pois causam erro de tipo
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    
                    // Efeito de profundidade
                    cameraDistance = 12.dp.toPx()
                }
        )
        
        // Partículas orbitais 3D
        repeat(8) { i ->
            val angle = (i * 45f + logoRotation * 2f) * PI / 180f
            val orbitRadius = 120f
            val particleX = orbitRadius * cos(angle).toFloat()
            val particleY = orbitRadius * sin(angle).toFloat()
            val particleZ = sin(logoRotation * PI / 90f + i * PI / 4f).toFloat() * 50f
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .offset(
                        x = particleX.dp,
                        y = particleY.dp
                    )
                    .graphicsLayer {
                        // Simular profundidade com escala e transparência
                        scaleX = 1f + particleZ / 100f
                        scaleY = 1f + particleZ / 100f
                        alpha = (1f - abs(particleZ) / 100f).coerceIn(0.3f, 1f)
                        // Adicionar blur para simular profundidade
                        shadowElevation = abs(particleZ / 10f)
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                when (i % 3) {
                                    0 -> Color(0xFFBB86FC)
                                    1 -> Color(0xFF00BCD4)
                                    else -> Color(0xFF6200EE)
                                },
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        // Holograma frontal
        if (hologramGlow > 0.7f) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = (hologramGlow - 0.7f) * 3f
                    }
            ) {
                // Linhas de scan holográficas
                repeat(20) { i ->
                    val y = size.height * (i / 20f)
                    drawLine(
                        color = Color(0xFF00FFFF).copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }
            }
        }
    }
}

@Composable
fun SonsPhereHolographicTitle(
    showFinal: Boolean,
    hologramGlow: Float,
    soundWave: Float
) {
    val titleText = if (showFinal) "🌟 BEM-VINDO AO SONSPHERE 🌟" else "SONSPHERE"
    val subtitleText = if (showFinal) "A música transcendeu todas as dimensões" else "Ultimate Quantum Audio Experience"
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título principal
        AnimatedContent(
            targetState = titleText,
            transitionSpec = {
                (fadeIn(tween(1000)) + scaleIn(initialScale = 0.5f) + 
                slideInVertically { -it }) togetherWith
                (fadeOut(tween(500)) + scaleOut(targetScale = 2f) + 
                slideOutVertically { it })
            },
            label = "titleAnimation"
        ) { text ->
            Box {
                // Camada de brilho traseiro
                Text(
                    text = text,
                    fontFamily = SoraFontFamily,
                    fontSize = if (showFinal) 32.sp else 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00FFFF),
                    modifier = Modifier
                        .blur(8.dp)
                        .alpha(0.5f * hologramGlow)
                )
                
                // Texto principal
                Text(
                    text = text,
                    fontFamily = SoraFontFamily,
                    fontSize = if (showFinal) 32.sp else 42.sp,
                    fontWeight = FontWeight.Black,
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFBB86FC),
                                Color(0xFF00BCD4),
                                Color.White
                            )
                        ),
                        shadow = Shadow(
                            color = Color(0xFF6200EE).copy(alpha = 0.8f),
                            offset = Offset(0f, 4f),
                            blurRadius = 8f
                        )
                    ),
                    modifier = Modifier.graphicsLayer {
                        shadowElevation = 12.dp.toPx()
                    }
                )
            }
        }
        
        // Subtítulo
        AnimatedContent(
            targetState = subtitleText,
            transitionSpec = {
                fadeIn(tween(1200, delayMillis = 200)) togetherWith
                fadeOut(tween(600))
            },
            label = "subtitleAnimation"
        ) { text ->
            Text(
                text = text,
                fontFamily = InterFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                style = TextStyle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.9f),
                    shadow = Shadow(
                        color = Color(0xFF00BCD4).copy(alpha = 0.6f),
                        offset = Offset(0f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}

@Composable
fun SonsPhereFuturisticLoadingInterface(
    progress: Float,
    currentPhase: String,
    currentMessage: String,
    quantumPulse: Float,
    soundWave: Float,
    showFinal: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (showFinal) {
            // Sequência final épica
            FinalSequenceUI(quantumPulse)
        } else {
            // Mensagem de fase
            AnimatedContent(
                targetState = currentMessage,
                transitionSpec = {
                    (fadeIn(tween(800)) + slideInHorizontally { -it / 2 } + 
                    scaleIn(initialScale = 0.8f)) togetherWith
                    (fadeOut(tween(400)) + slideOutHorizontally { it / 2 } + 
                    scaleOut(targetScale = 1.2f))
                },
                label = "messageAnimation"
            ) { message ->
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF6200EE).copy(alpha = 0.1f),
                                    Color(0xFF00BCD4).copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6200EE).copy(alpha = 0.3f),
                                    Color(0xFFBB86FC).copy(alpha = 0.5f),
                                    Color(0xFF00BCD4).copy(alpha = 0.3f)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = message,
                        fontFamily = InterFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            shadowElevation = 6.dp.toPx()
                        }
                    )
                }
            }
            
            // Barra de progresso futurista
            QuantumProgressBar(
                progress = progress,
                quantumPulse = quantumPulse
            )
            
            // Indicadores de fase
            PhaseIndicators(
                currentPhase = currentPhase,
                progress = progress
            )
        }
    }
}

@Composable
fun QuantumProgressBar(
    progress: Float,
    quantumPulse: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Background com padrão hexagonal
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fundo escuro
            drawRect(
                color = Color(0xFF0A0A0A)
            )
            
            // Padrão hexagonal
            val hexSize = 20f
            val rows = (size.height / hexSize).toInt() + 2
            val cols = (size.width / hexSize).toInt() + 2
            
            for (row in 0..rows) {
                for (col in 0..cols) {
                    val x = col * hexSize * 1.5f
                    val y = row * hexSize * sqrt(3f) + (if (col % 2 == 0) 0f else hexSize * sqrt(3f) / 2f)
                    
                    val hexPath = Path().apply {
                        moveTo(x + hexSize, y)
                        for (i in 1..6) {
                            val angle = i * PI / 3f
                            lineTo(
                                x + hexSize * cos(angle).toFloat(),
                                y + hexSize * sin(angle).toFloat()
                            )
                        }
                    }
                    
                    drawPath(
                        path = hexPath,
                        color = Color(0xFF1A1A1A),
                        style = Stroke(width = 1f)
                    )
                }
            }
        }
        
        // Barra de progresso com múltiplas camadas
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
        ) {
            // Camada base
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6200EE),
                                Color(0xFF9C27B0),
                                Color(0xFFBB86FC),
                                Color(0xFF00BCD4),
                                Color(0xFF00E5FF)
                            )
                        )
                    )
            )
            
            // Efeito de energia fluindo
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                // Ondas de energia
                repeat(3) { wave ->
                    val waveOffset = (wave * 100f) + (quantumPulse * 200f)
                    val waveAlpha = 0.3f - wave * 0.1f
                    
                    drawPath(
                        path = Path().apply {
                            moveTo(0f, size.height / 2f)
                            for (x in 0..size.width.toInt() step 10) {
                                val y = size.height / 2f + sin((x + waveOffset) * 0.02f) * 10f
                                lineTo(x.toFloat(), y)
                            }
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        },
                        color = Color.White.copy(alpha = waveAlpha)
                    )
                }
            }
            
            // Partículas brilhantes
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            startX = -200f + quantumPulse * 400f,
                            endX = 200f + quantumPulse * 400f
                        )
                    )
            )
        }
        
        // Bordas brilhantes
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF6200EE).copy(alpha = 0.8f),
                            Color(0xFFBB86FC),
                            Color(0xFF00BCD4).copy(alpha = 0.8f)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
        )
        
        // Texto de porcentagem
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${(progress * 100).toInt()}%",
                fontFamily = SoraFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun PhaseIndicators(
    currentPhase: String,
    progress: Float
) {
    val phases = listOf(
        "INITIALIZATION" to 0.0f,
        "QUANTUM_SYNC" to 0.2f,
        "HOLOGRAM_BUILD" to 0.4f,
        "DIMENSION_SHIFT" to 0.6f,
        "FINAL_CALIBRATION" to 0.8f,
        "READY" to 1.0f
    )
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        phases.forEach { (phase, threshold) ->
            val isActive = progress >= threshold
            val isCurrent = currentPhase == phase
            
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 16.dp else 12.dp)
                    .background(
                        brush = if (isActive) {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFF00BCD4)
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF2A2A2A),
                                    Color(0xFF1A1A1A)
                                )
                            )
                        },
                        shape = if (isCurrent) RoundedCornerShape(4.dp) else CircleShape
                    )
                    .border(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isActive) Color(0xFF00E5FF) else Color(0xFF3A3A3A),
                        shape = if (isCurrent) RoundedCornerShape(4.dp) else CircleShape
                    )
                    .graphicsLayer {
                        shadowElevation = if (isCurrent) 8.dp.toPx() else 0f
                        alpha = if (isActive) 1f else 0.5f
                    }
            ) {
                if (isCurrent) {
                    // Animação pulsante para fase atual
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.8f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun FinalSequenceUI(quantumPulse: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Ícone de sucesso épico
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ) {
            // Explosão de energia
            repeat(3) { ring ->
                Box(
                    modifier = Modifier
                        .size((100 + ring * 30).dp)
                        .graphicsLayer {
                            scaleX = quantumPulse * (1f + ring * 0.2f)
                            scaleY = quantumPulse * (1f + ring * 0.2f)
                            alpha = (1f - ring * 0.3f) * 0.5f
                        }
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00E676).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
            
            // Checkmark holográfico
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        shadowElevation = 20.dp.toPx()
                        rotationZ = quantumPulse * 10f
                    },
                tint = Color(0xFF00E676)
            )
        }
        
        // Mensagem de sucesso
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "SISTEMA INICIADO COM SUCESSO",
                fontFamily = SoraFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00E676),
                            Color(0xFF00BCD4),
                            Color(0xFF00E676)
                        )
                    )
                )
            )
            
            Text(
                text = "Preparando experiência imersiva...",
                fontFamily = InterFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun SonsPhereCinematicHUD(
    progress: Float,
    phase: String,
    soundWave: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            // HUD futurista
            val cornerSize = 20f
            val strokeWidth = 2f
            
            // Cantos do HUD
            // Canto superior esquerdo
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                start = Offset(0f, cornerSize),
                end = Offset(0f, 0f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                start = Offset(0f, 0f),
                end = Offset(cornerSize, 0f),
                strokeWidth = strokeWidth
            )
            
            // Canto superior direito
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                start = Offset(size.width - cornerSize, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                start = Offset(size.width, 0f),
                end = Offset(size.width, cornerSize),
                strokeWidth = strokeWidth
            )
            
            // Linha de progresso superior
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.3f),
                start = Offset(cornerSize + 10f, 0f),
                end = Offset(size.width - cornerSize - 10f, 0f),
                strokeWidth = 1f
            )
            
            val progressLineEnd = cornerSize + 10f + (size.width - (cornerSize + 10f) * 2) * progress
            drawLine(
                color = Color(0xFF00E5FF),
                start = Offset(cornerSize + 10f, 0f),
                end = Offset(progressLineEnd, 0f),
                strokeWidth = 2f
            )
        }
        
        // Informações do HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 30.dp, end = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "PHASE: $phase",
                fontFamily = InterFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = Color(0xFF00E5FF).copy(alpha = 0.7f)
            )
            
            Text(
                text = "QUANTUM CORE: ACTIVE",
                fontFamily = InterFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = Color(0xFF00E676).copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SonsPhereSystemInfo(
    hologramGlow: Float,
    soundWave: Float
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF00E5FF).copy(alpha = 0.3f * hologramGlow),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "v${stringResource(id = R.string.app_version)}",
            fontFamily = InterFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF00E5FF).copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )
        
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(
                    color = Color(0xFF00E5FF).copy(alpha = 0.6f),
                    shape = CircleShape
                )
        )
        
        Text(
            text = "QUANTUM ENGINE",
            fontFamily = InterFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF00E5FF).copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )
    }
} 