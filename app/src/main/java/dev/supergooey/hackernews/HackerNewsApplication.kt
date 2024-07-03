package dev.supergooey.hackernews

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dev.supergooey.hackernews.data.CookieStorage
import dev.supergooey.hackernews.data.HackerNewsWebClient
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class HackerNewsApplication : Application() {
  private lateinit var okHttpClient: OkHttpClient
  lateinit var webClient: HackerNewsWebClient

  override fun onCreate() {
    super.onCreate()

    // initialize storage
    val cookieStorage = CookieStorage(context = applicationContext)

    okHttpClient = OkHttpClient
      .Builder()
      .addNetworkInterceptor(ReceiveCookieInterceptor(cookieStorage))
      .addNetworkInterceptor(AttachCookieInterceptor(cookieStorage))
      .build()

    webClient = HackerNewsWebClient(okHttpClient)
  }
}

fun Context.webClient(): HackerNewsWebClient {
  return (applicationContext as HackerNewsApplication).webClient
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class ReceiveCookieInterceptor(
  private val cookieStorage: CookieStorage
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val response = chain.proceed(chain.request())
    if (response.headers("Set-Cookie").isNotEmpty()) {
      val cookie = response.headers["Set-Cookie"]
      runBlocking { cookieStorage.putCookie(cookie.orEmpty()) }
      Log.d("Login", "Cookie set: $cookie")
    }
    return response
  }
}

class AttachCookieInterceptor(
  private val cookieStorage: CookieStorage
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val cookie = runBlocking { cookieStorage.getCookie().firstOrNull() }
    val request = if (!cookie.isNullOrEmpty()) {
      chain.request().newBuilder().addHeader("Cookie", cookie).build()
    } else {
      chain.request()
    }
    return chain.proceed(request)
  }
}
