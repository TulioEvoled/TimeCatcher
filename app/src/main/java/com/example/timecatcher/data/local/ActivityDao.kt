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
            // Convirtiendo boolean a int (0 = false, 1 = true)
            put(DatabaseHelper.COLUMN_COMPLETED, if (item.completed) 1 else 0)
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
                // Convertir integer -> boolean
                val completedInt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPLETED))
                val completed = (completedInt == 1)

                val activityItem = ActivityItem(
                    id = id,
                    title = title,
                    description = description,
                    latitude = latitude,
                    longitude = longitude,
                    estimatedTime = estimatedTime,
                    completed = completed
                )
                activities.add(activityItem)

                    val activity = cursorToActivity(cursor)
                    activities.add(activity)

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return activities
    }

    fun getActivityById(itemId: Int): ActivityItem? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_ACTIVITIES} WHERE ${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(itemId.toString())
        )
        var activityItem: ActivityItem? = null
        if (cursor.moveToFirst()) {
            activityItem = cursorToActivity(cursor)
        }
        cursor.close()
        db.close()
        return activityItem
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
            put(DatabaseHelper.COLUMN_COMPLETED, if (item.completed) 1 else 0)
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

    // Función auxiliar para convertir Cursor a ActivityItem
    private fun cursorToActivity(cursor: Cursor): ActivityItem {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
        val title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE))
        val description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION))
        val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE))
        val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE))
        val estimatedTime = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ESTIMATED_TIME))
        val completedInt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPLETED))
        val completed = (completedInt == 1)

        return ActivityItem(
            id = id,
            title = title,
            description = description,
            latitude = latitude,
            longitude = longitude,
            estimatedTime = estimatedTime,
            completed = completed
        )
    }
}

