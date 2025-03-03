package com.onlive.trackify.data.database

import android.content.Context

class DatabaseInitializer(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    fun initializeCategories() {
    }
}