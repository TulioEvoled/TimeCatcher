package com.example.timecatcher.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.timecatcher.data.model.ActivityItem

class ActivityDAO(context: Context) {

    private val dbHelper = DatabaseHelper(context)


    fun insertActivity(item: ActivityItem): Long {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_TITLE, item.title)
            put(DatabaseHelper.COLUMN_DESCRIPTION, item.description)
            put(DatabaseHelper.COLUMN_LATITUDE, item.latitude)
            put(DatabaseHelper.COLUMN_LONGITUDE, item.longitude)
            put(DatabaseHelper.COLUMN_ESTIMATED_TIME, item.estimatedTime)
        }
        val resultId = db.insert(DatabaseHelper.TABLE_ACTIVITIES, null, values)
        db.close()
        return resultId
    }

    /**
     * Obtener todas las actividades
     */
    fun getAllActivities(): List<ActivityItem> {
        val activities = mutableListOf<ActivityItem>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_ACTIVITIES}", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE))
                val description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION))
                val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE))
                val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE))
                val estimatedTime = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ESTIMATED_TIME))

                val activityItem = ActivityItem(
                    id = id,
                    title = title,
                    description = description,
                    latitude = latitude,
                    longitude = longitude,
                    estimatedTime = estimatedTime
                )
                activities.add(activityItem)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return activities
    }

    /**
     * Actualizar una actividad
     */
    fun updateActivity(item: ActivityItem): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_TITLE, item.title)
            put(DatabaseHelper.COLUMN_DESCRIPTION, item.description)
            put(DatabaseHelper.COLUMN_LATITUDE, item.latitude)
            put(DatabaseHelper.COLUMN_LONGITUDE, item.longitude)
            put(DatabaseHelper.COLUMN_ESTIMATED_TIME, item.estimatedTime)
        }
        val rowsAffected = db.update(
            DatabaseHelper.TABLE_ACTIVITIES,
            values,
            "${DatabaseHelper.COLUMN_ID}=?",
            arrayOf(item.id.toString())
        )
        db.close()
        return rowsAffected
    }

    /**
     * Eliminar una actividad
     */
    fun deleteActivity(itemId: Int): Int {
        val db = dbHelper.writableDatabase
        val rowsDeleted = db.delete(
            DatabaseHelper.TABLE_ACTIVITIES,
            "${DatabaseHelper.COLUMN_ID}=?",
            arrayOf(itemId.toString())
        )
        db.close()
        return rowsDeleted
    }
}

