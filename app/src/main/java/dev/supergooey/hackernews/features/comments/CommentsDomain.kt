package dev.supergooey.hackernews.features.comments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.supergooey.hackernews.data.HackerNewsAlgoliaClient
import dev.supergooey.hackernews.data.ItemResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CommentsState(
  val comments: List<CommentState>
) {
  companion object {
    val empty = CommentsState(
      comments = emptyList()
    )
  }
}

data class CommentState(
  val id: Long,
  val content: String,
  val children: List<CommentState>,
  val level: Int = 0,
)

class CommentsViewModel(private val itemId: Long) : ViewModel() {
  private val internalState = MutableStateFlow(CommentsState.empty)
  val state = internalState.asStateFlow()

  init {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        val response = HackerNewsAlgoliaClient.api.getItem(itemId)
        val comments = response.children.map { rootComment ->
          rootComment.createCommentState(0)
        }
        internalState.update {
          CommentsState(
            comments = comments
          )
        }
      }
    }
  }

  private fun ItemResponse.createCommentState(level: Int): CommentState {
    Log.d("Creating CommentState()", "Level: $level, Id: $id")

    return CommentState(
      id = id,
      content = text ?: "",
      children = children.map { child ->
        child.createCommentState(level + 1)
      },
      level = level
    )
  }


  @Suppress("UNCHECKED_CAST")
  class Factory(private val itemId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return CommentsViewModel(itemId) as T
    }
  }
}
