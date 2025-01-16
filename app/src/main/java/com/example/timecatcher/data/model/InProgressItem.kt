package com.example.timecatcher.data.model


data class InProgressItem(
    val activity: ActivityItem,
    val startTimeMillis: Long,
    val finishTimeMillis: Long      // startTime + (estimatedTime * 60 * 1000)
)