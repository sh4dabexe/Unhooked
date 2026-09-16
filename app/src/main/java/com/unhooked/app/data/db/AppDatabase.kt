package com.unhooked.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.unhooked.app.data.db.daos.AnalyticsDao
import com.unhooked.app.data.db.daos.RuleDao
import com.unhooked.app.data.db.daos.SessionDao
import com.unhooked.app.data.db.entities.AppRuleEntity
import com.unhooked.app.data.db.entities.BlockedAttemptEntity
import com.unhooked.app.data.db.entities.FocusSessionEntity
import com.unhooked.app.data.db.entities.ScheduleEntity
import com.unhooked.app.data.db.entities.WebsiteRuleEntity

@Database(
    entities = [
        AppRuleEntity::class,
        WebsiteRuleEntity::class,
        ScheduleEntity::class,
        FocusSessionEntity::class,
        BlockedAttemptEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ruleDao(): RuleDao
    abstract fun sessionDao(): SessionDao
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unhooked_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
