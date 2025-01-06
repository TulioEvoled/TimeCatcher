package com.example.timecatcher.data.model

data class ActivityItem(
    val id: Int = 0,
    val title: String,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val estimatedTime: Int?
)