package dev.supergooey.hackernews.features.stories

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dev.supergooey.hackernews.features.stories.StoriesDestinations.Feed
import kotlinx.serialization.Serializable

@Serializable
data object Stories

sealed interface StoriesDestinations {
  @Serializable
  data object Feed: StoriesDestinations
}

fun NavGraphBuilder.storiesGraph(navController: NavController) {
  navigation<Stories>(startDestination = Feed) {
    composable<Feed> {
      val model = viewModel<StoriesViewModel>()
      val state by model.state.collectAsState()
      StoriesScreen(
        state = state,
        actions = model::actions,
        navigation = {
          // TODO
        }
      )
    }
  }
}