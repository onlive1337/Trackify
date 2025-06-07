package com.onlive.trackify.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.Subscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.room.withTransaction

@Database(
    entities = [Subscription::class, Payment::class, Category::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun paymentDao(): PaymentDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "trackify_database"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `category_groups` (
                        `groupId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT,
                        `colorCode` TEXT NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `categories_new` (
                        `categoryId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT,
                        `colorCode` TEXT NOT NULL,
                        `groupId` INTEGER,
                        FOREIGN KEY(`groupId`) REFERENCES `category_groups`(`groupId`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO `categories_new` (`categoryId`, `name`, `description`, `colorCode`, `groupId`)
                    SELECT `categoryId`, `name`, `description`, `colorCode`, NULL FROM `categories`
                """.trimIndent())

                database.execSQL("DROP TABLE IF EXISTS `categories`")
                database.execSQL("ALTER TABLE `categories_new` RENAME TO `categories`")

                database.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_groupId` ON `categories` (`groupId`)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `payments_new` (
                        `paymentId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `subscriptionId` INTEGER NOT NULL,
                        `amount` REAL NOT NULL,
                        `date` INTEGER NOT NULL,
                        `notes` TEXT,
                        FOREIGN KEY(`subscriptionId`) REFERENCES `subscriptions`(`subscriptionId`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO `payments_new` (`paymentId`, `subscriptionId`, `amount`, `date`, `notes`)
                    SELECT `paymentId`, `subscriptionId`, `amount`, `date`, `notes` FROM `payments`
                """.trimIndent())

                database.execSQL("DROP TABLE IF EXISTS `payments`")
                database.execSQL("ALTER TABLE `payments_new` RENAME TO `payments`")

                database.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_subscriptionId` ON `payments` (`subscriptionId`)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `categories_new` (
                        `categoryId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT,
                        `colorCode` TEXT NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO `categories_new` (`categoryId`, `name`, `description`, `colorCode`)
                    SELECT `categoryId`, `name`, `description`, `colorCode` FROM `categories`
                """.trimIndent())

                database.execSQL("DROP TABLE IF EXISTS `categories`")
                database.execSQL("ALTER TABLE `categories_new` RENAME TO `categories`")

                database.execSQL("DROP TABLE IF EXISTS `category_groups`")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(false)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }
    }

    suspend fun runInTransaction(block: suspend () -> Unit) {
        withContext(Dispatchers.IO) {
            withTransaction {
                block()
            }
        }
    }
}