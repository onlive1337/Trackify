package com.onlive.trackify.data.database

import android.content.Context
import com.onlive.trackify.data.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatabaseInitializer(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    fun initializeCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            val existingCategories = db.categoryDao().getCategoriesCountSync()

            if (existingCategories == 0) {
                val categories = listOf(
                    Category(
                        name = "Развлечения",
                        colorCode = "#FF5252"
                    ),
                    Category(
                        name = "Стриминг",
                        colorCode = "#7C4DFF"
                    ),
                    Category(
                        name = "Музыка",
                        colorCode = "#448AFF"
                    ),
                    Category(
                        name = "Программы",
                        colorCode = "#4CAF50"
                    ),
                    Category(
                        name = "Игры",
                        colorCode = "#FF9800"
                    ),
                    Category(
                        name = "Облачные сервисы",
                        colorCode = "#00BCD4"
                    ),
                    Category(
                        name = "Спорт",
                        colorCode = "#795548"
                    ),
                    Category(
                        name = "Здоровье",
                        colorCode = "#009688"
                    )
                )

                for (category in categories) {
                    db.categoryDao().insert(category)
                }
            }
        }
    }
}