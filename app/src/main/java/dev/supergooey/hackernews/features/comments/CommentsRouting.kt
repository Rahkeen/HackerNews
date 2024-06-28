package dev.supergooey.hackernews.features.comments

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable


sealed interface CommentsDestinations {
  @Serializable
  data class Comments(val storyId: Long) : CommentsDestinations
}

fun NavGraphBuilder.commentsRoutes() {
  composable<CommentsDestinations.Comments> { entry ->
    val comments: CommentsDestinations.Comments = entry.toRoute()
    CommentsScreen()
  }
}




