package com.katalis.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.katalis.app.data.local.KatalisDatabase
import com.katalis.app.data.local.dao.KnowledgeArticleDao
import com.katalis.app.data.local.dao.CategoryDao
import com.katalis.app.data.repository.KnowledgeRepositoryImpl
import com.katalis.app.data.repository.UserProfileRepositoryImpl
import com.katalis.app.domain.repository.KnowledgeRepository
import com.katalis.app.domain.repository.UserProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "katalis_preferences")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideKatalisDatabase(
        @ApplicationContext context: Context
    ): KatalisDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            KatalisDatabase::class.java,
            KatalisDatabase.DATABASE_NAME
        )
            .createFromAsset("database/${KatalisDatabase.ASSET_DATABASE_NAME}")
            .fallbackToDestructiveMigration(true) // For development only
            .build()
    }

    @Provides
    fun provideKnowledgeArticleDao(database: KatalisDatabase): KnowledgeArticleDao {
        return database.knowledgeArticleDao()
    }

    @Provides
    fun provideCategoryDao(database: KatalisDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideKnowledgeRepository(
        knowledgeArticleDao: KnowledgeArticleDao
    ): KnowledgeRepository {
        return KnowledgeRepositoryImpl(knowledgeArticleDao)
    }

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        @ApplicationContext context: Context
    ): UserProfileRepository {
        return UserProfileRepositoryImpl(context)
    }
}