package com.maary.yetanotherbatterynotifier

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, context.getString(R.string.name_shared_pref)))
    }
)

class PreferenceRepository @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    private val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    companion object {
        val SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
        val LEVEL1 = intPreferencesKey("alert_level_1")
        val LEVEL2 = intPreferencesKey("alert_level_2")

        val ALWAYS_SHOW_SPEED = booleanPreferencesKey("Always show speed Boolean")

        val FREQUENCY = longPreferencesKey("frequency")

        val TEMP_DND = booleanPreferencesKey("dnd")
        val TEMP_DND_ENABLED_TIME = longPreferencesKey("dnd_enabled_time")

        val DND_SET = booleanPreferencesKey("regular_dnd_set")
        val DND_START_TIME = stringPreferencesKey("dnd_start_time")
        val DND_END_TIME = stringPreferencesKey("dnd_end_time")

        val RATIO = intPreferencesKey("ratio")

        val ENABLE_FUCK_OEM = booleanPreferencesKey("fuck_oem")
    }

    fun getServiceEnabled(): Flow<Boolean> {
        return dataStore.data.map { pref ->
            pref[SERVICE_ENABLED] ?: false
        }
    }

    suspend fun toggleServiceState() {
        dataStore.edit { pref ->
            val newState = pref[SERVICE_ENABLED] ?: false
            pref[SERVICE_ENABLED] = !newState
        }
    }

    suspend fun setServiceState(state: Boolean) {
        dataStore.edit { pref ->
            pref[SERVICE_ENABLED] = state
        }
    }

    fun getLevel1(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[LEVEL1]?: 80
        }
    }

    suspend fun setLevel1(level: Int) {
        dataStore.edit { preferences ->
            preferences[LEVEL1] = level
        }
    }

    fun getLevel2(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[LEVEL2]?: 85
        }
    }

    suspend fun setLevel2(level: Int) {
        dataStore.edit { preferences ->
            preferences[LEVEL2] = level
        }
    }

    fun getAlwaysShow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[ALWAYS_SHOW_SPEED]?: false
        }
    }

    suspend fun setAlwaysShow(bool: Boolean) {
        dataStore.edit { preferences ->
            preferences[ALWAYS_SHOW_SPEED] = bool
        }
    }

    fun getFrequency(): Flow<Long> {
        return dataStore.data.map { preferences ->
            preferences[FREQUENCY]?: 5000L
        }
    }

    suspend fun setFrequency(frequency: Long) {
        dataStore.edit { preferences ->
            preferences[FREQUENCY] = frequency
        }
    }

    suspend fun toggleTempDnd() {
        if (getTempDnd().first()){
            setTempDnd(false)
        } else {
            setTempDnd(true)
            setTempDndEnabledTime(System.currentTimeMillis())
        }
    }

    fun getTempDnd(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[TEMP_DND]?: false
        }
    }

    suspend fun setTempDnd(bool: Boolean) {
        dataStore.edit { preferences ->
            preferences[TEMP_DND] = bool
        }
    }

    fun getTempDndEnabledTime(): Flow<Long> {
        return dataStore.data.map { preferences ->
            preferences[TEMP_DND_ENABLED_TIME]?: 0
        }
    }

    suspend fun setTempDndEnabledTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[TEMP_DND_ENABLED_TIME] = time
        }
    }

    fun getDndStartTime(): Flow<Date?> {
        return dataStore.data.map { preferences ->
            formatter.parse(preferences[DND_START_TIME]?: "23:00")
        }
    }

    suspend fun setDndStartTime(time: Date) {
        dataStore.edit { preferences ->
            preferences[DND_START_TIME] = formatter.format(time)
        }
    }

    fun getDndEndTime(): Flow<Date?> {
        return dataStore.data.map { preferences ->
            formatter.parse(preferences[DND_END_TIME]?: "07:00")
        }
    }

    suspend fun setDndEndTime(time: Date) {
        dataStore.edit { preferences ->
            preferences[DND_END_TIME] = formatter.format(time)
        }
    }

    fun getDndSet(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[DND_SET]?: false
        }
    }

    suspend fun setDndSet(bool: Boolean) {
        dataStore.edit { preferences ->
            preferences[DND_SET] = bool
        }
    }

    fun getRatio(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[RATIO]?: 1000
        }
    }

    suspend fun setRatio(ratio: Int) {
        dataStore.edit { preferences ->
            preferences[RATIO] = ratio
        }
    }

    fun getFuckOEM(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[ENABLE_FUCK_OEM]?: false
        }
    }

    suspend fun setFuckOEM(bool: Boolean) {
        dataStore.edit { preferences ->
            preferences[ENABLE_FUCK_OEM] = bool
        }
    }
}