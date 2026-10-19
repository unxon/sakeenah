package com.sakinah.reader.data

import android.content.Context
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sakinahDataStore by preferencesDataStore(name = "sakinah_preferences")

class UserPreferences(private val context: Context) {
    private val fontSizeKey = floatPreferencesKey("reader_font_size")
    private val darkModeKey = booleanPreferencesKey("dark_mode")

    val fontSize: Flow<Float> = context.sakinahDataStore.data.map { it[fontSizeKey] ?: 25f }
    val darkMode: Flow<Boolean> = context.sakinahDataStore.data.map { it[darkModeKey] ?: false }

    suspend fun setFontSize(value: Float) { context.sakinahDataStore.edit { it[fontSizeKey] = value.coerceIn(20f, 34f) } }
    suspend fun setDarkMode(value: Boolean) { context.sakinahDataStore.edit { it[darkModeKey] = value } }
}
