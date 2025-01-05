package com.example.timecatcher.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val estimatedTime: Int,  // en minutos, p.ej. 15, 30, 60
    val completed: Boolean = false
)