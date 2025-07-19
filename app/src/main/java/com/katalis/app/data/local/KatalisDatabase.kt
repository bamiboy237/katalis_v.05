package com.katalis.app.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.katalis.app.data.local.dao.KnowledgeArticleDao
import com.katalis.app.data.local.dao.CategoryDao
import com.katalis.app.data.local.entity.KnowledgeArticle
import com.katalis.app.data.local.entity.Category
import com.katalis.app.data.local.entity.SearchIndex

@Database(
    entities = [
        KnowledgeArticle::class,
        Category::class,
        SearchIndex::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KatalisDatabase : RoomDatabase() {

    abstract fun knowledgeArticleDao(): KnowledgeArticleDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "katalis_db"
        const val ASSET_DATABASE_NAME = "GCE_PMM.db"

        @Volatile
        private var INSTANCE: KatalisDatabase? = null

        fun getInstance(context: Context): KatalisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KatalisDatabase::class.java,
                    DATABASE_NAME
                )
                    .createFromAsset("database/$ASSET_DATABASE_NAME")
                    .fallbackToDestructiveMigration() // For development only
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}