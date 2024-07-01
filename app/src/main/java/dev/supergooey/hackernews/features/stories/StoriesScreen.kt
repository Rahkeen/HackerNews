package dev.supergooey.hackernews.features.stories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.supergooey.hackernews.R
import dev.supergooey.hackernews.data.Item
import dev.supergooey.hackernews.features.comments.CommentsDestinations
import dev.supergooey.hackernews.ui.theme.HNOrange
import dev.supergooey.hackernews.ui.theme.HackerNewsTheme

@Composable
fun StoriesScreen(
  modifier: Modifier = Modifier,
  state: StoriesState,
  actions: (StoriesAction) -> Unit,
  navigation: (StoriesNavigation) -> Unit
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    TitleDisplay()
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      items(state.stories) { item ->
        StoryRow(
          item = item,
          onClick = {
            actions(StoriesAction.SelectStory(it.id))
            navigation(
              StoriesNavigation.GoToStory(
                closeup = StoriesDestinations.Closeup(it.url!!)
              )
            )
          },
          onCommentClicked = {
            actions(StoriesAction.SelectComments(it.id))
            navigation(
              StoriesNavigation.GoToComments(
                comments = CommentsDestinations.Comments(it.id)
              )
            )
          }
        )
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
        start = Offset(0f, size.height - 10),
        end = Offset(size.width, size.height - 10),
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
            type = "story",
            descendants = 0,
            kids = emptyList()
          ),
          Item(
            id = 1L,
            title = "Nice to Meet You",
            by = "vasant",
            score = 5,
            type = "story",
            descendants = 0,
            kids = emptyList()
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
        descendants = 5,
        kids = emptyList()
      ),
      onClick = {},
      onCommentClicked = {}
    )
  }
}

@Composable
fun StoryRow(
  item: Item,
  onClick: (Item) -> Unit,
  onCommentClicked: (Item) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 80.dp)
      .background(color = MaterialTheme.colorScheme.background)
      .clickable {
        onClick(item)
      }
      .padding(8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally)
  ) {
    Column(
      modifier = Modifier
        .fillMaxHeight()
        .weight(1f),
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = item.title ?: "",
        style = MaterialTheme.typography.titleSmall
      )
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "${item.score}", style = MaterialTheme.typography.labelSmall)
        Text(text = "•", style = MaterialTheme.typography.labelSmall)
        Text(
          text = item.by ?: "",
          color = HNOrange,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Medium
        )
      }
    }

    Column(
      modifier = Modifier
        .wrapContentWidth()
        .fillMaxHeight()
        .padding(hor = 16.dp)
        .clickable {
          onCommentClicked(item)
        },
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        modifier = Modifier.size(20.dp),
        painter = painterResource(R.drawable.ic_chat),
        tint = MaterialTheme.colorScheme.onBackground,
        contentDescription = ""
      )
      Text(
        text = "${item.descendants}",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
