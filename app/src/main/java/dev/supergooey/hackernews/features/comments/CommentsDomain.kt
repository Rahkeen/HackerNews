package dev.supergooey.hackernews.features.comments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.supergooey.hackernews.data.CookieStorage
import dev.supergooey.hackernews.data.HackerNewsAlgoliaClient
import dev.supergooey.hackernews.data.HackerNewsWebClient
import dev.supergooey.hackernews.data.ItemPage
import dev.supergooey.hackernews.data.ItemResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommentsState(
  val id: Long,
  val title: String,
  val author: String,
  val points: Int,
  val comments: List<CommentState>,
  val liked: Boolean = false,
) {
  companion object {
    val empty = CommentsState(
      id = 0,
      title = "",
      author = "",
      points = 0,
      comments = emptyList(),
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
  private val webClient: HackerNewsWebClient
) : ViewModel() {
  private val internalState = MutableStateFlow(CommentsState.empty)
  val state = internalState.asStateFlow()

  private var page: ItemPage? = null

  init {
    viewModelScope.launch(Dispatchers.IO) {
      val itemResponse = HackerNewsAlgoliaClient.api.getItem(itemId)
      val itemPage = webClient.getItemPage(itemId)

      val comments = itemResponse.children.map { rootComment ->
        rootComment.createCommentState(0)
      }

      page = itemPage
      internalState.update {
        CommentsState(
          id = itemResponse.id,
          title = itemResponse.title ?: "",
          author = itemResponse.author ?: "",
          points = itemResponse.points ?: 0,
          comments = comments,
          liked = itemPage.upvoted
        )
      }
    }
  }

  fun actions(action: CommentsAction) {
    when (action) {
      CommentsAction.LikeTapped -> {
        viewModelScope.launch(Dispatchers.IO) {
          val url = page?.upvoteUrl.orEmpty()
          if (url.isNotEmpty()) {
            val success = webClient.upvoteItem(url)
            internalState.update { current ->
              current.copy(liked = success)
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
    private val webClient: HackerNewsWebClient
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return CommentsViewModel(itemId, webClient) as T
    }
  }
}
