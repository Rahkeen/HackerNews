package dev.supergooey.hackernews.features.stories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.supergooey.hackernews.data.Item
import dev.supergooey.hackernews.ui.theme.HNOrange
import dev.supergooey.hackernews.ui.theme.HackerNewsTheme

@Composable
fun StoriesScreen(
  modifier: Modifier = Modifier,
  state: StoriesState,
  actions: (StoriesAction) -> Unit,
  navigation: (String) -> Unit
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    TitleDisplay()
    LazyColumn(modifier = Modifier
      .fillMaxWidth()
      .weight(1f)) {
      items(state.stories) { item ->
        StoryRow(item) {
          actions(StoriesAction.SelectStory(it.id))
          navigation("story/${it.id}")
        }
      }
    }
  }
}

@Preview
@Composable
private fun TitleDisplay() {
    Text(
      modifier = Modifier.drawBehind {
        drawLine(
          start = Offset(0f, size.height-10),
          end = Offset(size.width, size.height-10),
          color = HNOrange,
          strokeWidth = 6f,
          cap = StrokeCap.Round
        )
      },
      text = "Top Stories",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Medium,
      fontSize = 24.sp
    )
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
            score = 10,
            type = "story"
          ),
          Item(
            id = 1L,
            title = "Nice to Meet You",
            by = "vasant",
            score = 5,
            type = "story",
          ),
        )
      ),
      actions = {},
      navigation = {}
    )
  }
}

@Preview
@Composable
private fun StoryRowPreview() {
  HackerNewsTheme {
    StoryRow(
      item = Item(
        id = 1L,
        by = "heyrikin",
        title = "A theory on why NIA is a terrible example of a demo application",
        score = 10,
        type = "Story",
        url = "www.google.com",
      )
    ) {}
  }
}

@Composable
fun StoryRow(item: Item, onClick: (Item) -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 80.dp)
      .clickable {
        onClick(item)
      }
      .padding(8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxHeight()
        .weight(1f),
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = item.title,
        color = Color.Black,
        style = MaterialTheme.typography.titleSmall
      )
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "${item.score}", style = MaterialTheme.typography.labelSmall)
        Text(text = "•", style = MaterialTheme.typography.labelSmall)
        Text(
          text = item.by,
          color = HNOrange,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}
