package dev.supergooey.hackernews.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

const val BASE_WEB_URL = "https://news.ycombinator.com/"
private const val LOGIN_URL = BASE_WEB_URL + "login"
private const val ITEM_URL = BASE_WEB_URL + "item"

data class ItemPage(
  val id: Long,
  val upvoted: Boolean,
  val upvoteUrl: String
)

class HackerNewsWebClient(
  private val httpClient: OkHttpClient,
) {
  suspend fun getItemPage(itemId: Long): ItemPage {
    return withContext(Dispatchers.IO) {
      // request page
      val response = httpClient.newCall(
        Request
          .Builder()
          .url("$ITEM_URL?id=$itemId")
          .build()
      ).execute()

      val document = Jsoup.parse(response.body?.string()!!)
      val upvoteElement = document.select("#up_$itemId")
      val upvoteHref = upvoteElement.attr("href")

      ItemPage(
        id = itemId,
        upvoted = upvoteElement.hasClass("nosee"),
        upvoteUrl = BASE_WEB_URL + upvoteHref
      )
    }
  }

  suspend fun upvoteItem(url: String): Boolean {
    return withContext(Dispatchers.IO) {
      val response = httpClient.newCall(
        Request.Builder()
          .url(url)
          .build()
      ).execute()

      response.isSuccessful
    }
  }
}



