package dev.supergooey.hackernews.features.comments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.supergooey.hackernews.ui.theme.HackerNewsTheme

@Composable
fun CommentsScreen() {
  Column(modifier = Modifier.fillMaxSize()) {
    Text("Hello Comments")
  }
}

@Preview
@Composable
private fun CommentsScreenPreview() {
  HackerNewsTheme {
    CommentsScreen()
  }
}