package com.schedule.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object ThemeManager {
    private val THEME_KEY = intPreferencesKey("theme_mode")

    const val THEME_SYSTEM = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2

    fun getThemeFlow(context: Context): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[THEME_KEY] ?: THEME_SYSTEM
        }
    }

    suspend fun setTheme(context: Context, themeMode: Int) {
        context.dataStore.edit { settings ->
            settings[THEME_KEY] = themeMode
        }
        applyTheme(themeMode)
    }

    fun applyTheme(themeMode: Int) {
        val nightMode = when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
