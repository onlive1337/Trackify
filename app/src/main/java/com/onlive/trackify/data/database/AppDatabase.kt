package com.onlive.trackify.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.CategoryGroup
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.Subscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [Subscription::class, Payment::class, Category::class, CategoryGroup::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun paymentDao(): PaymentDao
    abstract fun categoryDao(): CategoryDao
    abstract fun categoryGroupDao(): CategoryGroupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE payments ADD COLUMN status TEXT NOT NULL DEFAULT 'MANUAL'")
                database.execSQL("ALTER TABLE payments ADD COLUMN autoGenerated INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS category_groups (" +
                            "groupId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "description TEXT, " +
                            "colorCode TEXT NOT NULL)"
                )

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS categories_new (" +
                            "categoryId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "colorCode TEXT NOT NULL, " +
                            "groupId INTEGER, " +
                            "FOREIGN KEY (groupId) REFERENCES category_groups(groupId) ON DELETE SET NULL)"
                )

                database.execSQL(
                    "INSERT INTO categories_new (categoryId, name, colorCode, groupId) " +
                            "SELECT categoryId, name, colorCode, NULL FROM categories"
                )

                database.execSQL("DROP TABLE categories")

                database.execSQL("ALTER TABLE categories_new RENAME TO categories")

                database.execSQL("CREATE INDEX IF NOT EXISTS index_categories_groupId ON categories(groupId)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trackify_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun runInTransaction(block: suspend () -> Unit) {
        withContext(Dispatchers.IO) {
            beginTransaction()
            try {
                block()
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }
}