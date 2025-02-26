package com.onlive.trackify.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithGroup(
    @Embedded val category: Category,
    @Relation(
        parentColumn = "groupId",
        entityColumn = "groupId"
    )
    val group: CategoryGroup?
)