/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.richard.musicplayer.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.navigation.NavController
import com.richard.musicplayer.BuildConfig
import com.richard.musicplayer.LocalPlayerAwareWindowInsets
import com.richard.musicplayer.R
import com.richard.musicplayer.ui.component.IconButton
import com.richard.musicplayer.ui.utils.backToMain
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    var particlesVisible by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Parallax e efeitos 3D
    val parallaxOffset by remember {
        derivedStateOf {
            scrollState.firstVisibleItemScrollOffset * 0.3f
        }
    }
    
    val scrollOffset by remember {
        derivedStateOf {
            scrollState.firstVisibleItemScrollOffset.toFloat()
        }
    }

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
        delay(500)
        particlesVisible = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Background cósmico animado
        CosmicBackground()
        
        // Partículas flutuantes
        if (particlesVisible) {
            FloatingParticles()
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
        ) {
            // TopAppBar com efeito glassmorphism
            CinematicTopAppBar(
                navController = navController,
                scrollBehavior = scrollBehavior,
                scrollOffset = scrollOffset
            )

            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Hero Section 3D
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(
                                dampingRatio = 0.6f,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeIn(animationSpec = tween(1200)) + scaleIn(
                            initialScale = 0.8f,
                            animationSpec = spring(
                                dampingRatio = 0.7f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    ) {
                        Box(
                            modifier = Modifier.graphicsLayer {
                                translationY = -parallaxOffset * 0.5f
                                rotationX = (scrollOffset / 20f).coerceIn(-15f, 15f)
                            }
                        ) {
                            CinematicHeroSection()
                        }
                    }
                }
                
                // Cards com entrada cinematográfica
                val cardAnimationDelay = 200L
                
                // Card 1 - Description
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(
                            animationSpec = tween(800, delayMillis = 0)
                        ) + scaleIn(
                            initialScale = 0.9f,
                            animationSpec = spring(
                                dampingRatio = 0.7f,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    ) {
                        Box(
                            modifier = Modifier.graphicsLayer {
                                val cardOffset = (scrollOffset - 0 * 200f) / 10f
                                translationY = cardOffset.coerceIn(-50f, 50f)
                                rotationY = (cardOffset / 5f).coerceIn(-8f, 8f)
                                transformOrigin = TransformOrigin.Center
                            }
                        ) {
                            CinematicDescriptionCard()
                        }
                    }
                }
                
                // Card 2 - Features
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(
                            animationSpec = tween(800, delayMillis = 200)
                        ) + scaleIn(
                            initialScale = 0.9f,
                            animationSpec = spring(
                                dampingRatio = 0.7f,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    ) {
                        Box(
                            modifier = Modifier.graphicsLayer {
                                val cardOffset = (scrollOffset - 1 * 200f) / 10f
                                translationY = cardOffset.coerceIn(-50f, 50f)
                                rotationY = (cardOffset / 5f).coerceIn(-8f, 8f)
                                transformOrigin = TransformOrigin.Center
                            }
                        ) {
                            CinematicFeaturesCard()
                        }
                    }
                }
                
                // Card 3 - Tech
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(
                            animationSpec = tween(800, delayMillis = 400)
                        ) + scaleIn(
                            initialScale = 0.9f,
                            animationSpec = spring(
                                dampingRatio = 0.7f,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    ) {
                        Box(
                            modifier = Modifier.graphicsLayer {
                                val cardOffset = (scrollOffset - 2 * 200f) / 10f
                                translationY = cardOffset.coerceIn(-50f, 50f)
                                rotationY = (cardOffset / 5f).coerceIn(-8f, 8f)
                                transformOrigin = TransformOrigin.Center
                            }
                        ) {
                            CinematicTechCard()
                        }
                    }
                }
                
                // Card 4 - Contact
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(
                            animationSpec = tween(800, delayMillis = 600)
                        ) + scaleIn(
                            initialScale = 0.9f,
                            animationSpec = spring(
                                dampingRatio = 0.7f,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    ) {
                        Box(
                            modifier = Modifier.graphicsLayer {
                                val cardOffset = (scrollOffset - 3 * 200f) / 10f
                                translationY = cardOffset.coerceIn(-50f, 50f)
                                rotationY = (cardOffset / 5f).coerceIn(-8f, 8f)
                                transformOrigin = TransformOrigin.Center
                            }
                        ) {
                            CinematicContactCard()
                        }
                    }
                }
                
                // Card 5 - Credits
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(
                            animationSpec = tween(800, delayMillis = 800)
                        ) + scaleIn(
                            initialScale = 0.9f,
                            animationSpec = spring(
                                dampingRatio = 0.7f,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    ) {
                        Box(
                            modifier = Modifier.graphicsLayer {
                                val cardOffset = (scrollOffset - 4 * 200f) / 10f
                                translationY = cardOffset.coerceIn(-50f, 50f)
                                rotationY = (cardOffset / 5f).coerceIn(-8f, 8f)
                                transformOrigin = TransformOrigin.Center
                            }
                        ) {
                            CinematicCreditsCard()
                        }
                    }
                }
                
                // Spacer final
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun CosmicBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic")
    
    val nebula1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "nebula1"
    )
    
    val nebula2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "nebula2"
    )
    
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // Nebulosa 1
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF4A90E2).copy(alpha = 0.15f),
                    Color(0xFF9013FE).copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(
                    size.width * 0.3f + cos(nebula1 * PI / 180f).toFloat() * 100f,
                    size.height * 0.2f + sin(nebula1 * PI / 180f).toFloat() * 50f
                ),
                radius = size.width * 0.8f
            ),
            radius = size.width * 0.6f,
            center = Offset(
                size.width * 0.3f + cos(nebula1 * PI / 180f).toFloat() * 100f,
                size.height * 0.2f + sin(nebula1 * PI / 180f).toFloat() * 50f
            )
        )
        
        // Nebulosa 2
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE91E63).copy(alpha = 0.12f),
                    Color(0xFF673AB7).copy(alpha = 0.06f),
                    Color.Transparent
                ),
                center = Offset(
                    size.width * 0.7f + cos(nebula2 * PI / 180f).toFloat() * 80f,
                    size.height * 0.8f + sin(nebula2 * PI / 180f).toFloat() * 60f
                ),
                radius = size.width * 0.7f
            ),
            radius = size.width * 0.5f,
            center = Offset(
                size.width * 0.7f + cos(nebula2 * PI / 180f).toFloat() * 80f,
                size.height * 0.8f + sin(nebula2 * PI / 180f).toFloat() * 60f
            )
        )
        
        // Galáxia em espiral
        rotate(nebula1 * 0.1f, pivot = center) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF00BCD4).copy(alpha = 0.1f),
                        Color.Transparent,
                        Color(0xFF00BCD4).copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = center
                ),
                radius = size.width * 0.9f,
                center = center
            )
        }
    }
}

@Composable
private fun FloatingParticles() {
    val particles = remember {
        List(30) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 1f,
                speed = Random.nextFloat() * 0.5f + 0.1f,
                alpha = Random.nextFloat() * 0.7f + 0.3f
            )
        }
    }
    
    particles.forEach { particle ->
        val infiniteTransition = rememberInfiniteTransition(label = "particle_${particle.hashCode()}")
        
        val animatedY by infiniteTransition.animateFloat(
            initialValue = particle.y,
            targetValue = particle.y - 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween((10000 / particle.speed).toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "y"
        )
        
        val animatedAlpha by infiniteTransition.animateFloat(
            initialValue = particle.alpha,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
        
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val particleY = (animatedY % 1.5f) * size.height
            drawCircle(
                color = Color.White.copy(alpha = animatedAlpha * 0.6f),
                radius = particle.size,
                center = Offset(particle.x * size.width, particleY)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CinematicTopAppBar(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    scrollOffset: Float
) {
    val alpha = (1f - (scrollOffset / 300f)).coerceIn(0f, 1f)
    
    TopAppBar(
        title = { 
            Text(
                "Sobre",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.graphicsLayer {
                    this.alpha = alpha
                    scaleX = 0.9f + (alpha * 0.1f)
                    scaleY = 0.9f + (alpha * 0.1f)
                }
            ) 
        },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = scrollOffset * 0.1f
                    }
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        )
    )
}

@Composable
private fun CinematicHeroSection() {
    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    
    val logoRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E).copy(alpha = 0.9f),
                            Color(0xFF16213E).copy(alpha = 0.7f),
                            Color(0xFF0F3460).copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            // Efeito de névoa cósmica
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4A90E2).copy(alpha = glowIntensity * 0.3f),
                            Color(0xFF9013FE).copy(alpha = glowIntensity * 0.15f),
                            Color.Transparent
                        ),
                        radius = size.width * 0.8f
                    ),
                    radius = size.width * 0.6f,
                    center = center
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo 3D holográfico
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer {
                            rotationZ = logoRotation * 0.1f
                            rotationY = logoRotation * 0.05f
                            scaleX = pulseScale
                            scaleY = pulseScale
                            shadowElevation = 20.dp.toPx()
                        }
                ) {
                    // Múltiplas camadas de glow
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(1.2f + index * 0.1f)
                                .blur((24 + index * 8).dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF4A90E2).copy(alpha = glowIntensity * (0.4f - index * 0.1f)),
                                            Color(0xFF9013FE).copy(alpha = glowIntensity * (0.2f - index * 0.05f)),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                    
                    // Logo principal com efeito holográfico
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        shadowElevation = 16.dp,
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.sweepGradient(
                                        colors = listOf(
                                            Color(0xFF4A90E2),
                                            Color(0xFF9013FE),
                                            Color(0xFFE91E63),
                                            Color(0xFF00BCD4),
                                            Color(0xFF4A90E2)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Inner glow
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        ),
                                        CircleShape
                                    )
                            )
                            
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.music_note),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(90.dp)
                                    .graphicsLayer {
                                        rotationZ = -logoRotation * 0.2f
                                    },
                                tint = Color.White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Título holográfico
                HolographicText(
                    text = "SonsPhere",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HolographicText(
    text: String,
    style: androidx.compose.ui.text.TextStyle
) {
    val infiniteTransition = rememberInfiniteTransition(label = "holographic")
    
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    Box {
        // Glow effect
        Text(
            text = text,
            style = style,
            modifier = Modifier
                .blur(8.dp)
                .graphicsLayer {
                    alpha = 0.7f
                },
            color = Color(0xFF4A90E2)
        )
        
        // Main text
        Text(
            text = text,
            style = style,
            modifier = Modifier.graphicsLayer {
                shadowElevation = 8.dp.toPx()
            },
            color = Color.White
        )
        
        // Shimmer overlay
        Text(
            text = text,
            style = style,
            modifier = Modifier.graphicsLayer {
                alpha = 0.3f + shimmer * 0.4f
            },
            color = Color(0xFF64B5F6)
        )
    }
}

@Composable
private fun TypewriterText(
    text: String,
    style: androidx.compose.ui.text.TextStyle
) {
    var visibleChars by remember { mutableStateOf(0) }
    
    LaunchedEffect(text) {
        visibleChars = 0
        text.indices.forEach { index ->
            delay(50)
            visibleChars = index + 1
        }
    }
    
    Text(
        text = text.take(visibleChars),
        style = style,
        textAlign = TextAlign.Center,
        modifier = Modifier.graphicsLayer {
            shadowElevation = 4.dp.toPx()
        }
    )
}



@Composable
private fun CinematicDescriptionCard() {
    CinematicGlassCard(
        icon = Icons.Rounded.AutoAwesome,
        title = "Nossa Visão",
        gradientColors = listOf(
            Color(0xFF4A90E2),
            Color(0xFF9013FE)
        )
    ) {
        Text(
            text = "Transformamos cada momento musical em uma experiência única. Com tecnologia de ponta, design revolucionário e compromisso absoluto com sua privacidade, o SonsPhere redefine como você se conecta com a música.",
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 26.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        )
    }
}

@Composable
private fun CinematicFeaturesCard() {
    CinematicGlassCard(
        icon = Icons.Rounded.Rocket,
        title = "Recursos Avançados",
        gradientColors = listOf(
            Color(0xFFE91E63),
            Color(0xFF9C27B0)
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureItem("🎵", "Streaming de alta qualidade")
            FeatureItem("🎨", "Interface cinematográfica")
            FeatureItem("🔒", "Privacidade garantida")
            FeatureItem("⚡", "Performance otimizada")
            FeatureItem("🌟", "Experiência personalizada")
        }
    }
}

@Composable
private fun CinematicTechCard() {
    CinematicGlassCard(
        icon = Icons.Rounded.Memory,
        title = "Tecnologia",
        gradientColors = listOf(
            Color(0xFF00BCD4),
            Color(0xFF2196F3)
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TechItem("Jetpack Compose", "Interface moderna")
            TechItem("Kotlin Coroutines", "Performance assíncrona")
            TechItem("Material 3", "Design system")
            TechItem("Room Database", "Armazenamento local")
            TechItem("Hilt DI", "Injeção de dependência")
        }
    }
}

@Composable
private fun CinematicContactCard() {
    val context = LocalContext.current
    
    CinematicGlassCard(
        icon = Icons.Rounded.ContactMail,
        title = "Contato",
        gradientColors = listOf(
            Color(0xFFFF9800),
            Color(0xFFFF5722)
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ContactButton(
                icon = Icons.Rounded.Email,
                label = "Email",
                subtitle = "richardsilva.devx@gmail.com"
            ) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:richardsilva.devx@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "SonsPhere - Contato")
                }
                context.startActivity(intent)
            }
            
            ContactButton(
                icon = Icons.Rounded.VideoLibrary,
                label = "YouTube",
                subtitle = "Tutoriais e conteúdo"
            ) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://www.youtube.com/channel/UC4aNT4rM6NhbDIVNfMcEWYg")
                }
                context.startActivity(intent)
            }
        }
    }
}

@Composable
private fun CinematicCreditsCard() {
    CinematicGlassCard(
        icon = Icons.Rounded.Star,
        title = "Créditos",
        gradientColors = listOf(
            Color(0xFF673AB7),
            Color(0xFF3F51B5)
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CreditItem("Richard Silva", "Desenvolvedor Principal")
            CreditItem("OuterTune Project", "Base do projeto")
            CreditItem("Material Design", "Sistema de design")
            CreditItem("Jetpack Compose", "Framework UI")
        }
    }
}

@Composable
private fun CinematicGlassCard(
    icon: ImageVector,
    title: String,
    gradientColors: List<Color>,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card")
    
    val borderAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "border"
    )
    
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (isPressed) 0.98f else 1f
                scaleY = if (isPressed) 0.98f else 1f
                shadowElevation = if (isPressed) 8.dp.toPx() else 16.dp.toPx()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = gradientColors.map { it.copy(alpha = 0.15f) }
                    )
                )
        ) {
            // Border animation
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val borderWidth = 2.dp.toPx()
                val cornerRadius = 24.dp.toPx()
                
                val gradientBrush = Brush.sweepGradient(
                    colors = gradientColors + gradientColors.first(),
                    center = Offset(
                        size.width * borderAnimation,
                        size.height * borderAnimation
                    )
                )
                
                drawRoundRect(
                    brush = gradientBrush,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(borderWidth)
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Brush.linearGradient(gradientColors),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                
                content()
            }
        }
    }
}

@Composable
private fun FeatureItem(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(alpha = 0.9f)
            )
        )
    }
}

@Composable
private fun TechItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
private fun ContactButton(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun CreditItem(name: String, role: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = role,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.7f)
                )
            )
        }
        Icon(
            Icons.Rounded.Star,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFFFFD700)
        )
    }
}

data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)
