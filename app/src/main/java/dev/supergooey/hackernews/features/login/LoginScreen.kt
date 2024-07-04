package dev.supergooey.hackernews.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.supergooey.hackernews.data.HackerNewsWebClient
import dev.supergooey.hackernews.ui.theme.HackerNewsTheme
import dev.supergooey.hackernews.webClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Serializable
data object Login

@Composable
fun LoginScreen() {
  val context = LocalContext.current
  val model = viewModel<LoginViewModel>(
    factory = LoginViewModel.Factory(
      webClient = context.webClient()
    )
  )
  val state by model.state.collectAsState()

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    TextField(
      value = state.username,
      placeholder = { Text("Username") },
      onValueChange = { model.actions(LoginAction.UpdateUsername(it)) }
    )
    TextField(
      value = state.password,
      placeholder = { Text("Password") },
      onValueChange = { model.actions(LoginAction.UpdatePassword(it)) },
    )

    Button(onClick = { model.actions(LoginAction.LoginTapped) }) {
      Text("Login")
    }
  }
}

@Preview
@Composable
private fun LoginScreenPreview() {
  HackerNewsTheme {
    LoginScreen()
  }
}

data class LoginState(
  val username: String,
  val password: String
) {
  companion object {
    val empty = LoginState("heyrikin", "SSJ4barcelona2024")
  }
}

sealed interface LoginAction {
  data class UpdateUsername(val update: String) : LoginAction
  data class UpdatePassword(val update: String) : LoginAction
  data object LoginTapped : LoginAction
}

class LoginViewModel(private val webClient: HackerNewsWebClient) : ViewModel() {
  private val internalState = MutableStateFlow(LoginState.empty)
  val state = internalState.asStateFlow()

  @Suppress("UNCHECKED_CAST")
  class Factory(private val webClient: HackerNewsWebClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return LoginViewModel(webClient) as T
    }
  }

  fun actions(action: LoginAction) {
    when (action) {
      LoginAction.LoginTapped -> {
        viewModelScope.launch(Dispatchers.IO) {
          webClient.login(
            username = internalState.value.username,
            password = internalState.value.password
          )
        }
      }

      is LoginAction.UpdatePassword -> {
        internalState.update { current ->
          current.copy(password = action.update)
        }
      }

      is LoginAction.UpdateUsername -> {
        internalState.update { current ->
          current.copy(username = action.update)
        }
      }
    }
  }
}