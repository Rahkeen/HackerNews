package dev.supergooey.hackernews

import android.os.Bundle
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
import dev.supergooey.hackernews.data.HNClient
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
    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      state.storyIds.forEach { id ->
        Text("$id")
      }
    }
  }
}

data class AppState(
  val storyIds: List<Long>
)

class AppViewModel(): ViewModel() {
  private val internalState = MutableStateFlow(AppState(storyIds = emptyList()))
  val state = internalState.asStateFlow()

  init {
    getStoryIds()
  }

  fun getStoryIds() {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        val ids = HNClient.service.getTopStoryIds()
        internalState.update { current ->
          current.copy(storyIds = ids)
        }
      }
    }
  }
}