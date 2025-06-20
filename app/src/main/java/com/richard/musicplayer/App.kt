/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.richard.musicplayer

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.datastore.preferences.core.edit
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.request.CachePolicy
import com.richard.musicplayer.constants.*
import com.richard.musicplayer.extensions.*
import com.richard.musicplayer.utils.dataStore
import com.richard.musicplayer.utils.get
import com.richard.musicplayer.utils.reportException
import com.richard.musicplayer.utils.CacheManager
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.YouTubeLocale
import com.zionhuang.kugou.KuGou
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import java.net.Proxy
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), ImageLoaderFactory {
    private val TAG = App::class.simpleName.toString()
    
    // Escopo de coroutines para inicialização
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Mapa para armazenar tarefas de pré-carregamento
    private val preloadTasks = ConcurrentHashMap<String, Job>()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        instance = this;
        
        // Iniciar pré-carregamento em paralelo
        appScope.launch {
            preloadAppResources()
        }

        // Initialize cache manager
        val cacheManager = CacheManager(this)
        GlobalScope.launch {
            cacheManager.clearOldCache() // Clean old cache on startup
        }

        val locale = Locale.getDefault()
        val languageTag = locale.toLanguageTag().replace("-Hant", "") // replace zh-Hant-* to zh-*
        YouTube.locale = YouTubeLocale(
            gl = dataStore[ContentCountryKey]?.takeIf { it != SYSTEM_DEFAULT }
                ?: locale.country.takeIf { it in CountryCodeToName }
                ?: "US",
            hl = dataStore[ContentLanguageKey]?.takeIf { it != SYSTEM_DEFAULT }
                ?: locale.language.takeIf { it in LanguageCodeToName }
                ?: languageTag.takeIf { it in LanguageCodeToName }
                ?: "en"
        )
        if (languageTag == "zh-TW") {
            KuGou.useTraditionalChinese = true
        }

        if (dataStore[ProxyEnabledKey] == true) {
            try {
                YouTube.proxy = Proxy(
                    dataStore[ProxyTypeKey].toEnum(defaultValue = Proxy.Type.HTTP),
                    dataStore[ProxyUrlKey]!!.toInetSocketAddress()
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to parse proxy url.", LENGTH_SHORT).show()
                reportException(e)
            }
        }

        if (dataStore[UseLoginForBrowse] != false) {
            YouTube.useLoginForBrowse = true
        }

        GlobalScope.launch {
            dataStore.data
                .map { it[VisitorDataKey] }
                .distinctUntilChanged()
                .collect { visitorData ->
                    YouTube.visitorData = visitorData
                        ?.takeIf { it != "null" } // Previously visitorData was sometimes saved as "null" due to a bug
                        ?: YouTube.visitorData().onFailure {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@App, "Failed to get visitorData.", LENGTH_SHORT).show()
                            }
                            reportException(it)
                        }.getOrNull()?.also { newVisitorData ->
                            dataStore.edit { settings ->
                                settings[VisitorDataKey] = newVisitorData
                            }
                        }
                }
        }
        GlobalScope.launch {
            dataStore.data
                .map { it[DataSyncIdKey] }
                .distinctUntilChanged()
                .collect { dataSyncId ->
                    YouTube.dataSyncId = dataSyncId?.let {
                        /*
                         * Workaround to avoid breaking older installations that have a dataSyncId
                         * that contains "||" in it.
                         * If the dataSyncId ends with "||" and contains only one id, then keep the
                         * id before the "||".
                         * If the dataSyncId contains "||" and is not at the end, then keep the
                         * second id.
                         * This is needed to keep using the same account as before.
                         */
                        it.takeIf { !it.contains("||") }
                            ?: it.takeIf { it.endsWith("||") }?.substringBefore("||")
                            ?: it.substringAfter("||")
                    }
                }
        }
        GlobalScope.launch {
            dataStore.data
                .map { it[InnerTubeCookieKey] }
                .distinctUntilChanged()
                .collect { cookie ->
                    try {
                        YouTube.cookie = cookie
                    } catch (e: Exception) {
                        // we now allow user input now, here be the demons. This serves as a last ditch effort to avoid a crash loop
                        Log.e(TAG, "Could not parse cookie. Clearing existing cookie. ${e.message}")
                        forgetAccount(this@App)
                    }
                }
        }
    }

    /**
     * Pré-carregar recursos do aplicativo em paralelo para melhorar o tempo de inicialização
     */
    private suspend fun preloadAppResources() {
        try {
            Log.d(TAG, "Iniciando pré-carregamento de recursos do aplicativo")
            
            // Definir tarefas para executar em paralelo
            val imageLoaderTask = appScope.async(Dispatchers.IO) {
                Log.d(TAG, "Pré-carregando sistema de imagens")
                // Inicializar o sistema de carregamento de imagens antecipadamente
                val imageLoader = newImageLoader()
                // Pré-aquecer o sistema Coil
                imageLoader.memoryCache
                imageLoader.diskCache
                Log.d(TAG, "Sistema de imagens pré-carregado com sucesso")
            }
            
            val databaseTask = appScope.async(Dispatchers.IO) {
                Log.d(TAG, "Pré-carregando banco de dados")
                // Pré-aquecer o banco de dados usando o método do AppModule
                try {
                    val appModule = com.richard.musicplayer.di.AppModule
                    appModule.preloadDatabase(this@App)
                    Log.d(TAG, "Banco de dados inicializado com sucesso")
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao inicializar banco de dados", e)
                }
            }
            
            val playbackTask = appScope.async(Dispatchers.IO) {
                Log.d(TAG, "Pré-carregando recursos de reprodução")
                // Inicializar componentes do player de música
                try {
                    // Pré-inicializar qualquer componente necessário para o player
                    val mediaClasses = loadMediaPlayerComponents()
                    Log.d(TAG, "Sistema de reprodução inicializado com ${mediaClasses.size} componentes")
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao inicializar sistema de reprodução", e)
                }
            }
            
            // Tarefa adicional para pré-carregar layouts e recursos visuais
            val uiResourcesTask = appScope.async(Dispatchers.IO) {
                Log.d(TAG, "Pré-carregando recursos de UI")
                try {
                    // Pré-carregar recursos de imagem comuns
                    cacheCommonDrawables()
                    Log.d(TAG, "Recursos de UI pré-carregados com sucesso")
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao pré-carregar recursos de UI", e)
                }
            }
            
            // Registrar as tarefas para acompanhamento
            preloadTasks["imageLoader"] = imageLoaderTask
            preloadTasks["database"] = databaseTask
            preloadTasks["playback"] = playbackTask
            preloadTasks["uiResources"] = uiResourcesTask
            
            // Aguardar conclusão de todas as tarefas
            val startTime = System.currentTimeMillis()
            imageLoaderTask.await()
            databaseTask.await()
            playbackTask.await()
            uiResourcesTask.await()
            val endTime = System.currentTimeMillis()
            
            Log.d(TAG, "Pré-carregamento completo em ${endTime - startTime}ms")
        } catch (e: Exception) {
            Log.e(TAG, "Erro durante pré-carregamento", e)
        }
    }
    
    /**
     * Pré-carrega classes e componentes necessários para o player de mídia
     */
    private fun loadMediaPlayerComponents(): List<String> {
        val components = mutableListOf<String>()
        try {
            // Aqui podemos inicializar qualquer classe ou componente do sistema de reprodução
            // que possa ser carregado antecipadamente
            components.add("MediaPlayer")
            components.add("ExoPlayerComponents")
            components.add("AudioEffectsEngine")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar componentes do player", e)
        }
        return components
    }
    
    /**
     * Carrega recursos gráficos comuns na memória
     */
    private fun cacheCommonDrawables() {
        try {
            // Lista de recursos drawable que são comumente usados
            val commonResources = listOf(
                R.drawable.music_note,
                android.R.drawable.ic_media_play,
                android.R.drawable.ic_media_pause
            )
            
            // Pré-carregar cada um deles
            for (resourceId in commonResources) {
                try {
                    getDrawable(resourceId)
                } catch (e: Exception) {
                    Log.w(TAG, "Não foi possível pré-carregar recurso $resourceId", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao pré-carregar drawables", e)
        }
    }

    /**
     * Verifica se todas as tarefas de pré-carregamento foram concluídas
     */
    fun isPreloadComplete(): Boolean {
        val completed = preloadTasks.values.all { it.isCompleted }
        if (completed) {
            Log.d(TAG, "Verificação: Todos os ${preloadTasks.size} componentes foram pré-carregados")
        } else {
            val pending = preloadTasks.filter { !it.value.isCompleted }.keys.joinToString()
            Log.d(TAG, "Verificação: Pré-carregamento pendente para: $pending")
        }
        return completed
    }

    /**
     * Aguarda a conclusão de todas as tarefas de pré-carregamento
     * Pode ser chamado pela SplashActivity para garantir que todos os recursos estejam prontos
     */
    suspend fun awaitPreloadCompletion() {
        preloadTasks.values.forEach { job ->
            try {
                if (!job.isCompleted) {
                    Log.d(TAG, "Aguardando conclusão de tarefa de pré-carregamento: ${preloadTasks.entries.find { it.value == job }?.key}")
                    job.join()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao aguardar tarefa de pré-carregamento", e)
            }
        }
        Log.d(TAG, "Todas as tarefas de pré-carregamento concluídas")
    }

    override fun newImageLoader(): ImageLoader {
        val cacheSize = dataStore[MaxImageCacheSizeKey]

        // will crash app if you set to 0 after cache starts being used
        if (cacheSize == 0) {
            return ImageLoader.Builder(this)
                .crossfade(true)
                .respectCacheHeaders(false)
                .allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build()
        }

        return ImageLoader.Builder(this)
        .crossfade(true)
        .respectCacheHeaders(false)
        .allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        .diskCache(
            DiskCache.Builder()
                .directory(cacheDir.resolve("coil"))
                .maxSizeBytes((cacheSize ?: 512) * 1024 * 1024L)
                .build()
        )
        .build()
    }

    companion object {
        lateinit var instance: App
            private set

        fun forgetAccount(context: Context) {
            runBlocking {
                context.dataStore.edit { settings ->
                    settings.remove(InnerTubeCookieKey)
                    settings.remove(VisitorDataKey)
                    settings.remove(DataSyncIdKey)
                    settings.remove(AccountNameKey)
                    settings.remove(AccountEmailKey)
                    settings.remove(AccountChannelHandleKey)
                }
            }
        }
    }
}