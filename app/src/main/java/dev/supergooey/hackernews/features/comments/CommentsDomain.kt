package dev.supergooey.hackernews.features.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
  val content: String,
  val children: List<CommentState>
) {
  companion object {
    val empty = CommentState(
      content = "",
      children = emptyList()
    )
  }
}


class CommentsViewModel(private val itemId: Long): ViewModel() {
  private val internalState = MutableStateFlow(CommentsState.empty)
  val state = internalState.asStateFlow()

  @Suppress("UNCHECKED_CAST")
  class Factory(private val itemId: Long): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return CommentsViewModel(itemId) as T
    }
  }
}
