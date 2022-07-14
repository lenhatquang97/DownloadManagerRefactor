package com.quangln2.mydownloadmanager.data.model.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.quangln2.mydownloadmanager.DownloadManagerApplication.Companion.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object GlobalSettings {
    //Vibration
    private val IS_VIBRATED = booleanPreferencesKey("isVibrated")
    val getVibrated: (Context) -> Flow<Boolean> = { it ->
        it.dataStore.data.map {
            it[IS_VIBRATED] ?: false
        }
    }

    suspend fun setVibrated(context: Context, isVibrated: Boolean) {
        context.dataStore.edit {
            it[IS_VIBRATED] = isVibrated
        }
    }

    //Show on lock screen
    private val SHOW_ON_LOCK_SCREEN = booleanPreferencesKey("showOnLockScreen")
    val getShowOnLockScreen: (Context) -> Flow<Boolean> = { it ->
        it.dataStore.data.map {
            it[SHOW_ON_LOCK_SCREEN] ?: false
        }
    }

    suspend fun setShowOnLockScreen(context: Context, isShown: Boolean) {
        context.dataStore.edit {
            it[SHOW_ON_LOCK_SCREEN] = isShown
        }
    }

    //Pop up message
    private val POP_UP_MESSAGE = booleanPreferencesKey("popUpMessage")
    val getPopUpMessage: (Context) -> Flow<Boolean> = { it ->
        it.dataStore.data.map {
            it[POP_UP_MESSAGE] ?: false
        }
    }

    suspend fun setPopUpMessage(context: Context, isPopped: Boolean) {
        context.dataStore.edit {
            it[POP_UP_MESSAGE] = isPopped
        }
    }


}