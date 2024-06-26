package dev.supergooey.hackernews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.supergooey.hackernews.data.Item
import dev.supergooey.hackernews.features.stories.StoriesScreen
import dev.supergooey.hackernews.features.stories.StoriesState
import dev.supergooey.hackernews.features.stories.StoriesViewModel
import dev.supergooey.hackernews.ui.theme.HackerNewsTheme

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
        .background(color = MaterialTheme.colorScheme.background)
        .padding(innerPadding),
      state = state,
      actions = model::actions
    )
  }
}

@Preview
@Composable
private fun StoriesScreenPreview() {
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
    ) {}
  }
}
