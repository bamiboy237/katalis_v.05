package com.katalis.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String,

    val name: String,

    val description: String?,

    @ColumnInfo(name = "parent_category_id")
    val parentCategoryId: String?,

    @ColumnInfo(name = "display_order")
    val displayOrder: Int = 0,

    @ColumnInfo(name = "icon_name")
    val iconName: String?,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)