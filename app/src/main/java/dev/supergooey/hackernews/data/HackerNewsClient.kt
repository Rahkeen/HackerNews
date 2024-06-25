package dev.supergooey.hackernews.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private const val BASE_URL = "https://hacker-news.firebaseio.com/v0/"

@Serializable
data class Item(
  val id: Long,
  val by: String,
  val title: String,
  val type: String
)

interface HackerNewsApi {
  @GET("topstories.json")
  suspend fun getTopStoryIds(): List<Long>

  @GET("item/{id}.json")
  suspend fun getItem(@Path("id") itemId: Long): Item
}

object HackerNewsClient {
  private val json = Json { ignoreUnknownKeys = true }
  private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
    .build()

  val api: HackerNewsApi = retrofit.create(HackerNewsApi::class.java)
}