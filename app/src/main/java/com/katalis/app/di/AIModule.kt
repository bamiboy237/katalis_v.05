package com.katalis.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.katalis.app.domain.engine.Gemma3nEngine
import com.katalis.app.domain.service.DeviceCapabilityService
import com.katalis.app.domain.service.EducationalRAGService
import com.katalis.app.domain.service.ModelManagerService
import com.katalis.app.domain.service.PerformanceMonitoringService
import com.katalis.app.domain.service.PerformancePreferencesService
import com.katalis.app.domain.repository.KnowledgeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideModelManagerService(
        @ApplicationContext context: Context
    ): ModelManagerService {
        return ModelManagerService(context)
    }

    @Provides
    @Singleton
    fun provideDeviceCapabilityService(
        @ApplicationContext context: Context
    ): DeviceCapabilityService {
        return DeviceCapabilityService(context)
    }

    @Provides
    @Singleton
    fun providePerformancePreferencesService(
        dataStore: DataStore<Preferences>
    ): PerformancePreferencesService {
        return PerformancePreferencesService(dataStore)
    }

    @Provides
    @Singleton
    fun providePerformanceMonitoringService(
        dataStore: DataStore<Preferences>
    ): PerformanceMonitoringService {
        return PerformanceMonitoringService(dataStore)
    }

    @Provides
    @Singleton
    fun provideGemma3nEngine(
        @ApplicationContext context: Context,
        modelManager: ModelManagerService,
        deviceCapabilityService: DeviceCapabilityService,
        performancePreferencesService: PerformancePreferencesService
    ): Gemma3nEngine {
        return Gemma3nEngine(
            context,
            modelManager,
            deviceCapabilityService,
            performancePreferencesService
        )
    }

    @Provides
    @Singleton
    fun provideEducationalRAGService(
        gemma3nEngine: Gemma3nEngine,
        knowledgeRepository: KnowledgeRepository
    ): EducationalRAGService {
        return EducationalRAGService(gemma3nEngine, knowledgeRepository)
    }
}