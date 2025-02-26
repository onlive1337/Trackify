package com.onlive.trackify.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_groups")
data class CategoryGroup(
    @PrimaryKey(autoGenerate = true)
    val groupId: Long = 0,
    val name: String,
    val description: String? = null,
    val colorCode: String
)