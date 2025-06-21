/*
 * Copyright (C) 2025 SonsPhere Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.richard.musicplayer.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.richard.musicplayer.LocalPlayerAwareWindowInsets
import com.richard.musicplayer.ui.component.IconButton
import com.richard.musicplayer.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicensesScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
    ) {
        TopAppBar(
            title = { 
                Text(
                    "⚖️ Licenças Open Source",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LicenseCard(
                    title = "Compromisso com Open Source",
                    emoji = "🌟"
                ) {
                    Text(
                        "O SonsPhere é construído sobre a base sólida de projetos open source. Reconhecemos e agradecemos a todos os desenvolvedores e comunidades que tornaram este aplicativo possível.",
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }
            }

            item {
                LicenseCard(
                    title = "SonsPhere - GPL 3.0",
                    emoji = "🧩"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "O SonsPhere está licenciado sob GPL 3.0:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        LicenseBulletPoint("Código fonte aberto e disponível")
                        LicenseBulletPoint("Liberdade para usar, modificar e distribuir")
                        LicenseBulletPoint("Copyleft forte - derivados devem ser GPL")
                        LicenseBulletPoint("Proteção contra patents e DRM")
                        
                        Text(
                            "📁 Código fonte: github.com/richard-dev/sonpsphere",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                LicenseCard(
                    title = "Jetpack Compose - Apache 2.0",
                    emoji = "🎨"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Copyright © 2021 The Android Open Source Project",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        Text(
                            "Framework moderno para interface de usuário Android que revolucionou o desenvolvimento mobile.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            "🔗 developer.android.com/jetpack/compose",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                LicenseCard(
                    title = "Kotlin - Apache 2.0",
                    emoji = "⚡"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Copyright © 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        Text(
                            "Linguagem de programação moderna, concisa e segura que potencializa o desenvolvimento Android.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            "🔗 kotlinlang.org",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                LicenseCard(
                    title = "Material Design Components - Apache 2.0",
                    emoji = "🎯"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Copyright © 2016 Google Inc.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        Text(
                            "Sistema de design que proporciona experiências digitais coesas e intuitivas.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            "🔗 material.io",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                LicenseCard(
                    title = "ExoPlayer - Apache 2.0",
                    emoji = "🎵"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Copyright © 2016 Google Inc.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        Text(
                            "Player de mídia avançado para Android que oferece reprodução de alta qualidade e recursos extensos.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            "🔗 github.com/google/ExoPlayer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                LicenseCard(
                    title = "Room Database - Apache 2.0",
                    emoji = "🗄️"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Copyright © 2017 The Android Open Source Project",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        Text(
                            "Biblioteca de persistência que fornece uma camada de abstração sobre SQLite.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            "🔗 developer.android.com/training/data-storage/room",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                LicenseCard(
                    title = "Hilt Dependency Injection - Apache 2.0",
                    emoji = "💉"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Copyright © 2020 The Android Open Source Project",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        Text(
                            "Framework de injeção de dependência construído sobre Dagger para aplicações Android.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            "🔗 dagger.dev/hilt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                LicenseCard(
                    title = "Coil Image Loading - Apache 2.0",
                    emoji = "🖼️"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Copyright © 2023 Coil Contributors",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        Text(
                            "Biblioteca de carregamento de imagens para Android, construída com Kotlin Coroutines.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            "🔗 coil-kt.github.io/coil",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                LicenseCard(
                    title = "Kotlin Coroutines - Apache 2.0",
                    emoji = "🔄"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Copyright © 2016-2023 JetBrains s.r.o.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        Text(
                            "Biblioteca para programação assíncrona e reativa em Kotlin.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            "🔗 github.com/Kotlin/kotlinx.coroutines",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                LicenseCard(
                    title = "OuterTune Project - GPL 3.0",
                    emoji = "🚀"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Agradecimento especial ao projeto OuterTune que serviu como base e inspiração:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        LicenseBulletPoint("Arquitetura sólida e bem estruturada")
                        LicenseBulletPoint("Implementações de referência")
                        LicenseBulletPoint("Comunidade ativa e colaborativa")
                        LicenseBulletPoint("Código limpo e bem documentado")
                        
                        Text(
                            "🔗 github.com/DD3Boh/OuterTune",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                LicenseCard(
                    title = "Licença Apache 2.0 - Resumo",
                    emoji = "📄"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "A maioria das bibliotecas usa Apache 2.0, que permite:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        LicenseBulletPoint("Uso comercial e não comercial")
                        LicenseBulletPoint("Modificação e distribuição")
                        LicenseBulletPoint("Uso de patentes dos contribuidores")
                        LicenseBulletPoint("Sublicenciamento permitido")
                        LicenseBulletPoint("Atribuição obrigatória")
                    }
                }
            }

            item {
                LicenseCard(
                    title = "Licença GPL 3.0 - Resumo",
                    emoji = "⚖️"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "O SonsPhere usa GPL 3.0, garantindo:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        LicenseBulletPoint("Software livre para sempre")
                        LicenseBulletPoint("Código fonte sempre disponível")
                        LicenseBulletPoint("Derivados devem ser GPL 3.0")
                        LicenseBulletPoint("Proteção contra tivoization")
                        LicenseBulletPoint("Compatibilidade com outras licenças livres")
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "💝 Agradecimentos",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        Text(
                            "Nosso sincero agradecimento a todos os desenvolvedores, mantenedores e comunidades que criaram e mantêm estas bibliotecas incríveis. Sem vocês, o SonsPhere não seria possível.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                        
                        Text(
                            "O software livre é a base da inovação tecnológica! 🌍✨",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "⚠️ Avisos Legais",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        Text(
                            "• Todas as licenças são respeitadas integralmente",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "• Textos completos disponíveis nos repositórios originais",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "• Atribuições mantidas conforme requerido",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "• Conformidade com obrigações de cada licença",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun LicenseCard(
    title: String,
    emoji: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row {
                Text(
                    text = emoji,
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
            content()
        }
    }
}

@Composable
private fun LicenseBulletPoint(text: String) {
    Row {
        Text(
            text = "• ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
} 