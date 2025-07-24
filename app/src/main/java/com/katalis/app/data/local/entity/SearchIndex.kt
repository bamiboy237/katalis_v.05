package com.katalis.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "search_index",
    foreignKeys = [
        ForeignKey(
            entity = KnowledgeArticle::class,
            parentColumns = ["id"],
            childColumns = ["article_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["article_id"])]
)
data class SearchIndex(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "article_id")
    val articleId: String,

    @ColumnInfo(name = "searchable_text")
    val searchableText: String,

    val weight: Float = 1.0f // For relevance scoring
)