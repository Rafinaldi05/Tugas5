package com.example.tugas5

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_data")

class UserDataStore(private val context: Context) {

    private val USER_NAME = stringPreferencesKey("user_name")
    private val USER_EMAIL = stringPreferencesKey("user_email")

    suspend fun saveUserData(name: String?, email: String?) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name ?: ""
            preferences[USER_EMAIL] = email ?: ""
        }
    }

    fun getUserData(): Flow<Pair<String?, String?>> {
        return context.dataStore.data
            .map { preferences ->
                val name = preferences[USER_NAME]
                val email = preferences[USER_EMAIL]
                Pair(name, email)
            }
    }

    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = ""
            preferences[USER_EMAIL] = ""
        }
    }
}
