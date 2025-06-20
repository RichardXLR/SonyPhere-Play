package com.richard.musicplayer.di

import android.content.Context
import com.richard.musicplayer.db.PrivacySecurityRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PrivacySecurityModule {
    
    @Provides
    @Singleton
    fun providePrivacySecurityRepository(
        @ApplicationContext context: Context
    ): PrivacySecurityRepository {
        return PrivacySecurityRepository(context)
    }
} 