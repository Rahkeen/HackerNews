package dev.supergooey.hackernews.features.comments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.supergooey.hackernews.data.HackerNewsAlgoliaClient
import dev.supergooey.hackernews.data.ItemResponse
import dev.supergooey.hackernews.features.login.CookieJar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.text.Typography.amp

data class CommentsState(
  val id: Long,
  val title: String,
  val author: String,
  val points: Int,
  val comments: List<CommentState>,
  val liked: Boolean = false,
  val html: String? = null,
) {
  companion object {
    val empty = CommentsState(
      id = 0,
      title = "",
      author = "",
      points = 0,
      comments = emptyList(),
      html = null
    )
  }

  val headerState = HeaderState(id, title, author, points, liked)
}

data class CommentState(
  val id: Long,
  val author: String,
  val content: String,
  val children: List<CommentState>,
  val level: Int = 0,
)

sealed interface CommentsAction {
  data object LikeTapped : CommentsAction
}

data class HeaderState(
  val id: Long,
  val title: String,
  val author: String,
  val points: Int,
  val liked: Boolean
)

class CommentsViewModel(
  private val itemId: Long,
  private val jar: CookieJar,
) : ViewModel() {
  private val internalState = MutableStateFlow(CommentsState.empty)
  val state = internalState.asStateFlow()
  private val client = OkHttpClient.Builder()
    .build()
  private var likeUrlPath = ""

  init {
    viewModelScope.launch(Dispatchers.IO) {
      val cookie = jar.getCookie().first()
      Log.d("Comments", "Cookie: $cookie")
      val response = HackerNewsAlgoliaClient.api.getItem(itemId)
      val itemPageResponse = async {
        client.newCall(
          Request.Builder()
            .addHeader("Cookie", cookie)
            .url("https://news.ycombinator.com/item?id=$itemId")
            .get()
            .build()
        ).execute()
      }.await()

      // extract item like URL
      val html = itemPageResponse.body?.string()!!
      val split = html.split("up_$itemId")[1]
      Log.d("Comments", "First Split: $split")
      val pattern = "href\\s*=\\s*(['\"])([^\"]*)\\1".toRegex()
      val match = pattern.find(split)
      Log.d("Comments", "Match: ${match?.value}")
      val url = match?.groupValues?.get(2)
      val extracted = url?.split("'")?.get(0)
      Log.d("Comments", "Extracted Upvote Url: $extracted")
      likeUrlPath = extracted?.let { URLDecoder.decode(it, Charsets.UTF_8) } ?: ""
      likeUrlPath = likeUrlPath.replace("&amp;", "&")
      Log.d("Comments", "Decoded Upvote Url: $likeUrlPath")
      val gotoIndex = likeUrlPath.indexOf("goto")
      val gotoUrlPath = likeUrlPath.substring(gotoIndex)
      Log.d("Comments", "Goto URl Substring: $gotoUrlPath")
      val gotoUrlSplit = gotoUrlPath.split("=", limit = 2)
      val goToEncodedValue = URLEncoder.encode(gotoUrlSplit[1], Charsets.UTF_8)
      Log.d("Comments", "Re-encoded item for goto: $goToEncodedValue")
      val finalUrl = likeUrlPath.substring(0, gotoIndex) + gotoUrlSplit[0] + "=" + goToEncodedValue
      Log.d("Comments", "Final Url: $finalUrl")
      likeUrlPath = finalUrl

      val comments = response.children.map { rootComment ->
        rootComment.createCommentState(0)
      }
      internalState.update {
        CommentsState(
          id = response.id,
          title = response.title ?: "",
          author = response.author ?: "",
          points = response.points ?: 0,
          comments = comments,
          html = html
        )
      }
    }
  }

  fun actions(action: CommentsAction) {
    when (action) {
      CommentsAction.LikeTapped -> {
        viewModelScope.launch(Dispatchers.IO) {
          if (likeUrlPath.isNotEmpty()) {
            val cookie = jar.getCookie().first()
            val likePostRequest = Request.Builder()
                .addHeader("Cookie", cookie)
                .url("https://news.ycombinator.com/$likeUrlPath")
                .get()
                .build()
            Log.d("Comments", "Like Post Request: ${likePostRequest.url}")
            val likePostResponse = client.newCall(likePostRequest).execute()
            Log.d("Comments", "Like Post: $likePostResponse")
            if (likePostResponse.code == 200) {
              internalState.update { current ->
                current.copy(liked = true)
              }
            }
          }
        }
      }
    }
  }

  private fun ItemResponse.createCommentState(level: Int): CommentState {
    Log.d("Creating CommentState()", "Level: $level, Id: $id")

    return CommentState(
      id = id,
      author = author ?: "",
      content = text ?: "",
      children = children.map { child ->
        child.createCommentState(level + 1)
      },
      level = level
    )
  }


  @Suppress("UNCHECKED_CAST")
  class Factory(
    private val itemId: Long,
    private val jar: CookieJar
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return CommentsViewModel(itemId, jar) as T
    }
  }
}
