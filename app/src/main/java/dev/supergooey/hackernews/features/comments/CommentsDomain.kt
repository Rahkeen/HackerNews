package dev.supergooey.hackernews.features.comments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.supergooey.hackernews.data.HackerNewsClient
import dev.supergooey.hackernews.data.Item
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
) {
  companion object {
    val empty = CommentState(
      id = 0L,
      content = "",
      children = emptyList()
    )
  }
}

fun Item.toCommentsState(): CommentsState {
  return CommentsState(
    comments = this.kids?.map { CommentState(it, "$it", emptyList()) } ?: emptyList()
  )
}

fun Item.toCommentState(level: Int = 0): CommentState {
  return CommentState(
    id = this.id,
    content = this.text ?: "",
    children = emptyList(),
    level = level
  )
}


class CommentsViewModel(private val itemId: Long): ViewModel() {
  private val internalState = MutableStateFlow(CommentsState.empty)
  val state = internalState.asStateFlow()

  init {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        val item = HackerNewsClient.api.getItem(itemId)
        val rootCommentIds = item.kids
        val comments = mutableListOf<CommentState>()
        rootCommentIds?.forEach { commentId ->
          val comment = HackerNewsClient.api.getItem(commentId)
          comments.add(createCommentTree(comment, 0))
        }
        comments.forEach { Log.d("Parsing Comment", "Comment ID: ${it.id}, Children: ${it.children.size}") }
        internalState.update {
          CommentsState(
            comments = comments
          )
        }
      }
    }
  }

  // traversing a tree
  private suspend fun createCommentTree(root: Item, level: Int): CommentState {
    Log.d("Parsing Comments", "Level: $level, Id: ${root.id}")
    val children = mutableListOf<CommentState>()

    root.kids?.forEach { childId ->
      val child = HackerNewsClient.api.getItem(childId)
      children.add(createCommentTree(child, level+1))
    }

    return CommentState(
      id = root.id,
      content = root.text ?: "",
      children = children,
      level = level
    )
  }


  @Suppress("UNCHECKED_CAST")
  class Factory(private val itemId: Long): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return CommentsViewModel(itemId) as T
    }
  }
}
