package com.richard.musicplayer.di

import android.content.Context
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.richard.musicplayer.constants.MaxSongCacheSizeKey
import com.richard.musicplayer.db.InternalDatabase
import com.richard.musicplayer.db.MusicDatabase
import com.richard.musicplayer.db.PrivacySecurityRepository
import com.richard.musicplayer.utils.AudioFeedbackManager
import com.richard.musicplayer.utils.LmImageCacheMgr
import com.richard.musicplayer.utils.dataStore
import com.richard.musicplayer.utils.get
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ImageCache

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DownloadCache

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Database instance holder - melhor controle sobre inicialização
    private var databaseInstance: MusicDatabase? = null

    /**
     * Fornece a instância do banco de dados.
     * Esta implementação agora suporta inicialização preguiçosa e pré-carregamento
     */
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): MusicDatabase {
        // Se já temos uma instância, retorná-la diretamente
        databaseInstance?.let { return it }
        
        // Caso contrário, criar nova instância
        return synchronized(this) {
            // Verificar novamente dentro do bloco sincronizado
            databaseInstance?.let { return it }
            
            // Criar nova instância
            val newInstance = InternalDatabase.newInstance(context)
            databaseInstance = newInstance
            return newInstance
        }
    }

    /**
     * Inicializa o banco de dados em segundo plano.
     * Esta função pode ser chamada durante o pré-carregamento
     */
    suspend fun preloadDatabase(@ApplicationContext context: Context) {
        if (databaseInstance != null) return
        
        withContext(Dispatchers.IO) {
            provideDatabase(context)
        }
    }

    @Provides
    @Singleton
    @ImageCache
    fun provideImageCache(): LmImageCacheMgr = LmImageCacheMgr()

    @Singleton
    @Provides
    fun provideDatabaseProvider(@ApplicationContext context: Context): DatabaseProvider =
        StandaloneDatabaseProvider(context)

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class PlayerCache

    @Singleton
    @Provides
    @PlayerCache
    fun providePlayerCache(@ApplicationContext context: Context, databaseProvider: DatabaseProvider): SimpleCache {
        val constructor = {
            SimpleCache(
                context.filesDir.resolve("exoplayer"),
                when (val cacheSize = context.dataStore[MaxSongCacheSizeKey] ?: 512) {
                    -1 -> NoOpCacheEvictor()
                    else -> LeastRecentlyUsedCacheEvictor(cacheSize * 1024 * 1024L)
                },
                databaseProvider
            )
        }
        constructor().release()
        return constructor()
    }

    @Singleton
    @Provides
    @DownloadCache
    fun provideDownloadCache(@ApplicationContext context: Context, databaseProvider: DatabaseProvider): SimpleCache {
        val constructor = {
            SimpleCache(context.filesDir.resolve("download"), NoOpCacheEvictor(), databaseProvider)
        }
        constructor().release()
        return constructor()
    }

    @Singleton
    @Provides
    fun provideAudioFeedbackManager(
        @ApplicationContext context: Context,
        privacySecurityRepository: PrivacySecurityRepository
    ): AudioFeedbackManager {
        val manager = AudioFeedbackManager(context, privacySecurityRepository)
        manager.initialize()
        return manager
    }
}
