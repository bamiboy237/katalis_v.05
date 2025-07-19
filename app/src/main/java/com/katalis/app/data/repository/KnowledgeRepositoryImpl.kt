package com.katalis.app.data.repository

import com.katalis.app.data.local.dao.KnowledgeArticleDao
import com.katalis.app.data.local.entity.KnowledgeArticle
import com.katalis.app.domain.repository.KnowledgeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KnowledgeRepositoryImpl @Inject constructor(
    private val knowledgeArticleDao: KnowledgeArticleDao
) : KnowledgeRepository {

    override suspend fun getArticleById(id: String): KnowledgeArticle? {
        return knowledgeArticleDao.getArticleById(id)
    }

    override fun getArticlesByCategory(category: String): Flow<List<KnowledgeArticle>> {
        return knowledgeArticleDao.getArticlesByCategory(category)
    }

    override fun getFeaturedArticles(limit: Int): Flow<List<KnowledgeArticle>> {
        return knowledgeArticleDao.getFeaturedArticles(limit)
    }

    override suspend fun searchArticles(query: String, limit: Int): List<KnowledgeArticle> {
        return knowledgeArticleDao.searchArticles(query, limit)
    }

    override fun getRecentArticles(limit: Int): Flow<List<KnowledgeArticle>> {
        return knowledgeArticleDao.getRecentArticles(limit)
    }

    override suspend fun getAllCategories(): List<String> {
        return knowledgeArticleDao.getAllCategories()
    }

    override suspend fun getArticleCountByCategory(category: String): Int {
        return knowledgeArticleDao.getArticleCountByCategory(category)
    }
}