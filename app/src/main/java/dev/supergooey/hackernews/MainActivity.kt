package dev.supergooey.hackernews

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.supergooey.hackernews.data.HackerNewsClient
import dev.supergooey.hackernews.data.Item
import dev.supergooey.hackernews.ui.theme.HackerNewsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      HackerNewsTheme {
        App()
      }
    }
  }
}

@Composable
fun App() {
  val model = viewModel<AppViewModel>()
  val state by model.state.collectAsState()

  Scaffold { innerPadding ->
    Column(modifier = Modifier
      .fillMaxSize()
      .padding(innerPadding)) {
      state.stories.forEach { story ->
        Text(story.title)
      }
    }
  }
}

data class AppState(
  val stories: List<Item>
)

class AppViewModel() : ViewModel() {
  private val internalState = MutableStateFlow(AppState(stories = emptyList()))
  val state = internalState.asStateFlow()

  init {
    getStories()
  }

  fun getStories() {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        val ids = HackerNewsClient.api.getTopStoryIds()
        val items = mutableListOf<Item>()
        // now for each ID I need to load the item.
        ids.take(20).forEach { id ->
          val item = HackerNewsClient.api.getItem(id)
          Log.d("API", "Story Loaded: ${item.id}")
          if (items.contains(item)) {
            // replace item
          } else {
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
}