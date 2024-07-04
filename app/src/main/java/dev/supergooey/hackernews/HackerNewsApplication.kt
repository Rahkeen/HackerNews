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
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
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
      .cookieJar(PrefsCookieJar(cookieStorage))
//      .addNetworkInterceptor(ReceiveCookieInterceptor(cookieStorage))
//      .addNetworkInterceptor(AttachCookieInterceptor(cookieStorage))
      .build()

    webClient = HackerNewsWebClient(okHttpClient)
  }
}

fun Context.webClient(): HackerNewsWebClient {
  return (applicationContext as HackerNewsApplication).webClient
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class PrefsCookieJar(private val cookieStorage: CookieStorage): CookieJar {

  override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
    Log.d("Cookie Jar", "Url: $url, cookie = ${cookies[0]}")
    cookies.firstOrNull { it.name == "user" }?.let { authCookie ->
      runBlocking { cookieStorage.putCookie(authCookie.value) }
    }
  }

  override fun loadForRequest(url: HttpUrl): List<Cookie> {
    val authCookie = runBlocking { cookieStorage.getCookie().firstOrNull() }
    Log.d("Cookie Jar", "Cookie: user=$authCookie" )
    return if (authCookie != null) {
      val cookie = Cookie.Builder()
        .name("user")
        .value(authCookie)
        .domain("news.ycombinator.com")
        .build()
      listOf(cookie)
    } else {
      emptyList()
    }
  }
}

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
