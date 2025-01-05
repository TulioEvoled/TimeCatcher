package com.example.timecatcher.data.repository
import android.content.Context
import com.example.timecatcher.data.local.AppDatabase
import com.example.timecatcher.data.model.ActivityItem

class ActivityRepository(context: Context) {
    private val activityDao = AppDatabase.getInstance(context).activityDao()

    suspend fun addActivity(item: ActivityItem) {
        activityDao.insertActivity(item)
    }

    suspend fun getAllActivities(): List<ActivityItem> {
        return activityDao.getAllActivities()
    }
    suspend fun updateActivity(item: ActivityItem) {
        activityDao.updateActivity(item)
    }

    suspend fun deleteActivity(item: ActivityItem) {
        activityDao.deleteActivity(item)
    }
}
