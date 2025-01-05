package com.example.timecatcher.data.local

import androidx.room.*
import com.example.timecatcher.data.model.ActivityItem

@Dao
interface ActivityDao {
    @Insert
    suspend fun insertActivity(activity: ActivityItem)

    @Update
    suspend fun updateActivity(activity: ActivityItem)

    @Delete
    suspend fun deleteActivity(activity: ActivityItem)

    @Query("SELECT * FROM activities")
    suspend fun getAllActivities(): List<ActivityItem>

    @Query("SELECT * FROM activities WHERE completed = :completed")
    suspend fun getActivitiesByCompletion(completed: Boolean): List<ActivityItem>
}
