package dev.supergooey.hackernews.features.stories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.supergooey.hackernews.data.HackerNewsBaseClient
import dev.supergooey.hackernews.data.Item
import dev.supergooey.hackernews.features.comments.CommentsDestinations
import dev.supergooey.hackernews.features.stories.StoriesAction.LoadStories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StoriesState(
  val stories: List<Item>,
)

sealed interface StoryItem {
  data class Loading(val id: Long) : StoryItem
  data class Content(
    val id: Long,
    val title: String,
    val author: String,
    val url: String
  ) : StoryItem
}

sealed class StoriesAction {
  data object LoadStories : StoriesAction()
  data class SelectStory(val id: Long) : StoriesAction()
  data class SelectComments(val id: Long): StoriesAction()
}

// TODO(rikin): Second pass at Navigation Setup
sealed interface StoriesNavigation {
  data class GoToStory(val closeup: StoriesDestinations.Closeup): StoriesNavigation
  data class GoToComments(val comments: CommentsDestinations.Comments): StoriesNavigation
}

class StoriesViewModel(private val baseClient: HackerNewsBaseClient) : ViewModel() {
  private val internalState = MutableStateFlow(StoriesState(stories = emptyList()))
  val state = internalState.asStateFlow()

  init {
    actions(LoadStories)
  }

  fun actions(action: StoriesAction) {
    when (action) {
      LoadStories -> {
        viewModelScope.launch {
          withContext(Dispatchers.IO) {
            val ids = baseClient.api.getTopStoryIds()
            // now for each ID I need to load the item.
            ids.take(20).forEach { id ->
              val item = baseClient.api.getItem(id)
              if (item.type == "story" && item.url != null) {
                internalState.update { current ->
                  current.copy(
                    stories = current.stories.toMutableList().apply { add(item) }.toList()
                  )
                }
              }
            }
          }
        }
      }

      is StoriesAction.SelectStory -> {
        // TODO
      }

      is StoriesAction.SelectComments -> {
        // TODO
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  class Factory(private val baseClient: HackerNewsBaseClient): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return StoriesViewModel(baseClient) as T
    }
  }
}
