package com.agunganamiroh.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.readDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_read")

class NotificationReadManager(private val context: Context) {
    private val readIdsKey = stringPreferencesKey("read_activity_ids")

    val readIds: Flow<Set<String>> = context.readDataStore.data.map { prefs ->
        val raw = prefs[readIdsKey] ?: ""
        if (raw.isEmpty()) emptySet() else raw.split(",").toSet()
    }

    suspend fun markAsRead(activityId: String) {
        context.readDataStore.edit { prefs ->
            val current = prefs[readIdsKey] ?: ""
            val set = if (current.isEmpty()) mutableSetOf() else current.split(",").toMutableSet()
            set.add(activityId)
            prefs[readIdsKey] = set.joinToString(",")
        }
    }

    suspend fun markAllAsRead(ids: List<String>) {
        context.readDataStore.edit { prefs ->
            val current = prefs[readIdsKey] ?: ""
            val existing = if (current.isEmpty()) emptySet() else current.split(",").toSet()
            val merged = (existing + ids.toSet()).joinToString(",")
            prefs[readIdsKey] = merged
        }
    }
}
