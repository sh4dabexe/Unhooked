package com.unhooked.app.data.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unhooked.app.data.db.entities.AppRuleEntity
import com.unhooked.app.data.db.entities.ScheduleEntity
import com.unhooked.app.data.db.entities.WebsiteRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {

    // App Rules
    @Query("SELECT * FROM app_rules ORDER BY appName ASC")
    fun getAllAppRulesFlow(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules WHERE enabled = 1")
    suspend fun getActiveAppRules(): List<AppRuleEntity>

    @Query("SELECT * FROM app_rules WHERE packageName = :packageName LIMIT 1")
    suspend fun getRuleForPackage(packageName: String): AppRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAppRule(rule: AppRuleEntity): Long

    @Delete
    suspend fun deleteAppRule(rule: AppRuleEntity)

    // Schedules
    @Query("SELECT * FROM schedules ORDER BY startHour ASC, startMinute ASC")
    fun getAllSchedulesFlow(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE enabled = 1")
    suspend fun getActiveSchedules(): List<ScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    // Website Rules
    @Query("SELECT * FROM website_rules ORDER BY domain ASC")
    fun getAllWebsiteRulesFlow(): Flow<List<WebsiteRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebsiteRule(rule: WebsiteRuleEntity): Long

    @Delete
    suspend fun deleteWebsiteRule(rule: WebsiteRuleEntity)
}
