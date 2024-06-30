package dev.supergooey.hackernews.features.comments

import android.text.Html
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dev.supergooey.hackernews.ui.theme.HackerNewsTheme

@Composable
fun CommentsScreen(state: CommentsState) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
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
      state = CommentsState.empty
    )
  }
}

@Composable
fun CommentRow(state: CommentState) {
  val bottomPadding = 0.dp
  val startPadding = (state.level * 32).dp
  val html = Html.fromHtml(state.content, Html.FROM_HTML_MODE_COMPACT)
  Row(modifier = Modifier.fillMaxWidth().padding(start = startPadding, bottom = bottomPadding)) {
    AndroidView(
      factory = { TextView(it) },
      update = { it.text = html }
    )
//    Text(text = state.content, style = MaterialTheme.typography.labelSmall)
  }
  state.children.forEach { child ->
    CommentRow(child)
  }
}