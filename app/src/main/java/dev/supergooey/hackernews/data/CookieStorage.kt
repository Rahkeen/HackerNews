package dev.supergooey.hackernews.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.supergooey.hackernews.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class CookieStorage(
  private val context: Context,
) {
  private val key = stringPreferencesKey("Cookie")
  suspend fun putCookie(cookie: String) {
    context.dataStore.edit { jar ->
      jar[key] = cookie
    }
  }

  fun getCookie(): Flow<String> {
    return context.dataStore.data.map { it[key] }.filterNotNull()
  }
}
