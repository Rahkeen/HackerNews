package dev.supergooey.hackernews.features.comments

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.supergooey.hackernews.webClient
import kotlinx.serialization.Serializable

sealed interface CommentsDestinations {
  @Serializable
  data class Comments(val storyId: Long) : CommentsDestinations
}

fun NavGraphBuilder.commentsRoutes() {
  composable<CommentsDestinations.Comments> { entry ->
    val comments: CommentsDestinations.Comments = entry.toRoute()
    val context = LocalContext.current
    val model = viewModel<CommentsViewModel>(
      factory = CommentsViewModel.Factory(
        comments.storyId,
        context.webClient()
      )
    )
    val state by model.state.collectAsState()
    CommentsScreen(state, model::actions)
  }
}




