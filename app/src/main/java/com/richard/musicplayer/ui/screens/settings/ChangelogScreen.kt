/*
 * Copyright (C) 2025 SonsPhere Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.richard.musicplayer.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.richard.musicplayer.BuildConfig
import com.richard.musicplayer.LocalPlayerAwareWindowInsets
import com.richard.musicplayer.ui.component.IconButton
import com.richard.musicplayer.ui.utils.backToMain
import kotlinx.coroutines.delay

data class ChangelogEntry(
    val version: String,
    val versionCode: Int,
    val date: String,
    val type: ChangelogType,
    val title: String,
    val description: String,
    val features: List<ChangelogFeature>
)

data class ChangelogFeature(
    val icon: String,
    val title: String,
    val description: String,
    val type: FeatureType
)

enum class ChangelogType(val emoji: String, val color: Color) {
    MAJOR("🚀", Color(0xFF4CAF50)),
    MINOR("✨", Color(0xFF2196F3)),
    PATCH("🔧", Color(0xFFFF9800)),
    HOTFIX("🚨", Color(0xFFF44336))
}

enum class FeatureType(val emoji: String) {
    NEW("🆕"),
    IMPROVED("⚡"),
    FIXED("🔧"),
    REMOVED("❌"),
    SECURITY("🔒")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val changelog = remember { getChangelogData() }
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
    ) {
        TopAppBar(
            title = { 
                Text(
                    "📰 Novidades",
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
            scrollBehavior = scrollBehavior
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -50 },
                        animationSpec = tween(700)
                    ) + fadeIn(animationSpec = tween(700))
                ) {
                    CurrentVersionCard()
                }
            }
            
            itemsIndexed(changelog) { index, entry ->
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { 50 },
                        animationSpec = tween(700, delayMillis = (index + 1) * 100)
                    ) + fadeIn(animationSpec = tween(700, delayMillis = (index + 1) * 100))
                ) {
                    ChangelogEntryCard(entry = entry)
                }
            }
            
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { 50 },
                        animationSpec = tween(700, delayMillis = (changelog.size + 2) * 100)
                    ) + fadeIn(animationSpec = tween(700, delayMillis = (changelog.size + 2) * 100))
                ) {
                    ChangelogFooter()
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun CurrentVersionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500),
                    repeatMode = RepeatMode.Reverse
                ), label = ""
            )
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎵",
                    fontSize = 40.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "SonsPhere ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "Build ${BuildConfig.VERSION_CODE} • Janeiro 2025",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "✨ Versão mais moderna e completa do reprodutor musical!",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ChangelogEntryCard(entry: ChangelogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                entry.type.color.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.type.emoji,
                            fontSize = 16.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = "v${entry.version}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = entry.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = entry.type.color.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = entry.type.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = entry.type.color
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            entry.features.forEach { feature ->
                FeatureItem(feature = feature)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FeatureItem(feature: ChangelogFeature) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "${feature.type.emoji} ${feature.icon}",
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChangelogFooter() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🚀 Mais por vir!",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Estamos trabalhando constantemente para trazer novos recursos e melhorias. Fique atento às próximas atualizações!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "💙 Obrigado por usar o SonsPhere!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun getChangelogData(): List<ChangelogEntry> {
    return listOf(
        ChangelogEntry(
            version = "25.06.1",
            versionCode = BuildConfig.VERSION_CODE,
            date = "Janeiro 2025",
            type = ChangelogType.MAJOR,
            title = "SonsPhere - Revolução Musical Completa! 🎵",
            description = "Uma completa reformulação do aplicativo com foco em design moderno, performance ultra e experiência do usuário excepcional. Esta é a maior atualização já lançada!",
            features = listOf(
                ChangelogFeature(
                    icon = "🧩",
                    title = "Tela 'Sobre' Redesenhada",
                    description = "Interface completamente nova com animações suaves, design moderno e informações organizadas",
                    type = FeatureType.NEW
                ),
                ChangelogFeature(
                    icon = "🔐",
                    title = "Política de Privacidade Completa",
                    description = "Documento detalhado sobre proteção de dados e conformidade com LGPD",
                    type = FeatureType.NEW
                ),
                ChangelogFeature(
                    icon = "📋",
                    title = "Termos de Uso Abrangentes",
                    description = "Condições claras de utilização e direitos do usuário",
                    type = FeatureType.NEW
                ),
                ChangelogFeature(
                    icon = "⚖️",
                    title = "Licenças Open Source",
                    description = "Reconhecimento completo de todas as bibliotecas e projetos utilizados",
                    type = FeatureType.NEW
                ),
                ChangelogFeature(
                    icon = "💬",
                    title = "WhatsApp Atualizado",
                    description = "Link direto para suporte via WhatsApp com resposta rápida",
                    type = FeatureType.IMPROVED
                )
            )
        ),
        ChangelogEntry(
            version = "25.06.0",
            versionCode = BuildConfig.VERSION_CODE - 1,
            date = "Janeiro 2025",
            type = ChangelogType.MAJOR,
            title = "Mini Player Vinil & Performance Ultra! 🎵",
            description = "Introdução do revolucionário Mini Player em formato vinil com animações realistas e otimizações massivas de performance para dispositivos de alta taxa de atualização.",
            features = listOf(
                ChangelogFeature(
                    icon = "💿",
                    title = "VinylMiniPlayer",
                    description = "Mini player circular com animação de vinil girando, grooves realistas e efeitos de luz",
                    type = FeatureType.NEW
                ),
                ChangelogFeature(
                    icon = "🚀",
                    title = "Performance Ultra",
                    description = "Otimizações para 120Hz/144Hz/165Hz+ com taxa de atualização máxima forçada",
                    type = FeatureType.IMPROVED
                ),
                ChangelogFeature(
                    icon = "⚡",
                    title = "Controle de Velocidade 2x",
                    description = "Botão flash personalizado para alternar velocidade de reprodução instantaneamente",
                    type = FeatureType.NEW
                ),
                ChangelogFeature(
                    icon = "🎨",
                    title = "Splash Screen Moderno",
                    description = "Tela inicial redesenhada com partículas animadas e ondas sonoras abstratas",
                    type = FeatureType.IMPROVED
                ),
                ChangelogFeature(
                    icon = "🔒",
                    title = "Tela de Bloqueio Refinada",
                    description = "AuthScreen limpo e minimalista com animações suaves",
                    type = FeatureType.IMPROVED
                )
            )
        ),
        ChangelogEntry(
            version = "25.05.2",
            versionCode = BuildConfig.VERSION_CODE - 2,
            date = "Janeiro 2025",
            type = ChangelogType.MINOR,
            title = "Otimizações e Correções de Ícones 🔧",
            description = "Ajustes importantes nos atalhos do sistema e otimizações de compatibilidade com diferentes launchers Android.",
            features = listOf(
                ChangelogFeature(
                    icon = "📱",
                    title = "Ícones de Atalho Corrigidos",
                    description = "Ícone 'Músicas' agora usa o logo oficial do SonsPhere nos shortcuts",
                    type = FeatureType.FIXED
                ),
                ChangelogFeature(
                    icon = "🔄",
                    title = "Cache de Launcher Otimizado",
                    description = "Melhor compatibilidade com MIUI, One UI e outros launchers personalizados",
                    type = FeatureType.IMPROVED
                ),
                ChangelogFeature(
                    icon = "🗑️",
                    title = "Limpeza de Código",
                    description = "Remoção de funcionalidades obsoletas e otimização da base de código",
                    type = FeatureType.REMOVED
                ),
                ChangelogFeature(
                    icon = "🔇",
                    title = "Remoção do Bass Control",
                    description = "Sistema de Bass removido para simplificar a interface de áudio",
                    type = FeatureType.REMOVED
                )
            )
        ),
        ChangelogEntry(
            version = "25.05.1",
            versionCode = BuildConfig.VERSION_CODE - 3,
            date = "Janeiro 2025",
            type = ChangelogType.PATCH,
            title = "Configuração Simplificada 🛠️",
            description = "Remoção da tela de configuração inicial para uma experiência mais direta e imediata.",
            features = listOf(
                ChangelogFeature(
                    icon = "🚪",
                    title = "Setup Wizard Removido",
                    description = "App inicia diretamente na tela principal sem configurações obrigatórias",
                    type = FeatureType.REMOVED
                ),
                ChangelogFeature(
                    icon = "⚡",
                    title = "Inicialização Mais Rápida",
                    description = "Tempo de carregamento reduzido significativamente",
                    type = FeatureType.IMPROVED
                ),
                ChangelogFeature(
                    icon = "👤",
                    title = "Experiência Simplificada",
                    description = "Foco na música desde o primeiro momento de uso",
                    type = FeatureType.IMPROVED
                )
            )
        ),
        ChangelogEntry(
            version = "25.05.0",
            versionCode = BuildConfig.VERSION_CODE - 4,
            date = "Janeiro 2025",
            type = ChangelogType.MAJOR,
            title = "Segurança Biométrica & Base Sólida 🔐",
            description = "Implementação completa do sistema de segurança biométrica e estabelecimento da arquitetura base do SonsPhere.",
            features = listOf(
                ChangelogFeature(
                    icon = "👆",
                    title = "Autenticação Biométrica",
                    description = "Bloqueio do app com impressão digital, Face ID ou PIN personalizado",
                    type = FeatureType.NEW
                ),
                ChangelogFeature(
                    icon = "🔒",
                    title = "Configurações de Privacidade",
                    description = "Painel completo para gerenciar segurança e privacidade",
                    type = FeatureType.NEW
                ),
                ChangelogFeature(
                    icon = "🏗️",
                    title = "Arquitetura Base",
                    description = "Estrutura sólida baseada no OuterTune com Clean Architecture",
                    type = FeatureType.NEW
                ),
                ChangelogFeature(
                    icon = "🎵",
                    title = "Core Musical Estável",
                    description = "Sistema de reprodução robusto com ExoPlayer e Room Database",
                    type = FeatureType.NEW
                )
            )
        )
    )
} 