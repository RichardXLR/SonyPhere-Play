package com.dd3boh.outertune.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.R
import com.dd3boh.outertune.ui.component.*
import com.dd3boh.outertune.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecuritySettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    var incognitoMode by remember { mutableStateOf(false) }
    var biometricLock by remember { mutableStateOf(false) }
    var clearDataOnExit by remember { mutableStateOf(false) }
    var encryptBackups by remember { mutableStateOf(true) }
    var analyticsEnabled by remember { mutableStateOf(false) }
    var crashReportsEnabled by remember { mutableStateOf(true) }
    
    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
    ) {
        // Privacy Settings
        PreferenceGroupTitle(title = "Privacidade")
        
        SwitchPreference(
            title = { Text("Modo Incógnito") },
            description = "Não salvar histórico de reprodução",
            icon = { Icon(Icons.Rounded.VisibilityOff, null) },
            checked = incognitoMode,
            onCheckedChange = { incognitoMode = it }
        )
        
        SwitchPreference(
            title = { Text("Limpar Dados ao Sair") },
            description = "Apagar cache e dados temporários",
            icon = { Icon(Icons.Rounded.DeleteSweep, null) },
            checked = clearDataOnExit,
            onCheckedChange = { clearDataOnExit = it }
        )
        
        PreferenceEntry(
            title = { Text("Limpar Histórico") },
            description = "Apagar todo o histórico de reprodução",
            icon = { Icon(Icons.Rounded.History, null) },
            onClick = { /* TODO: Clear history */ }
        )
        
        PreferenceEntry(
            title = { Text("Gerenciar Permissões") },
            description = "Controlar permissões do app",
            icon = { Icon(Icons.Rounded.Security, null) },
            onClick = { /* TODO: Open permissions */ }
        )
        
        // Security Settings
        PreferenceGroupTitle(title = "Segurança")
        
        SwitchPreference(
            title = { Text("Bloqueio Biométrico") },
            description = "Usar impressão digital ou face para desbloquear",
            icon = { Icon(Icons.Rounded.Fingerprint, null) },
            checked = biometricLock,
            onCheckedChange = { biometricLock = it }
        )
        
        PreferenceEntry(
            title = { Text("Alterar PIN") },
            description = "Definir código de acesso",
            icon = { Icon(Icons.Rounded.Pin, null) },
            onClick = { /* TODO: Change PIN */ }
        )
        
        SwitchPreference(
            title = { Text("Criptografar Backups") },
            description = "Proteger backups com senha",
            icon = { Icon(Icons.Rounded.Lock, null) },
            checked = encryptBackups,
            onCheckedChange = { encryptBackups = it }
        )
        
        PreferenceEntry(
            title = { Text("Sessões Ativas") },
            description = "Gerenciar dispositivos conectados",
            icon = { Icon(Icons.Rounded.Devices, null) },
            onClick = { /* TODO: Show active sessions */ }
        )
        
        // Data & Analytics
        PreferenceGroupTitle(title = "Dados e Análises")
        
        SwitchPreference(
            title = { Text("Análises de Uso") },
            description = "Ajudar a melhorar o app com dados anônimos",
            icon = { Icon(Icons.Rounded.Analytics, null) },
            checked = analyticsEnabled,
            onCheckedChange = { analyticsEnabled = it }
        )
        
        SwitchPreference(
            title = { Text("Relatórios de Erro") },
            description = "Enviar relatórios automáticos de falhas",
            icon = { Icon(Icons.Rounded.BugReport, null) },
            checked = crashReportsEnabled,
            onCheckedChange = { crashReportsEnabled = it }
        )
        
        PreferenceEntry(
            title = { Text("Exportar Dados") },
            description = "Baixar todos os seus dados",
            icon = { Icon(Icons.Rounded.Download, null) },
            onClick = { /* TODO: Export data */ }
        )
        
        PreferenceEntry(
            title = { Text("Excluir Conta") },
            description = "Remover permanentemente todos os dados",
            icon = { Icon(Icons.Rounded.DeleteForever, null) },
            onClick = { /* TODO: Delete account */ }
        )
        
        // Network Security
        PreferenceGroupTitle(title = "Segurança de Rede")
        
        SwitchPreference(
            title = { Text("Forçar HTTPS") },
            description = "Usar apenas conexões seguras",
            icon = { Icon(Icons.Rounded.Https, null) },
            checked = true,
            onCheckedChange = { /* TODO: Save preference */ }
        )
        
        SwitchPreference(
            title = { Text("Bloquear Rastreadores") },
            description = "Impedir rastreamento de terceiros",
            icon = { Icon(Icons.Rounded.Block, null) },
            checked = true,
            onCheckedChange = { /* TODO: Save preference */ }
        )
        
        PreferenceEntry(
            title = { Text("Configurar Proxy") },
            description = "Usar servidor proxy personalizado",
            icon = { Icon(Icons.Rounded.VpnKey, null) },
            onClick = { /* TODO: Configure proxy */ }
        )
        
        Spacer(Modifier.height(16.dp))
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