package dev.supergooey.hackernews

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
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
  val model = viewModel<StoriesViewModel>()
  val state by model.state.collectAsState()

  Scaffold { innerPadding ->
    StoriesScreen(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      state = state
    )
  }
}

@Preview
@Composable
private fun StorieScreenPreview() {
  HackerNewsTheme {
    StoriesScreen(
      modifier = Modifier.fillMaxSize(),
      state = StoriesState(
        stories = listOf(
          Item(
            id = 1L,
            title = "Hello There",
            by = "heyrikin",
            type = "story"
          ),
          Item(
            id = 1L,
            title = "Nice to Meet You",
            by = "vasant",
            type = "story"
          ),
        )
      )
    )
  }
}

@Composable
fun StoriesScreen(modifier: Modifier = Modifier, state: StoriesState) {
  LazyColumn(modifier = modifier) {
    items(state.stories) { item ->
      StoryRow(item)
    }
  }
}

@Preview
@Composable
private fun StoryRowPreview() {
  StoryRow(
    item = Item(
      id = 1L,
      by = "heyrikin",
      title = "A theory on why NIA is a terrible example of a demo application",
      type = "Story"
    )
  )
}

@Composable
fun StoryRow(item: Item) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 80.dp)
      .padding(8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // title + subtitle
    Column(
      modifier = Modifier
        .fillMaxHeight()
        .weight(1f),
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        item.title,
        color = Color.Black,
        fontSize = 20.sp,
        fontWeight = FontWeight(500),
        lineHeight = 1.em
      )
      Text(item.by, color = Color.DarkGray)
    }

    Icon(
      modifier = Modifier.size(24.dp),
      imageVector = Icons.Default.KeyboardArrowUp,
      contentDescription = "Comments"
    )
  }
}

data class StoriesState(
  val stories: List<Item>
)

class StoriesViewModel() : ViewModel() {
  private val internalState = MutableStateFlow(StoriesState(stories = emptyList()))
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