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
fun TermsOfUseScreen(
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
                    "📋 Termos de Uso",
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
                TermCard(
                    title = "Boas-vindas ao SonsPhere",
                    emoji = "🎵"
                ) {
                    Text(
                        "Bem-vindo ao SonsPhere! Estes termos governam o uso do nosso aplicativo de música. Ao utilizar nossos serviços, você concorda com estes termos e condições.",
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }
            }

            item {
                TermCard(
                    title = "Aceitação dos Termos",
                    emoji = "✅"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Ao usar o SonsPhere, você concorda com:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        TermBulletPoint("Todos os termos e condições descritos")
                        TermBulletPoint("Nossa Política de Privacidade")
                        TermBulletPoint("Diretrizes da comunidade")
                        TermBulletPoint("Atualizações futuras dos termos")
                    }
                }
            }

            item {
                TermCard(
                    title = "Uso Permitido",
                    emoji = "🎯"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "O SonsPhere pode ser usado para:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        TermBulletPoint("Reprodução de música pessoal")
                        TermBulletPoint("Organização de biblioteca musical")
                        TermBulletPoint("Criação de playlists personalizadas")
                        TermBulletPoint("Descoberta de novos conteúdos")
                        TermBulletPoint("Compartilhamento dentro dos limites legais")
                    }
                }
            }

            item {
                TermCard(
                    title = "Uso Proibido",
                    emoji = "🚫"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "É estritamente proibido:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        TermBulletPoint("Pirataria ou violação de direitos autorais")
                        TermBulletPoint("Distribuição ilegal de conteúdo")
                        TermBulletPoint("Modificação não autorizada do aplicativo")
                        TermBulletPoint("Uso para fins comerciais sem permissão")
                        TermBulletPoint("Tentativas de invasão ou hacking")
                        TermBulletPoint("Spam ou comportamento abusivo")
                    }
                }
            }

            item {
                TermCard(
                    title = "Direitos Autorais e Propriedade Intelectual",
                    emoji = "©️"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Respeitamos e protegemos direitos autorais:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        TermBulletPoint("Suporte apenas para música legalmente adquirida")
                        TermBulletPoint("Conformidade com leis de direitos autorais")
                        TermBulletPoint("Sistema de denúncias para violações")
                        TermBulletPoint("Cooperação com detentores de direitos")
                        
                        Text(
                            "🎼 O SonsPhere não hospeda nem distribui música protegida por direitos autorais.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            item {
                TermCard(
                    title = "Responsabilidades do Usuário",
                    emoji = "👤"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Como usuário, você é responsável por:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        TermBulletPoint("Manter suas credenciais seguras")
                        TermBulletPoint("Usar apenas conteúdo legalmente adquirido")
                        TermBulletPoint("Respeitar outros usuários")
                        TermBulletPoint("Reportar problemas e violações")
                        TermBulletPoint("Manter o aplicativo atualizado")
                    }
                }
            }

            item {
                TermCard(
                    title = "Limitação de Responsabilidade",
                    emoji = "⚠️"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "O SonsPhere é fornecido 'como está'. Não nos responsabilizamos por:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        TermBulletPoint("Interrupções no serviço")
                        TermBulletPoint("Perda de dados devido a falhas técnicas")
                        TermBulletPoint("Danos indiretos ou consequenciais")
                        TermBulletPoint("Problemas com conteúdo de terceiros")
                        TermBulletPoint("Incompatibilidade com dispositivos específicos")
                    }
                }
            }

            item {
                TermCard(
                    title = "Atualizações e Modificações",
                    emoji = "🔄"
                ) {
                    Text(
                        "Reservamo-nos o direito de atualizar o aplicativo e estes termos a qualquer momento. Mudanças significativas serão comunicadas com antecedência mínima de 15 dias.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }



            item {
                TermCard(
                    title = "Lei Aplicável",
                    emoji = "⚖️"
                ) {
                    Text(
                        "Estes termos são regidos pelas leis brasileiras, incluindo a Lei Geral de Proteção de Dados (LGPD), Marco Civil da Internet e Código de Defesa do Consumidor.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }

            item {
                TermCard(
                    title = "Resolução de Disputas",
                    emoji = "🤝"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Para resolver conflitos, seguimos esta ordem:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        TermBulletPoint("1. Negociação direta via suporte")
                        TermBulletPoint("2. Mediação através do Procon")
                        TermBulletPoint("3. Arbitragem, se aplicável")
                        TermBulletPoint("4. Judiciário como última instância")
                    }
                }
            }

            item {
                TermCard(
                    title = "Contato para Questões Legais",
                    emoji = "📞"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Para questões relacionadas aos termos:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        Text(
                            "📧 Email: richardsilva.devx@gmail.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            "💬 WhatsApp: Suporte Legal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            "⏱️ Prazo de resposta: Até 5 dias úteis",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "📅 Vigência dos Termos",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            "Estes termos entram em vigor a partir de Janeiro de 2025",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Versão 1.0 - SonsPhere Termos de Uso",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun TermCard(
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
private fun TermBulletPoint(text: String) {
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