package com.richard.musicplayer.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.richard.musicplayer.LocalPlayerAwareWindowInsets
import com.richard.musicplayer.R
import com.richard.musicplayer.ui.component.*
import com.richard.musicplayer.ui.utils.backToMain
import com.richard.musicplayer.viewmodels.PrivacySecurityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecuritySettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: PrivacySecurityViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    
    val showClearHistoryDialog = false
    val showDeleteAccountDialog = false
    
    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
    ) {
        // Segurança - apenas Bloqueio Biométrico
        PreferenceGroupTitle(title = "Segurança")
        
        SwitchPreference(
            title = { Text("Bloqueio Biométrico") },
            description = "Usar impressão digital ou face para desbloquear",
            icon = { Icon(Icons.Rounded.Fingerprint, null) },
            checked = state.biometricLock,
            onCheckedChange = viewModel::setBiometricLock
        )
        
        // Dados e Análises
        PreferenceGroupTitle(title = "Dados e Análises")
        
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