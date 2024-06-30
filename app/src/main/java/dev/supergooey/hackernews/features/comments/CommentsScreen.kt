package dev.supergooey.hackernews.features.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.supergooey.hackernews.ui.theme.HackerNewsTheme

@Composable
fun CommentsScreen(state: CommentsState) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().background(color = Color.LightGray),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(items = state.comments) { comment ->
      CommentRow(comment)
    }
  }
}

@Preview
@Composable
private fun CommentsScreenPreview() {
  HackerNewsTheme {
    CommentsScreen(
      state = CommentsState(
        comments = listOf(
          CommentState(
            id = 1,
            content = "Hello There",
            children = emptyList()
          )
        )
      )
    )
  }
}

@Composable
fun CommentRow(state: CommentState) {
  val startPadding = (state.level * 16).dp
  Row(
    modifier = Modifier
      .padding(start = startPadding)
      .fillMaxWidth()
      .heightIn(min = 80.dp)
      .background(color = MaterialTheme.colorScheme.background)
      .padding(8.dp)
  ) {
    Text(
      text = state.content.parseAsHtml(),
      style = MaterialTheme.typography.labelSmall
    )
  }
  state.children.forEach { child ->
    CommentRow(child)
  }
}