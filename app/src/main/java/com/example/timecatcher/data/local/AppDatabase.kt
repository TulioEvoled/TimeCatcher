package com.example.timecatcher.data.local
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.timecatcher.data.model.ActivityItem

@Database(entities = [ActivityItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timecatcher_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}