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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.richard.musicplayer.BuildConfig
import com.richard.musicplayer.LocalPlayerAwareWindowInsets
import com.richard.musicplayer.R
import com.richard.musicplayer.ui.component.IconButton
import com.richard.musicplayer.ui.utils.backToMain
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
    ) {
        // TopAppBar moderna com gradiente sutil
        TopAppBar(
            title = { 
                Text(
                    "Sobre o App 🧩",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = navController::backToMain
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 🧩 App Name Header
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -50 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(animationSpec = tween(800))
                ) {
                    AppNameSection()
                }
            }
            
            // 🔢 Current Version
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInHorizontally(
                        initialOffsetX = { -100 },
                        animationSpec = tween(700, delayMillis = 200)
                    ) + fadeIn(animationSpec = tween(700, delayMillis = 200))
                ) {
                    VersionSection(navController)
                }
            }
            
            // 👨‍💻👩‍🎨 Team Credits
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInHorizontally(
                        initialOffsetX = { 100 },
                        animationSpec = tween(700, delayMillis = 400)
                    ) + fadeIn(animationSpec = tween(700, delayMillis = 400))
                ) {
                    TeamCreditsSection()
                }
            }
            
            // 📄🔐 Privacy & Terms
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { 50 },
                        animationSpec = tween(700, delayMillis = 600)
                    ) + fadeIn(animationSpec = tween(700, delayMillis = 600))
                ) {
                    PrivacyTermsSection(navController)
                }
            }
            
            // 💬📧 Contact & Support
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInHorizontally(
                        initialOffsetX = { -100 },
                        animationSpec = tween(700, delayMillis = 800)
                    ) + fadeIn(animationSpec = tween(700, delayMillis = 800))
                ) {
                    ContactSupportSection()
                }
            }
            
            // 🙏 Special Thanks
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { 50 },
                        animationSpec = tween(700, delayMillis = 1000)
                    ) + fadeIn(animationSpec = tween(700, delayMillis = 1000))
                ) {
                    SpecialThanksSection()
                }
            }
            
            // 🏷️ Footer
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(700, delayMillis = 1200))
                ) {
                    FooterSection()
                }
            }
            
            // Spacer final
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AppNameSection() {
    val infiniteTransition = rememberInfiniteTransition(label = "AppNameAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AppNameScale"
    )

    ModernCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo com gradiente e animação
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        ambientColor = Color(0xFF9C27B0),
                        spotColor = Color(0xFF9C27B0)
                    )
                    .background(
                        color = Color(0xFF9C27B0), // Cor roxa vibrante
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = R.drawable.app_icon,
                    contentDescription = "SonsPhere Logo",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .padding(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Nome do app com tipografia estilosa
            Text(
                text = "SonsPhere",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Your Musical Universe ✨",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VersionSection(navController: NavController) {
    val context = LocalContext.current
    
    ModernCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔢",
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Versão Atual",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Botão What's New
            FilledTonalButton(
                onClick = {
                    navController.navigate("changelog")
                },
                modifier = Modifier
                    .animateContentSize()
            ) {
                Icon(
                    Icons.Rounded.Newspaper,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Novidades")
            }
        }
    }
}

@Composable
private fun TeamCreditsSection() {
    ModernCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            SectionHeader(
                icon = "👨‍💻",
                title = "Nossa Equipe"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TeamMember(
                emoji = "💻",
                name = "Richard Silva",
                role = "Desenvolvedor Principal",
                description = "Arquitetura, UI/UX, Performance"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TeamMember(
                emoji = "🎨",
                name = "Design Team",
                role = "Interface & Experiência",
                description = "Material Design, Animações, Usabilidade"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TeamMember(
                emoji = "🔊",
                name = "Audio Engineers",
                role = "Qualidade Sonora",
                description = "Processamento, Algoritmos, Otimização"
            )
        }
    }
}

@Composable
private fun PrivacyTermsSection(navController: NavController) {
    val context = LocalContext.current
    
    ModernCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            SectionHeader(
                icon = "📄",
                title = "Políticas e Termos"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ActionButton(
                icon = "🔐",
                title = "Política de Privacidade",
                subtitle = "Como protegemos seus dados",
                onClick = {
                    navController.navigate("privacy_policy")
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ActionButton(
                icon = "📋",
                title = "Termos de Uso",
                subtitle = "Condições de utilização",
                onClick = {
                    navController.navigate("terms_of_use")
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ActionButton(
                icon = "⚖️",
                title = "Licenças Open Source",
                subtitle = "Bibliotecas e atribuições",
                onClick = {
                    navController.navigate("open_source_licenses")
                }
            )
        }
    }
}

@Composable
private fun ContactSupportSection() {
    val context = LocalContext.current
    
    ModernCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            SectionHeader(
                icon = "💬",
                title = "Contato e Suporte"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ContactButton(
                    emoji = "📧",
                    label = "Email",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:richardsilva.devx@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "SonsPhere - Suporte")
                        }
                        context.startActivity(intent)
                    }
                )
                
                ContactButton(
                    emoji = "💬",
                    label = "WhatsApp",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://wa.me/message/GRT4FIQS7SIFE1")
                        }
                        context.startActivity(intent)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ActionButton(
                icon = "🎥",
                title = "Canal no YouTube",
                subtitle = "Tutoriais e demonstrações",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://www.youtube.com/channel/UC4aNT4rM6NhbDIVNfMcEWYg")
                    }
                    context.startActivity(intent)
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ActionButton(
                icon = "🐛",
                title = "Reportar Bug",
                subtitle = "Ajude-nos a melhorar o app",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:richardsilva.devx@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "SonsPhere - Bug Report")
                        putExtra(Intent.EXTRA_TEXT, "Versão: ${BuildConfig.VERSION_NAME}\nDescreva o problema:\n\n")
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
private fun SpecialThanksSection() {
    ModernCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            SectionHeader(
                icon = "🙏",
                title = "Agradecimentos Especiais"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ThanksItem(
                emoji = "🚀",
                title = "OuterTune Project",
                description = "Base sólida e inspiração para este projeto"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ThanksItem(
                emoji = "🎯",
                title = "Material Design Team",
                description = "Sistema de design moderno e consistente"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ThanksItem(
                emoji = "💙",
                title = "Nossa Comunidade",
                description = "Feedback valioso e suporte constante"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ThanksItem(
                emoji = "🌟",
                title = "Beta Testers",
                description = "Testagem e relatórios de qualidade"
            )
        }
    }
}

@Composable
private fun FooterSection() {
    val buildDate = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date())
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏷️ Build Info",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Build: ${BuildConfig.VERSION_CODE}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Compilado em: $buildDate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Made with ❤️ in Brazil",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Componentes auxiliares

@Composable
private fun ModernCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        content()
    }
}

@Composable
private fun SectionHeader(
    icon: String,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun TeamMember(
    emoji: String,
    name: String,
    role: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = role,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Surface(
        onClick = { 
            isPressed = true
            onClick()
        },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (isPressed) 0.98f else 1f)
            .animateContentSize()
    ) {
        LaunchedEffect(isPressed) {
            if (isPressed) {
                kotlinx.coroutines.delay(100)
                isPressed = false
            }
        }
        
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContactButton(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Surface(
        onClick = { 
            isPressed = true
            onClick()
        },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
            .scale(if (isPressed) 0.95f else 1f)
            .animateContentSize()
    ) {
        LaunchedEffect(isPressed) {
            if (isPressed) {
                kotlinx.coroutines.delay(100)
                isPressed = false
            }
        }
        
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ThanksItem(
    emoji: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
