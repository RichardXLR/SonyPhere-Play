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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.richard.musicplayer.LocalPlayerAwareWindowInsets
import com.richard.musicplayer.ui.component.IconButton
import com.richard.musicplayer.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
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
                    "🔐 Política de Privacidade",
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
                PolicyCard(
                    title = "Compromisso com Sua Privacidade",
                    emoji = "🛡️"
                ) {
                    Text(
                        "No SonsPhere, sua privacidade é nossa prioridade máxima. Desenvolvemos nossa plataforma musical com foco absoluto na proteção dos seus dados pessoais e experiência musical.",
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }
            }

            item {
                PolicyCard(
                    title = "Coleta de Dados",
                    emoji = "📊"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Coletamos apenas informações essenciais para fornecer nossa experiência musical:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        PolicyBulletPoint("Preferências musicais e configurações do app")
                        PolicyBulletPoint("Histórico de reprodução (armazenado localmente)")
                        PolicyBulletPoint("Configurações de interface e personalização")
                        PolicyBulletPoint("Dados técnicos para melhorar performance")
                    }
                }
            }

            item {
                PolicyCard(
                    title = "Uso das Informações",
                    emoji = "🎯"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Utilizamos seus dados exclusivamente para:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        PolicyBulletPoint("Personalizar sua experiência musical")
                        PolicyBulletPoint("Melhorar algoritmos de recomendação")
                        PolicyBulletPoint("Otimizar performance do aplicativo")
                        PolicyBulletPoint("Fornecer suporte técnico quando necessário")
                        
                        Text(
                            "❌ Jamais vendemos, alugamos ou compartilhamos seus dados com terceiros.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }

            item {
                PolicyCard(
                    title = "Armazenamento e Segurança",
                    emoji = "🔒"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PolicyBulletPoint("Dados armazenados localmente no seu dispositivo")
                        PolicyBulletPoint("Criptografia de ponta a ponta para dados sensíveis")
                        PolicyBulletPoint("Backup seguro opcional na nuvem")
                        PolicyBulletPoint("Acesso restrito apenas ao necessário")
                        PolicyBulletPoint("Exclusão automática de dados temporários")
                    }
                }
            }

            item {
                PolicyCard(
                    title = "Seus Direitos",
                    emoji = "⚖️"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "De acordo com a LGPD e regulamentações internacionais, você tem direito a:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        PolicyBulletPoint("Acessar todos os seus dados coletados")
                        PolicyBulletPoint("Corrigir informações incorretas")
                        PolicyBulletPoint("Solicitar exclusão completa dos dados")
                        PolicyBulletPoint("Portabilidade para outros serviços")
                        PolicyBulletPoint("Revogar consentimentos a qualquer momento")
                    }
                }
            }

            item {
                PolicyCard(
                    title = "Cookies e Tecnologias",
                    emoji = "🍪"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Utilizamos tecnologias para melhorar sua experiência:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        PolicyBulletPoint("Preferências de interface (tema, idioma)")
                        PolicyBulletPoint("Cache de músicas para reprodução offline")
                        PolicyBulletPoint("Estatísticas de uso agregadas e anônimas")
                        PolicyBulletPoint("Otimização de performance do app")
                    }
                }
            }

            item {
                PolicyCard(
                    title = "Menores de Idade",
                    emoji = "👶"
                ) {
                    Text(
                        "O SonsPhere é adequado para todas as idades. Para usuários menores de 13 anos, requeremos consentimento parental explícito. Implementamos proteções especiais para garantir segurança de crianças e adolescentes.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }

            item {
                PolicyCard(
                    title = "Alterações na Política",
                    emoji = "📝"
                ) {
                    Text(
                        "Qualquer alteração nesta política será comunicada através do aplicativo com 30 dias de antecedência. Mudanças significativas requerem seu consentimento explícito.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }

            item {
                PolicyCard(
                    title = "Contato e Suporte",
                    emoji = "📞"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Para questões sobre privacidade, entre em contato:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        
                        Text(
                            "📧 Email: richardsilva.devx@gmail.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            "💬 WhatsApp: Suporte 24/7",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            "⏱️ Tempo de resposta: Até 24 horas",
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "✨ Última Atualização",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            "Esta política foi atualizada em Janeiro de 2025",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Versão 1.0 - SonsPhere",
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
private fun PolicyCard(
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
private fun PolicyBulletPoint(text: String) {
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