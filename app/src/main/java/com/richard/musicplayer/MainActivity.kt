/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 O​u​t​er​Tu​ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.richard.musicplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.view.Display
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.core.util.Consumer
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.imageLoader
import coil.request.ImageRequest
import com.richard.musicplayer.constants.AppBarHeight
import com.richard.musicplayer.constants.SearchBarHeight
import com.richard.musicplayer.constants.AutomaticScannerKey
import com.richard.musicplayer.constants.DarkMode
import com.richard.musicplayer.constants.DarkModeKey
import com.richard.musicplayer.constants.DefaultOpenTabKey
import com.richard.musicplayer.constants.DynamicThemeKey
import com.richard.musicplayer.constants.MaterialYouKey
import com.richard.musicplayer.constants.EnabledTabsKey
import com.richard.musicplayer.constants.ExcludedScanPathsKey
import com.richard.musicplayer.constants.FirstSetupPassed
import com.richard.musicplayer.constants.NotificationPermissionAsked
import com.richard.musicplayer.constants.LibraryFilterKey
import com.richard.musicplayer.constants.LocalLibraryEnableKey
import com.richard.musicplayer.constants.LookupYtmArtistsKey
import com.richard.musicplayer.constants.MiniPlayerHeight
import com.richard.musicplayer.constants.NavigationBarAnimationSpec
import com.richard.musicplayer.constants.NavigationBarHeight
import com.richard.musicplayer.constants.PauseSearchHistoryKey
import com.richard.musicplayer.constants.PlayerBackgroundStyle
import com.richard.musicplayer.constants.PlayerBackgroundStyleKey
import com.richard.musicplayer.constants.PureBlackKey
import com.richard.musicplayer.constants.ScanPathsKey
import com.richard.musicplayer.constants.ScannerImpl
import com.richard.musicplayer.constants.ScannerImplKey
import com.richard.musicplayer.constants.ScannerMatchCriteria
import com.richard.musicplayer.constants.ScannerSensitivityKey
import com.richard.musicplayer.constants.ScannerStrictExtKey
import com.richard.musicplayer.constants.SearchSource
import com.richard.musicplayer.constants.SearchSourceKey
import com.richard.musicplayer.constants.SlimNavBarKey
import com.richard.musicplayer.constants.StopMusicOnTaskClearKey
import com.richard.musicplayer.db.MusicDatabase
import com.richard.musicplayer.db.entities.SearchHistory
import com.richard.musicplayer.playback.DownloadUtil
import com.richard.musicplayer.playback.MusicService
import com.richard.musicplayer.playback.MusicService.MusicBinder
import com.richard.musicplayer.playback.PlayerConnection
import com.richard.musicplayer.ui.component.BottomSheetMenu
import com.richard.musicplayer.ui.component.LocalMenuState
import com.richard.musicplayer.ui.component.NotificationPermissionDialog
import com.richard.musicplayer.ui.component.SearchBar
import com.richard.musicplayer.ui.component.rememberBottomSheetState
import com.richard.musicplayer.ui.component.shimmer.ShimmerTheme
import com.richard.musicplayer.ui.menu.YouTubeSongMenu
import com.richard.musicplayer.ui.player.BottomSheetPlayer
import com.richard.musicplayer.ui.screens.AccountScreen
import com.richard.musicplayer.ui.screens.AlbumScreen
import com.richard.musicplayer.ui.screens.BrowseScreen
import com.richard.musicplayer.ui.screens.HistoryScreen
import com.richard.musicplayer.ui.screens.HomeScreen
import com.richard.musicplayer.ui.screens.LoginScreen
import com.richard.musicplayer.ui.screens.MoodAndGenresScreen
import com.richard.musicplayer.ui.screens.Screens
import com.richard.musicplayer.ui.screens.Screens.LibraryFilter

import com.richard.musicplayer.ui.screens.StatsScreen
import com.richard.musicplayer.ui.screens.YouTubeBrowseScreen
import com.richard.musicplayer.ui.screens.artist.ArtistAlbumsScreen
import com.richard.musicplayer.ui.screens.artist.ArtistItemsScreen
import com.richard.musicplayer.ui.screens.artist.ArtistScreen
import com.richard.musicplayer.ui.screens.artist.ArtistSongsScreen
import com.richard.musicplayer.ui.screens.library.LibraryAlbumsScreen
import com.richard.musicplayer.ui.screens.library.LibraryArtistsScreen
import com.richard.musicplayer.ui.screens.library.LibraryFoldersScreen
import com.richard.musicplayer.ui.screens.library.LibraryPlaylistsScreen
import com.richard.musicplayer.ui.screens.library.LibraryScreen
import com.richard.musicplayer.ui.screens.library.LibrarySongsScreen
import com.richard.musicplayer.ui.screens.playlist.AutoPlaylistScreen
import com.richard.musicplayer.ui.screens.playlist.LocalPlaylistScreen
import com.richard.musicplayer.ui.screens.playlist.OnlinePlaylistScreen
import com.richard.musicplayer.ui.screens.search.LocalSearchScreen
import com.richard.musicplayer.ui.screens.search.OnlineSearchResult
import com.richard.musicplayer.ui.screens.search.OnlineSearchScreen
import com.richard.musicplayer.ui.screens.settings.AboutScreen
import com.richard.musicplayer.ui.screens.settings.AccountSyncSettings
import com.richard.musicplayer.ui.screens.settings.AppearanceSettings
import com.richard.musicplayer.ui.screens.settings.BackupAndRestore
import com.richard.musicplayer.constants.DEFAULT_ENABLED_TABS
import com.richard.musicplayer.ui.screens.library.FolderScreen

import com.richard.musicplayer.ui.screens.settings.InterfaceSettings
import com.richard.musicplayer.ui.screens.settings.LibrarySettings
import com.richard.musicplayer.ui.screens.settings.LocalPlayerSettings
import com.richard.musicplayer.ui.screens.settings.LyricsSettings
import com.richard.musicplayer.ui.screens.settings.PlayerSettings
import com.richard.musicplayer.ui.screens.settings.SettingsScreen
import com.richard.musicplayer.ui.screens.settings.StorageSettings
import com.richard.musicplayer.ui.theme.ColorSaver
import com.richard.musicplayer.ui.theme.DefaultThemeColor
import com.richard.musicplayer.ui.theme.OuterTuneTheme
import com.richard.musicplayer.ui.theme.extractThemeColor
import com.richard.musicplayer.ui.utils.DEFAULT_SCAN_PATH
import com.richard.musicplayer.ui.utils.MEDIA_PERMISSION_LEVEL
import com.richard.musicplayer.ui.utils.appBarScrollBehavior
import com.richard.musicplayer.ui.utils.clearDtCache
import com.richard.musicplayer.ui.utils.imageCache
import com.richard.musicplayer.ui.utils.resetHeightOffset
import com.richard.musicplayer.ui.utils.NavigationTransitions
import com.richard.musicplayer.utils.ActivityLauncherHelper
import com.richard.musicplayer.utils.NetworkConnectivityObserver
import com.richard.musicplayer.utils.ApplyPerformanceOptimizations
import com.richard.musicplayer.utils.ApplyHighRefreshRateOptimizations
import com.richard.musicplayer.utils.SyncUtils
import com.richard.musicplayer.utils.dataStore
import com.richard.musicplayer.utils.get
import com.richard.musicplayer.utils.rememberEnumPreference
import com.richard.musicplayer.utils.rememberPreference
import com.richard.musicplayer.utils.reportException
import com.richard.musicplayer.utils.scanners.LocalMediaScanner
import com.richard.musicplayer.utils.scanners.LocalMediaScanner.Companion.destroyScanner
import com.richard.musicplayer.utils.scanners.LocalMediaScanner.Companion.scannerActive
import com.richard.musicplayer.utils.scanners.ScannerAbortException
import com.richard.musicplayer.utils.urlEncode
import com.valentinilk.shimmer.LocalShimmerTheme
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.SongItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.richard.musicplayer.ui.screens.settings.PrivacySecuritySettings
import com.richard.musicplayer.ui.screens.settings.PinChangeScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.richard.musicplayer.viewmodels.PrivacySecurityViewModel
import com.richard.musicplayer.ui.screens.AuthScreen
import com.richard.musicplayer.utils.AudioFeedbackManager
import androidx.biometric.BiometricManager

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject
    lateinit var database: MusicDatabase

    @Inject
    lateinit var downloadUtil: DownloadUtil

    @Inject
    lateinit var syncUtils: SyncUtils
    
    @Inject
    lateinit var audioFeedbackManager: AudioFeedbackManager

    lateinit var activityLauncher: ActivityLauncherHelper
    lateinit var connectivityObserver: NetworkConnectivityObserver

    private var playerConnection by mutableStateOf<PlayerConnection?>(null)
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is MusicBinder) {
                playerConnection = PlayerConnection(service, database, lifecycleScope)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playerConnection?.dispose()
            playerConnection = null
        }
    }

    // storage permission helpers
    val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
//                Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.scanner_missing_storage_perm), Toast.LENGTH_SHORT).show()
            }
        }

    override fun onStart() {
        super.onStart()
        startService(Intent(this, MusicService::class.java))
        bindService(Intent(this, MusicService::class.java), serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        try {
            connectivityObserver.unregister()
        } catch (e: UninitializedPropertyAccessException) {
            // lol
        }

        /*
         * While music is playing:
         *      StopMusicOnTaskClearKey true: clearing from recent apps will kill service
         *      StopMusicOnTaskClearKey false: clearing from recent apps will NOT kill service
         * While music is not playing: 
         *      Service will never be automatically killed
         *
         * Regardless of what happens, queues and last position are saves
         */
        super.onDestroy()
        unbindService(serviceConnection)

        if (dataStore.get(StopMusicOnTaskClearKey, false) && isFinishing) {
//                stopService(Intent(this, MusicService::class.java)) // Believe me, this doesn't actually stop
            playerConnection?.service?.onDestroy()
            playerConnection = null
        } else {
            playerConnection?.service?.saveQueueToDisk()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint(
        "UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition",
        "StateFlowValueCalledInComposition", "UnusedBoxWithConstraintsScope"
    )
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // CORREÇÃO: Garantir fundo escuro imediato para eliminar tela branca
        window.statusBarColor = android.graphics.Color.parseColor("#0D0E1F")
        window.navigationBarColor = android.graphics.Color.parseColor("#0D0E1F")
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 🚀 SISTEMA DE TAXA MÁXIMA DE ATUALIZAÇÃO - FORÇA 120Hz/144Hz/165Hz
        setupMaxRefreshRate()

        activityLauncher = ActivityLauncherHelper(this)

        setContent {
            // Aplicar otimizações de performance ultra para 60/90/120 FPS
            ApplyPerformanceOptimizations()
            
            // 🚀 APLICAR OTIMIZAÇÕES ESPECÍFICAS PARA ALTA TAXA DE ATUALIZAÇÃO
            ApplyHighRefreshRateOptimizations()
            
            val haptic = LocalHapticFeedback.current

            val enableDynamicTheme by rememberPreference(DynamicThemeKey, defaultValue = true)
            val enableMaterialYou by rememberPreference(MaterialYouKey, defaultValue = false)
            val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
            val pureBlack by rememberPreference(PureBlackKey, defaultValue = true)
            val isSystemInDarkTheme = isSystemInDarkTheme()
            val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
                if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
            }
            LaunchedEffect(useDarkTheme) {
                setSystemBarAppearance(useDarkTheme)
            }
            var themeColor by rememberSaveable(stateSaver = ColorSaver) {
                mutableStateOf(DefaultThemeColor)
            }

            val playerBackground by rememberEnumPreference(
                key = PlayerBackgroundStyleKey,
                defaultValue = PlayerBackgroundStyle.BLUR
            )

            try {
                connectivityObserver.unregister()
            } catch (e: UninitializedPropertyAccessException) {
                // lol
            }
            connectivityObserver = NetworkConnectivityObserver(this@MainActivity)
            val isNetworkConnected by connectivityObserver.networkStatus.collectAsState(true)

            LaunchedEffect(playerConnection, enableDynamicTheme, enableMaterialYou, isSystemInDarkTheme) {
                val playerConnection = playerConnection
                
                // Se Material You está habilitado, usar tema do sistema
                if (enableMaterialYou) {
                    themeColor = DefaultThemeColor
                    return@LaunchedEffect
                }
                
                // Caso contrário, usar tema dinâmico do álbum se habilitado
                if (!enableDynamicTheme || playerConnection == null) {
                    themeColor = DefaultThemeColor
                    return@LaunchedEffect
                }
                
                playerConnection.service.currentMediaMetadata.collectLatest { song ->
                    themeColor = if (song != null) {
                        withContext(Dispatchers.IO) {
                            val result = imageLoader.execute(
                                ImageRequest.Builder(this@MainActivity)
                                    .data(song.thumbnailUrl)
                                    .allowHardware(false) // pixel access is not supported on Config#HARDWARE bitmaps
                                    .build()
                            )
                            (result.drawable as? BitmapDrawable)?.bitmap?.extractThemeColor()
                                ?: DefaultThemeColor
                        }
                    } else DefaultThemeColor
                }
            }

            val (firstSetupPassed) = rememberPreference(FirstSetupPassed, defaultValue = true)
            val (notificationPermissionAsked, setNotificationPermissionAsked) = rememberPreference(NotificationPermissionAsked, defaultValue = false)
            val (localLibEnable) = rememberPreference(LocalLibraryEnableKey, defaultValue = true)
            
            // Estado do diálogo de permissão de notificação
            var showNotificationDialog by remember { mutableStateOf(false) }

            // auto scanner
            val (scannerSensitivity) = rememberEnumPreference(
                key = ScannerSensitivityKey,
                defaultValue = ScannerMatchCriteria.LEVEL_2
            )
            val (scannerImpl) = rememberEnumPreference(
                key = ScannerImplKey,
                defaultValue = ScannerImpl.TAGLIB
            )
            val (scanPaths) = rememberPreference(ScanPathsKey, defaultValue = DEFAULT_SCAN_PATH)
            val (excludedScanPaths) = rememberPreference(ExcludedScanPathsKey, defaultValue = "")
            val (strictExtensions) = rememberPreference(ScannerStrictExtKey, defaultValue = false)
            val (lookupYtmArtists) = rememberPreference(LookupYtmArtistsKey, defaultValue = true)
            val (autoScan) = rememberPreference(AutomaticScannerKey, defaultValue = true)
            LaunchedEffect(Unit) {
                downloadUtil.resumeDownloadsOnStart()

                CoroutineScope(Dispatchers.IO).launch {
                    val perms = checkSelfPermission(MEDIA_PERMISSION_LEVEL)
                    // Check if the permissions for local media access
                    if (!scannerActive.value && autoScan && firstSetupPassed && localLibEnable) {
                        if (perms == PackageManager.PERMISSION_GRANTED) {
                            // equivalent to (quick scan)
                            try {
                                withContext(Dispatchers.Main) {
                                    playerConnection?.player?.pause()
                                }
                                val scanner = LocalMediaScanner.getScanner(this@MainActivity, scannerImpl)
                                val directoryStructure = scanner.scanLocal(
                                    database,
                                    scanPaths.split('\n'),
                                    excludedScanPaths.split('\n'),
                                    pathsOnly = true
                                ).value
                                scanner.quickSync(
                                    database, directoryStructure.toList(), scannerSensitivity,
                                    strictExtensions,
                                )

                                // start artist linking job
                                if (lookupYtmArtists && !scannerActive.value) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            scanner.localToRemoteArtist(database)
                                        } catch (e: ScannerAbortException) {
                                            Looper.prepare()
                                            Toast.makeText(
                                                this@MainActivity,
                                                "${this@MainActivity.getString(R.string.scanner_scan_fail)}: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            } catch (e: ScannerAbortException) {
                                Looper.prepare()
                                Toast.makeText(
                                    this@MainActivity,
                                    "${this@MainActivity.getString(R.string.scanner_scan_fail)}: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            } finally {
                                destroyScanner()
                            }

                            // post scan actions
                            clearDtCache()
                            imageCache.purgeCache()
                            playerConnection?.service?.initQueue()
                        } else if (perms == PackageManager.PERMISSION_DENIED) {
                            // Request the permission using the permission launcher
                            permissionLauncher.launch(MEDIA_PERMISSION_LEVEL)
                        }
                    }
                }
            }
            // Estado de autenticação melhorado com carregamento adequado
            val privacySecurityViewModel: PrivacySecurityViewModel = hiltViewModel()
            val privacySecurityState by privacySecurityViewModel.state.collectAsState()
            
            // Estados de controle - CORRIGIDO: isAuthenticated sempre inicia como false
            var isAuthenticated by remember { mutableStateOf(false) }
            var hasStateLoaded by remember { mutableStateOf(false) }
            var hasInitialized by remember { mutableStateOf(false) }
            
            // Verificar se o SplashActivity indicou que auth é necessária
            val requireAuth = intent?.getBooleanExtra("REQUIRE_AUTH", false) ?: false
            
            // IMPORTANTE: Verificar se splash enviou requisição de auth
            LaunchedEffect(Unit) {
                println("🔄 VERIFICANDO REQUISIÇÃO DE AUTH DO SPLASH: requireAuth=$requireAuth")
                android.util.Log.e("SonsPhere", "🔄 VERIFICANDO REQUISIÇÃO DE AUTH DO SPLASH: requireAuth=$requireAuth")
                
                if (requireAuth) {
                    // SplashActivity indicou que auth é necessária
                    isAuthenticated = false
                    hasInitialized = true
                    hasStateLoaded = true
                    println("🔒 SPLASH REQUISITOU AUTH - DIRECIONANDO PARA AUTENTICAÇÃO")
                    android.util.Log.e("SonsPhere", "🔒 SPLASH REQUISITOU AUTH - DIRECIONANDO PARA AUTENTICAÇÃO")
                } else {
                    // SplashActivity indicou que não precisa de auth
                    isAuthenticated = true
                    hasInitialized = true
                    hasStateLoaded = true
                    println("🔓 SPLASH LIBEROU ACESSO - DIRECIONANDO PARA APP PRINCIPAL")
                    android.util.Log.e("SonsPhere", "🔓 SPLASH LIBEROU ACESSO - DIRECIONANDO PARA APP PRINCIPAL")
                }
            }
            
            // Aguardar o estado carregar completamente (apenas se não foi inicializado pelo splash)
            LaunchedEffect(privacySecurityState) {
                println("📊 ESTADO RECEBIDO: biometricLock=${privacySecurityState.biometricLock}, hasInitialized=$hasInitialized, requireAuth=$requireAuth")
                android.util.Log.e("SonsPhere", "📊 ESTADO RECEBIDO: biometricLock=${privacySecurityState.biometricLock}, hasInitialized=$hasInitialized, requireAuth=$requireAuth")
                
                // Se o splash já inicializou o estado, não fazer nada aqui
                if (hasInitialized) {
                    println("⏭️ SPLASH JÁ INICIALIZOU - IGNORANDO ESTADO CARREGADO")
                    android.util.Log.e("SonsPhere", "⏭️ SPLASH JÁ INICIALIZOU - IGNORANDO ESTADO CARREGADO")
                    return@LaunchedEffect
                }
                
                // Fallback: se não veio do splash, usar lógica antiga
                hasStateLoaded = true
                
                if (privacySecurityState.biometricLock) {
                    // Biometria ATIVADA = usuário PRECISA se autenticar
                    isAuthenticated = false
                    println("✅ FALLBACK COM BIOMETRIA: biometricLock=true, isAuthenticated=false - REQUER AUTH")
                    android.util.Log.e("SonsPhere", "✅ FALLBACK COM BIOMETRIA: biometricLock=true, isAuthenticated=false - REQUER AUTH")
                } else {
                    // Biometria DESATIVADA = acesso direto
                    isAuthenticated = true
                    println("✅ FALLBACK SEM BIOMETRIA: biometricLock=false, isAuthenticated=true - ACESSO LIVRE")
                    android.util.Log.e("SonsPhere", "✅ FALLBACK SEM BIOMETRIA: biometricLock=false, isAuthenticated=true - ACESSO LIVRE")
                }
                hasInitialized = true
            }
            
            // REMOVIDO: LaunchedEffect que reagia incorretamente a mudanças na biometria
            // A autenticação para desabilitar já é tratada na própria tela de configurações
            // Quando usuário ATIVA biometria, deve continuar no app normalmente
            // Quando usuário DESATIVA biometria, a tela de configurações já cuida da autenticação
            
            OuterTuneTheme(
                darkTheme = useDarkTheme,
                pureBlack = pureBlack,
                themeColor = themeColor
            ) {
                // DEBUG: Log do estado atual antes de decidir qual tela mostrar
                LaunchedEffect(hasStateLoaded, isAuthenticated, requireAuth) {
                    val telaEscolhida = when {
                        !hasStateLoaded -> "CARREGAMENTO"
                        !isAuthenticated -> "TELA DE AUTH"
                        else -> "APP PRINCIPAL"
                    }
                    println("🎯 DECISÃO DE TELA: hasStateLoaded=$hasStateLoaded, isAuthenticated=$isAuthenticated, requireAuth=$requireAuth, Vai mostrar=$telaEscolhida")
                    android.util.Log.e("SonsPhere", "🎯 DECISÃO DE TELA: hasStateLoaded=$hasStateLoaded, isAuthenticated=$isAuthenticated, requireAuth=$requireAuth, Vai mostrar=$telaEscolhida")
                }
                
                // Aguardar estado carregar antes de decidir o que mostrar
                if (!hasStateLoaded) {
                    println("🔄 MOSTRANDO TELA DE CARREGAMENTO")
                    android.util.Log.e("SonsPhere", "🔄 MOSTRANDO TELA DE CARREGAMENTO")
                    // Tela de carregamento simples
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (!isAuthenticated) {
                    println("🔒 MOSTRANDO TELA DE AUTENTICAÇÃO")
                    android.util.Log.e("SonsPhere", "🔒 MOSTRANDO TELA DE AUTENTICAÇÃO")
                    
                    // Mostrar tela de autenticação biométrica
                    AuthScreen(
                        onAuthenticated = { 
                            println("✅ AUTENTICAÇÃO REALIZADA COM SUCESSO")
                            android.util.Log.e("SonsPhere", "✅ AUTENTICAÇÃO REALIZADA COM SUCESSO")
                            isAuthenticated = true 
                        },
                        onBackPressed = { 
                            println("🚪 USUÁRIO SAIU DA AUTENTICAÇÃO")
                            android.util.Log.e("SonsPhere", "🚪 USUÁRIO SAIU DA AUTENTICAÇÃO")
                            finish() 
                        },
                        audioFeedbackManager = audioFeedbackManager
                    )
                } else {
                    println("🏠 MOSTRANDO APP PRINCIPAL")
                    android.util.Log.e("SonsPhere", "🏠 MOSTRANDO APP PRINCIPAL")
                    
                    // Verificar se deve mostrar o diálogo de permissão de notificação
                    LaunchedEffect(firstSetupPassed, notificationPermissionAsked) {
                        if (firstSetupPassed && !notificationPermissionAsked) {
                            // Primeira execução completa - solicitar permissão de notificação
                            showNotificationDialog = true
                        }
                    }
                    
                    // Mostrar app principal
                    BoxWithConstraints( // Deprecated. please use the scaffold
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                    val focusManager = LocalFocusManager.current
                    val density = LocalDensity.current
                    val windowsInsets = WindowInsets.systemBars
                    val bottomInset = with(density) { windowsInsets.getBottom(density).toDp() }

                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val inSelectMode =
                        navBackStackEntry?.savedStateHandle?.getStateFlow("inSelectMode", false)?.collectAsState()
                    val (previousTab, setPreviousTab) = rememberSaveable { mutableStateOf("home") }

                    val (slimNav) = rememberPreference(SlimNavBarKey, defaultValue = false)
                    val (enabledTabs) = rememberPreference(EnabledTabsKey, defaultValue = DEFAULT_ENABLED_TABS)
                    val navigationItems = Screens.getScreens(enabledTabs)
                    val (defaultOpenTab, onDefaultOpenTabChange) = rememberPreference(DefaultOpenTabKey, defaultValue = Screens.Home.route)
                    // reset to home if somehow this gets set to a disabled tab
                    if (Screens.getScreens(enabledTabs).none { it.route == defaultOpenTab }) {
                        onDefaultOpenTabChange("home")
                    }

                    val tabOpenedFromShortcut = remember {
                        // reroute to library page for new layout is handled in NavHost section
                        when (intent?.action) {
                            ACTION_SONGS -> if (navigationItems.contains(Screens.Songs)) Screens.Songs else Screens.Library
                            ACTION_ALBUMS -> if (navigationItems.contains(Screens.Albums)) Screens.Albums else Screens.Library
                            ACTION_PLAYLISTS -> if (navigationItems.contains(Screens.Playlists)) Screens.Playlists else Screens.Library
                            else -> null
                        }
                    }
                    // setup filters for new layout
                    if (tabOpenedFromShortcut != null && navigationItems.contains(Screens.Library)) {
                        var filter by rememberEnumPreference(LibraryFilterKey, LibraryFilter.ALL)
                        filter = when (intent?.action) {
                            ACTION_SONGS -> LibraryFilter.SONGS
                            ACTION_ALBUMS -> LibraryFilter.ALBUMS
                            ACTION_PLAYLISTS -> LibraryFilter.PLAYLISTS
                            ACTION_SEARCH -> filter // do change filter for search
                            else -> LibraryFilter.ALL
                        }
                    }

                    val coroutineScope = rememberCoroutineScope()
                    var sharedSong: SongItem? by remember {
                        mutableStateOf(null)
                    }

                    /**
                     * Directly navigate to a YouTube page given an YouTube url
                     */
                    fun youtubeNavigator(uri: Uri): Boolean {
                        when (val path = uri.pathSegments.firstOrNull()) {
                            "playlist" -> uri.getQueryParameter("list")?.let { playlistId ->
                                if (playlistId.startsWith("OLAK5uy_")) {
                                    coroutineScope.launch {
                                        YouTube.albumSongs(playlistId).onSuccess { songs ->
                                            songs.firstOrNull()?.album?.id?.let { browseId ->
                                                navController.navigate("album/$browseId")
                                            }
                                        }.onFailure {
                                            reportException(it)
                                        }
                                    }
                                } else {
                                    navController.navigate("online_playlist/$playlistId")
                                }
                            }

                            "channel", "c" -> uri.lastPathSegment?.let { artistId ->
                                navController.navigate("artist/$artistId")
                            }

                            else -> when {
                                path == "watch" -> uri.getQueryParameter("v")
                                uri.host == "youtu.be" -> path
                                else -> return false
                            }?.let { videoId ->
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        YouTube.queue(listOf(videoId))
                                    }.onSuccess {
                                        sharedSong = it.firstOrNull()
                                    }.onFailure {
                                        reportException(it)
                                    }
                                }
                            }
                        }

                        return true
                    }


                    val (query, onQueryChange) = rememberSaveable(stateSaver = TextFieldValue.Saver) {
                        mutableStateOf(TextFieldValue())
                    }
                    var active by rememberSaveable {
                        mutableStateOf(false)
                    }
                    val onActiveChange: (Boolean) -> Unit = { newActive ->
                        active = newActive
                        if (!newActive) {
                            focusManager.clearFocus()
                            if (navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route }) {
                                onQueryChange(TextFieldValue())
                            }
                        }
                    }
                    var searchSource by rememberEnumPreference(SearchSourceKey, SearchSource.ONLINE)

                    val searchBarFocusRequester = remember { FocusRequester() }

                    val onSearch: (String) -> Unit = {
                        if (it.isNotEmpty()) {
                            onActiveChange(false)
                            if (youtubeNavigator(it.toUri())) {
                                // don't do anything
                            } else {
                                navController.navigate("search/${it.urlEncode()}")
                                if (dataStore[PauseSearchHistoryKey] != true) {
                                    database.query {
                                        insert(SearchHistory(query = it))
                                    }
                                }
                            }
                        }
                    }

                    var openSearchImmediately: Boolean by remember {
                        mutableStateOf(intent?.action == ACTION_SEARCH)
                    }

                    val shouldShowSearchBar = remember(active, navBackStackEntry, inSelectMode?.value) {
                        (active || navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route } ||
                                navBackStackEntry?.destination?.route?.startsWith("search/") == true)
                                && inSelectMode?.value != true
                    }
                    val shouldShowNavigationBar = remember(navBackStackEntry, active) {
                        navBackStackEntry?.destination?.route == null ||
                                navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route } && !active
                    }

                    fun getNavPadding(): Dp {
                        return if (shouldShowNavigationBar) {
                            if (slimNav) 52.dp else 68.dp
                        } else {
                            0.dp
                        }
                    }

                    // OTIMIZAÇÃO: Animação de altura simplificada
                    val navigationBarHeight by animateDpAsState(
                        targetValue = if (shouldShowNavigationBar) NavigationBarHeight else 0.dp,
                        animationSpec = tween(150), // Simplificado para melhor performance
                        label = ""
                    )

                    val playerBottomSheetState = rememberBottomSheetState(
                        dismissedBound = 0.dp,
                        collapsedBound = bottomInset + getNavPadding() + MiniPlayerHeight,
                        expandedBound = maxHeight,
                    )

                    val playerAwareWindowInsets =
                        remember(bottomInset, shouldShowNavigationBar, playerBottomSheetState.isDismissed, shouldShowSearchBar) {
                            var bottom = bottomInset
                            if (shouldShowNavigationBar) bottom += NavigationBarHeight
                            if (!playerBottomSheetState.isDismissed) bottom += MiniPlayerHeight
                            
                            // Usar altura da SearchBar quando visível, senão AppBarHeight padrão
                            val topHeight = if (shouldShowSearchBar) SearchBarHeight else AppBarHeight
                            
                            windowsInsets
                                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                                .add(WindowInsets(top = topHeight, bottom = bottom))
                        }

                    val scrollBehavior = appBarScrollBehavior(
                        canScroll = {
                            navBackStackEntry?.destination?.route?.startsWith("search/") == false &&
                                    (playerBottomSheetState.isCollapsed || playerBottomSheetState.isDismissed)
                        }
                    )

                    val searchBarScrollBehavior = appBarScrollBehavior(
                        state = rememberTopAppBarState(),
                        canScroll = {
                            navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route } &&
                                    (playerBottomSheetState.isCollapsed || playerBottomSheetState.isDismissed)
                        }
                    )

                    LaunchedEffect(navBackStackEntry) {
                        if (navBackStackEntry?.destination?.route?.startsWith("search/") == true) {
                            val searchQuery = withContext(Dispatchers.IO) {
                                navBackStackEntry?.arguments?.getString("query")!!
                            }
                            onQueryChange(TextFieldValue(searchQuery, TextRange(searchQuery.length)))
                        } else if (navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route }) {
                            onQueryChange(TextFieldValue())
                        }

                        if (navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route })
                            if (navigationItems.fastAny { it.route == previousTab })
                                searchBarScrollBehavior.state.resetHeightOffset()

                        navController.currentBackStackEntry?.destination?.route?.let {
                            setPreviousTab(it)
                        }

                        /*
                         * If the current back stack entry matches one of the main screens, but
                         * is not in the current navigation items, we need to remove the entry
                         * to avoid entering a "ghost" screen.
                         */
                        if (Screens.getScreens(enabledTabs).fastAny { it.route == navBackStackEntry?.destination?.route }) {
                            if (!navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route }) {
                                navController.popBackStack()
                                navController.navigate(Screens.Home.route)
                            }
                        }
                    }

                    LaunchedEffect(playerConnection) {
                        val player = playerConnection?.player ?: return@LaunchedEffect
                        if (player.currentMediaItem == null) {
                            if (!playerBottomSheetState.isDismissed) {
                                playerBottomSheetState.dismiss()
                            }
                        } else {
                            if (playerBottomSheetState.isDismissed) {
                                playerBottomSheetState.collapseSoft()
                            }
                        }
                    }

                    DisposableEffect(playerConnection, playerBottomSheetState) {
                        val player = playerConnection?.player ?: return@DisposableEffect onDispose { }
                        val listener = object : Player.Listener {
                            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED && mediaItem != null && playerBottomSheetState.isDismissed) {
                                    playerBottomSheetState.collapseSoft()
                                }
                            }
                        }
                        player.addListener(listener)
                        onDispose {
                            player.removeListener(listener)
                        }
                    }

                    DisposableEffect(Unit) {
                        val listener = Consumer<Intent> { intent ->
                            val uri =
                                intent.data ?: intent.extras?.getString(Intent.EXTRA_TEXT)?.toUri()
                                ?: return@Consumer
                            youtubeNavigator(uri)
                        }

                        addOnNewIntentListener(listener)
                        onDispose { removeOnNewIntentListener(listener) }
                    }

                    CompositionLocalProvider(
                        LocalDatabase provides database,
                        LocalContentColor provides contentColorFor(MaterialTheme.colorScheme.surface),
                        LocalPlayerConnection provides playerConnection,
                        LocalPlayerAwareWindowInsets provides playerAwareWindowInsets,
                        LocalDownloadUtil provides downloadUtil,
                        LocalShimmerTheme provides ShimmerTheme,
                        LocalSyncUtils provides syncUtils,
                        LocalNetworkConnected provides isNetworkConnected
                    ) {
                        Scaffold(
                            topBar = {
                                AnimatedVisibility(
                                    visible = shouldShowSearchBar,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    SearchBar(
                                        query = query,
                                        onQueryChange = onQueryChange,
                                        onSearch = onSearch,
                                        active = active,
                                        onActiveChange = onActiveChange,
                                        scrollBehavior = searchBarScrollBehavior,
                                        placeholder = {
                                            Text(
                                                text = stringResource(
                                                    if (!active) R.string.search
                                                    else when (searchSource) {
                                                        SearchSource.LOCAL -> R.string.search_library
                                                        SearchSource.ONLINE -> R.string.search_yt_music
                                                    }
                                                )
                                            )
                                        },
                                        leadingIcon = {
                                            IconButton(
                                                onClick = {
                                                    when {
                                                        active -> onActiveChange(false)

                                                        !active && navBackStackEntry?.destination?.route?.startsWith(
                                                            "search"
                                                        ) == true -> {
                                                            navController.navigateUp()
                                                        }

                                                        else -> onActiveChange(true)
                                                    }
                                                },
                                            ) {
                                                    if (active || navBackStackEntry?.destination?.route?.startsWith(
                                                            "search"
                                                        ) == true
                                                    ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                                        contentDescription = null
                                                    )
                                                    } else {
                                                    Icon(
                                                        painter = painterResource(R.drawable.ic_search_modern),
                                                    contentDescription = null
                                                )
                                                }
                                            }
                                        },
                                        trailingIcon = {
                                            if (active) {
                                                if (query.text.isNotEmpty()) {
                                                    IconButton(
                                                        onClick = { onQueryChange(TextFieldValue("")) }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Close,
                                                            contentDescription = null
                                                        )
                                                    }
                                                }
                                                IconButton(
                                                    onClick = {
                                                        searchSource =
                                                            if (searchSource == SearchSource.ONLINE) SearchSource.LOCAL else SearchSource.ONLINE
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = when (searchSource) {
                                                            SearchSource.LOCAL -> Icons.Rounded.LibraryMusic
                                                            SearchSource.ONLINE -> Icons.Rounded.Language
                                                        },
                                                        contentDescription = null
                                                    )
                                                }
                                            } else if (navBackStackEntry?.destination?.route in Screens.getAllScreens().map { it.route }) {
                                                IconButton(
                                                    onClick = {
                                                        navController.navigate("settings")
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Settings,
                                                        contentDescription = null
                                                    )
                                                }
                                            }
                                        },
                                        focusRequester = searchBarFocusRequester,
                                        modifier = Modifier.align(Alignment.TopCenter),
                                    ) {
                                        // OTIMIZAÇÃO: Crossfade simplificado para melhor performance
                                        Crossfade(
                                            targetState = searchSource,
                                            animationSpec = tween(100), // Reduzido drasticamente
                                            label = "",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(bottom = if (!playerBottomSheetState.isDismissed) MiniPlayerHeight else 0.dp)
                                                .navigationBarsPadding()
                                        ) { searchSource ->
                                            when (searchSource) {
                                                SearchSource.LOCAL -> LocalSearchScreen(
                                                    query = query.text,
                                                    navController = navController,
                                                    onDismiss = { onActiveChange(false) }
                                                )

                                                SearchSource.ONLINE -> OnlineSearchScreen(
                                                    query = query.text,
                                                    onQueryChange = onQueryChange,
                                                    navController = navController,
                                                    onSearch = {
                                                        if (youtubeNavigator(it.toUri())) {
                                                            return@OnlineSearchScreen
                                                        } else {
                                                            navController.navigate("search/${it.urlEncode()}")
                                                            if (dataStore[PauseSearchHistoryKey] != true) {
                                                                database.query {
                                                                    insert(SearchHistory(query = it))
                                                                }
                                                            }
                                                        }
                                                    },
                                                    onDismiss = { onActiveChange(false) }
                                                )
                                            }
                                        }
                                    }
                                }

                                if (BuildConfig.DEBUG) {
                                    val debugColour = Color.Red
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .offset(y = 100.dp)
                                    ) {
                                        Text(
                                            text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) | ${BuildConfig.FLAVOR}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = debugColour
                                        )
                                        Text(
                                            text = "${BuildConfig.APPLICATION_ID} | ${BuildConfig.BUILD_TYPE}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = debugColour
                                        )
                                        Text(
                                            text = "${Build.BRAND} ${Build.DEVICE} (${Build.MODEL})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = debugColour
                                        )
                                        Text(
                                            text = "${Build.VERSION.SDK_INT} (${Build.ID})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = debugColour
                                        )
                                    }
                                }
                            },
                            bottomBar = {
                                Box() {
                                    if (firstSetupPassed) {
                                        BottomSheetPlayer(
                                            state = playerBottomSheetState,
                                            navController = navController
                                        )
                                    }

                                    LaunchedEffect(playerBottomSheetState.isExpanded) {
                                        setSystemBarAppearance(
                                            (playerBottomSheetState.isExpanded
                                                    && playerBackground != PlayerBackgroundStyle.DEFAULT) || useDarkTheme
                                        )
                                    }
                                    NavigationBar(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .height(bottomInset + getNavPadding())
                                            // OTIMIZAÇÃO: Offset simplificado para melhor performance
                                            .offset {
                                                val yOffset = if (navigationBarHeight == 0.dp) {
                                                    (bottomInset + NavigationBarHeight).roundToPx()
                                                } else {
                                                    // Simplificado: apenas usar o progresso básico
                                                    val progress = playerBottomSheetState.progress.coerceIn(0f, 1f)
                                                    ((bottomInset + NavigationBarHeight) * progress).roundToPx()
                                                }
                                                IntOffset(x = 0, y = yOffset)
                                            }
                                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp))
                                    ) {
                                        navigationItems.fastForEach { screen ->
                                            NavigationBarItem(
                                                selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true,
                                                icon = {
                                                    Icon(
                                                        screen.icon,
                                                        contentDescription = null
                                                    )
                                                },
                                                label = {
                                                    if (!slimNav) {
                                                        Text(
                                                            text = stringResource(screen.titleId),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    if (navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true) {
                                                        navBackStackEntry?.savedStateHandle?.set(
                                                            "scrollToTop",
                                                            true
                                                        )
                                                        coroutineScope.launch {
                                                            searchBarScrollBehavior.state.resetHeightOffset()
                                                        }
                                                    } else {
                                                        navController.navigate(screen.route) {
                                                            popUpTo(navController.graph.startDestinationId) {
                                                                saveState = true
                                                            }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }

                                                    // OTIMIZAÇÃO: Haptic mais leve para melhor performance
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(searchBarScrollBehavior.nestedScrollConnection)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = (tabOpenedFromShortcut ?: Screens.getAllScreens()
                                    .find { it.route == defaultOpenTab })?.route
                                    ?: Screens.Home.route,
                                enterTransition = {
                                    fadeIn(animationSpec = tween(150))
                                },
                                exitTransition = {
                                    fadeOut(animationSpec = tween(150))
                                },
                                popEnterTransition = {
                                    fadeIn(animationSpec = tween(150))
                                },
                                popExitTransition = {
                                    fadeOut(animationSpec = tween(150))
                                },
                                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                            ) {
                                composable(Screens.Home.route) {
                                    HomeScreen(navController)
                                }
                                composable(Screens.Songs.route) {
                                    LibrarySongsScreen(navController)
                                }
                                composable(Screens.Folders.route) {
                                    LibraryFoldersScreen(navController, scrollBehavior)
                                }
                                composable(
                                    route = "${Screens.Folders.route}/{path}",
                                    arguments = listOf(
                                        navArgument("path") {
                                            type = NavType.StringType
                                        }
                                    )
                                ) {
                                    FolderScreen(navController, scrollBehavior)
                                }
                                composable(Screens.Artists.route) {
                                    LibraryArtistsScreen(navController)
                                }
                                composable(Screens.Albums.route) {
                                    LibraryAlbumsScreen(navController)
                                }
                                composable(Screens.Playlists.route) {
                                    LibraryPlaylistsScreen(navController)
                                }
                                composable(Screens.Library.route) {
                                    LibraryScreen(navController, scrollBehavior)
                                }
                                composable("history") {
                                    HistoryScreen(navController)
                                }
                                composable("stats") {
                                    StatsScreen(navController)
                                }
                                composable("mood_and_genres") {
                                    MoodAndGenresScreen(navController, scrollBehavior)
                                }
                                composable("account") {
                                    AccountScreen(navController, scrollBehavior)
                                }

                                composable(
                                    route = "browse/{browseId}",
                                    arguments = listOf(
                                        navArgument("browseId") {
                                            type = NavType.StringType
                                        }
                                    )
                                ) {
                                    BrowseScreen(
                                        navController,
                                        scrollBehavior,
                                        it.arguments?.getString("browseId")
                                    )
                                }
                                composable(
                                    route = "search/{query}",
                                    arguments = listOf(
                                        navArgument("query") {
                                            type = NavType.StringType
                                        }
                                    )
                                ) {
                                    OnlineSearchResult(navController)
                                }
                                composable(
                                    route = "album/{albumId}",
                                    arguments = listOf(
                                        navArgument("albumId") {
                                            type = NavType.StringType
                                        },
                                    )
                                ) {
                                    AlbumScreen(navController, scrollBehavior)
                                }
                                composable(
                                    route = "artist/{artistId}",
                                    arguments = listOf(
                                        navArgument("artistId") {
                                            type = NavType.StringType
                                        }
                                    )
                                ) {
                                    ArtistScreen(navController, scrollBehavior)
                                }
                                composable(
                                    route = "artist/{artistId}/songs",
                                    arguments = listOf(
                                        navArgument("artistId") {
                                            type = NavType.StringType
                                        }
                                    )
                                ) {
                                    ArtistSongsScreen(navController, scrollBehavior)
                                }
                                composable(
                                    route = "artist/{artistId}/albums",
                                    arguments = listOf(
                                        navArgument("artistId") {
                                            type = NavType.StringType
                                        }
                                    )
                                ) {
                                    ArtistAlbumsScreen(navController, scrollBehavior)
                                }
                                composable(
                                    route = "artist/{artistId}/items?browseId={browseId}?params={params}",
                                    arguments = listOf(
                                        navArgument("artistId") {
                                            type = NavType.StringType
                                        },
                                        navArgument("browseId") {
                                            type = NavType.StringType
                                            nullable = true
                                        },
                                        navArgument("params") {
                                            type = NavType.StringType
                                            nullable = true
                                        }
                                    )
                                ) {
                                    ArtistItemsScreen(navController, scrollBehavior)
                                }
                                composable(
                                    route = "online_playlist/{playlistId}",
                                    arguments = listOf(
                                        navArgument("playlistId") {
                                            type = NavType.StringType
                                        }
                                    )
                                ) {
                                    OnlinePlaylistScreen(navController, scrollBehavior)
                                }
                                composable(
                                    route = "local_playlist/{playlistId}",
                                    arguments = listOf(
                                        navArgument("playlistId") {
                                            type = NavType.StringType
                                        }
                                    )
                                ) {
                                    LocalPlaylistScreen(navController, scrollBehavior)
                                }
                                composable(
                                    route = "auto_playlist/{playlistId}",
                                    arguments = listOf(
                                        navArgument("playlistId") {
                                            type = NavType.StringType
                                        }
                                    )
                                ) {
                                    AutoPlaylistScreen(navController, scrollBehavior)
                                }
                                composable(
                                    route = "youtube_browse/{browseId}?params={params}",
                                    arguments = listOf(
                                        navArgument("browseId") {
                                            type = NavType.StringType
                                            nullable = true
                                        },
                                        navArgument("params") {
                                            type = NavType.StringType
                                            nullable = true
                                        }
                                    )
                                ) {
                                    YouTubeBrowseScreen(navController, scrollBehavior)
                                }
                                composable("settings") {
                                    SettingsScreen(navController, scrollBehavior)
                                }
                                composable("settings/appearance") {
                                    AppearanceSettings(navController, scrollBehavior)
                                }
                                composable("settings/interface") {
                                    InterfaceSettings(navController, scrollBehavior)
                                }
                                composable("settings/library") {
                                    LibrarySettings(navController, scrollBehavior)
                                }
                                composable("settings/library/lyrics") {
                                    LyricsSettings(navController, scrollBehavior)
                                }
                                composable("settings/account_sync") {
                                    AccountSyncSettings(navController, scrollBehavior)
                                }
                                composable("settings/player") {
                                    PlayerSettings(navController, scrollBehavior)
                                }
                                composable("settings/storage") {
                                    StorageSettings(navController, scrollBehavior)
                                }
                                composable("settings/backup_restore") {
                                    BackupAndRestore(navController, scrollBehavior)
                                }
                                composable("settings/privacy_security") {
                                    PrivacySecuritySettings(navController, scrollBehavior)
                                }
                                composable("settings/local") {
                                    LocalPlayerSettings(navController, scrollBehavior)
                                }
                                
                                composable("settings/about") {
                                    AboutScreen(navController, scrollBehavior)
                                }
                                composable("privacy_policy") {
                                    com.richard.musicplayer.ui.screens.settings.PrivacyPolicyScreen(navController, scrollBehavior)
                                }
                                composable("terms_of_use") {
                                    com.richard.musicplayer.ui.screens.settings.TermsOfUseScreen(navController, scrollBehavior)
                                }
                                composable("open_source_licenses") {
                                    com.richard.musicplayer.ui.screens.settings.OpenSourceLicensesScreen(navController, scrollBehavior)
                                }
                                composable("changelog") {
                                    com.richard.musicplayer.ui.screens.settings.ChangelogScreen(navController, scrollBehavior)
                                }
                                // Logs screen - Only available in debug builds
                                if (BuildConfig.DEBUG) {
                                    composable("logs") {
                                        com.richard.musicplayer.ui.screens.LogsScreen(
                                            onBackClick = { navController.navigateUp() }
                                        )
                                    }
                                }
                                composable("login") {
                                    LoginScreen(navController)
                                }



                                composable("settings/pin_change") {
                                    PinChangeScreen(
                                        navController = navController,
                                        viewModel = hiltViewModel<PrivacySecurityViewModel>()
                                    )
                                }
                            }
                        }

                        BottomSheetMenu(
                            state = LocalMenuState.current,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )



                        sharedSong?.let { song ->
                            playerConnection?.let {
                                Dialog(
                                    onDismissRequest = { sharedSong = null },
                                    properties = DialogProperties(usePlatformDefaultWidth = false)
                                ) {
                                    Surface(
                                        modifier = Modifier.padding(24.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        color = AlertDialogDefaults.containerColor,
                                        tonalElevation = AlertDialogDefaults.TonalElevation
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            YouTubeSongMenu(
                                                song = song,
                                                navController = navController,
                                                onDismiss = { sharedSong = null }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LaunchedEffect(shouldShowSearchBar, openSearchImmediately) {
                        if (shouldShowSearchBar && openSearchImmediately) {
                            onActiveChange(true)
                            searchBarFocusRequester.requestFocus()
                            openSearchImmediately = false
                        }
                    }
                }
                
                // Diálogo de permissão de notificação na primeira execução
                NotificationPermissionDialog(
                    isVisible = showNotificationDialog,
                    onPermissionResult = { granted ->
                        setNotificationPermissionAsked(true)
                        showNotificationDialog = false
                        if (granted) {
                            println("✅ PERMISSÃO DE NOTIFICAÇÃO CONCEDIDA")
                            android.util.Log.d("SonsPhere", "✅ PERMISSÃO DE NOTIFICAÇÃO CONCEDIDA")
                        } else {
                            println("❌ PERMISSÃO DE NOTIFICAÇÃO NEGADA")
                            android.util.Log.d("SonsPhere", "❌ PERMISSÃO DE NOTIFICAÇÃO NEGADA")
                        }
                    },
                    onDismiss = {
                        setNotificationPermissionAsked(true)
                        showNotificationDialog = false
                    }
                )
                } // Fechamento do bloco if-else de autenticação
            }
        }
    }

    private fun setSystemBarAppearance(isDark: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView.rootView).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }

    /**
     * 🚀 SISTEMA DE TAXA MÁXIMA DE ATUALIZAÇÃO
     * Força o app a usar sempre a maior taxa de atualização disponível no dispositivo
     * Suporta: 60Hz, 90Hz, 120Hz, 144Hz, 165Hz+
     */
    private fun setupMaxRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ (API 30+) - Método moderno
                val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    display
                } else {
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay
                }
                
                display?.let { d ->
                    val supportedModes = d.supportedModes
                    
                    // Encontrar o modo com maior refresh rate
                    val maxRefreshRateMode = supportedModes.maxByOrNull { mode ->
                        mode.refreshRate
                    }
                    
                    maxRefreshRateMode?.let { mode ->
                        val layoutParams = window.attributes
                        layoutParams.preferredDisplayModeId = mode.modeId
                        window.attributes = layoutParams
                        
                        // Log para debug
                        android.util.Log.d("RefreshRate", 
                            "🚀 Taxa máxima configurada: ${mode.refreshRate}Hz " +
                            "(${mode.physicalWidth}x${mode.physicalHeight})")
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6+ (API 23+) - Método legado
                @Suppress("DEPRECATION")
                val display = windowManager.defaultDisplay
                val supportedRefreshRates = display.supportedRefreshRates
                
                val maxRefreshRate = supportedRefreshRates.maxOrNull()
                maxRefreshRate?.let { rate ->
                    val layoutParams = window.attributes
                    layoutParams.preferredRefreshRate = rate
                    window.attributes = layoutParams
                    
                    android.util.Log.d("RefreshRate", 
                        "🚀 Taxa máxima configurada (legado): ${rate}Hz")
                }
            }
            
            // Configurações adicionais de performance para alta taxa de atualização
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            
            // Habilitar hardware acceleration se não estiver habilitado
            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
            
        } catch (e: Exception) {
            android.util.Log.w("RefreshRate", "⚠️ Erro ao configurar taxa máxima: ${e.message}")
        }
    }

    companion object {
        const val ACTION_SEARCH = "com.richard.musicplayer.action.SEARCH"
        const val ACTION_SONGS = "com.richard.musicplayer.action.SONGS"
        const val ACTION_ALBUMS = "com.richard.musicplayer.action.ALBUMS"
        const val ACTION_PLAYLISTS = "com.richard.musicplayer.action.PLAYLISTS"
    }
}

val LocalDatabase = staticCompositionLocalOf<MusicDatabase> { error("No database provided") }
val LocalPlayerConnection = staticCompositionLocalOf<PlayerConnection?> { error("No PlayerConnection provided") }
val LocalPlayerAwareWindowInsets = compositionLocalOf<WindowInsets> { error("No WindowInsets provided") }
val LocalDownloadUtil = staticCompositionLocalOf<DownloadUtil> { error("No DownloadUtil provided") }
val LocalSyncUtils = staticCompositionLocalOf<SyncUtils> { error("No SyncUtils provided") }
val LocalNetworkConnected = staticCompositionLocalOf<Boolean> { error("No Network Status provided") }
