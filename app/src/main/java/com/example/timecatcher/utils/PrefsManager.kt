package com.example.timecatcher.utils

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("TimeCatcherPrefs", Context.MODE_PRIVATE)

    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("darkModeEnabled", enabled).apply()
    }

    fun isDarkModeEnabled(): Boolean {
        return prefs.getBoolean("darkModeEnabled", false)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notificationsEnabled", enabled).apply()
    }

    fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean("notificationsEnabled", true)
    }

}