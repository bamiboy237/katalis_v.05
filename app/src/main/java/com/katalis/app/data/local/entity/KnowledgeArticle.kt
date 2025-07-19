package com.katalis.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "knowledge_articles")
data class KnowledgeArticle(
    @PrimaryKey
    val id: String,

    val title: String,

    val content: String,

    val category: String,

    val subcategory: String?,

    @ColumnInfo(name = "created_date")
    val createdDate: String,

    @ColumnInfo(name = "updated_date")
    val updatedDate: String,

    val tags: String, // Comma-separated tags

    val priority: Int = 0,

    @ColumnInfo(name = "is_featured")
    val isFeatured: Boolean = false,

    val keywords: String? // For search optimization
)