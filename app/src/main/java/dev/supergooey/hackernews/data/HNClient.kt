package dev.supergooey.hackernews.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://hacker-news.firebaseio.com/v0/"

@Serializable
data class Item(
  val id: Long,
  val by: String,
  val title: String,
  val url: String,
  val type: String
)

interface HNService {
  @GET("topstories.json")
  suspend fun getTopStoryIds(): List<Long>
}

object HNClient {
  private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(Json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
    .build()

  val service = retrofit.create(HNService::class.java)
}