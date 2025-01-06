package com.example.timecatcher.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        // Aquí creamos nuestras tablas
        val createActivitiesTable = """
            CREATE TABLE $TABLE_ACTIVITIES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_LATITUDE REAL,
                $COLUMN_LONGITUDE REAL,
                $COLUMN_ESTIMATED_TIME INTEGER
            );
        """.trimIndent()

        db?.execSQL(createActivitiesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Aquí manejamos la lógica de actualización de versiones
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ACTIVITIES")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "timecatcher.db"
        private const val DATABASE_VERSION = 1

        // Nombre de la tabla
        const val TABLE_ACTIVITIES = "activities"

        // Columnas
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_ESTIMATED_TIME = "estimatedTime"
    }
}
