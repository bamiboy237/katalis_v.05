package com.katalis.app.domain.repository

import com.katalis.app.data.local.entity.KnowledgeArticle
import kotlinx.coroutines.flow.Flow

interface KnowledgeRepository {

    suspend fun getArticleById(id: String): KnowledgeArticle?

    fun getArticlesByCategory(category: String): Flow<List<KnowledgeArticle>>

    fun getFeaturedArticles(limit: Int = 10): Flow<List<KnowledgeArticle>>

    suspend fun searchArticles(query: String, limit: Int = 50): List<KnowledgeArticle>

    fun getRecentArticles(limit: Int = 20): Flow<List<KnowledgeArticle>>

    suspend fun getAllCategories(): List<String>

    suspend fun getArticleCountByCategory(category: String): Int
}