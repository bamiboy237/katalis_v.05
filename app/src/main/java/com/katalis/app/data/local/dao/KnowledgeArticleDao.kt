package com.katalis.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.katalis.app.data.local.entity.KnowledgeArticle
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeArticleDao {

    @Query("SELECT * FROM knowledge_articles WHERE id = :id")
    suspend fun getArticleById(id: String): KnowledgeArticle?

    @Query("SELECT * FROM knowledge_articles WHERE category = :category ORDER BY priority DESC, title ASC")
    fun getArticlesByCategory(category: String): Flow<List<KnowledgeArticle>>

    @Query("SELECT * FROM knowledge_articles WHERE is_featured = 1 ORDER BY priority DESC LIMIT :limit")
    fun getFeaturedArticles(limit: Int = 10): Flow<List<KnowledgeArticle>>

    @Query(
        """
        SELECT * FROM knowledge_articles 
        WHERE title LIKE '%' || :query || '%' 
           OR content LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
           OR keywords LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN title LIKE '%' || :query || '%' THEN 1
                WHEN tags LIKE '%' || :query || '%' THEN 2
                WHEN keywords LIKE '%' || :query || '%' THEN 3
                ELSE 4
            END,
            priority DESC
        LIMIT :limit
    """
    )
    suspend fun searchArticles(query: String, limit: Int = 50): List<KnowledgeArticle>

    @Query("SELECT * FROM knowledge_articles ORDER BY updated_date DESC LIMIT :limit")
    fun getRecentArticles(limit: Int = 20): Flow<List<KnowledgeArticle>>

    @Query("SELECT DISTINCT category FROM knowledge_articles ORDER BY category")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT COUNT(*) FROM knowledge_articles WHERE category = :category")
    suspend fun getArticleCountByCategory(category: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<KnowledgeArticle>)

    @Query("DELETE FROM knowledge_articles")
    suspend fun clearAllArticles()
}