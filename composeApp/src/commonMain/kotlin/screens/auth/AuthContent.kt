package screens.auth

import Task
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import api.model.task.TaskResponses
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import screens.answers.AnswerScreen
import screens.task.TaskScreen

@Composable
fun AuthContent(
    state: AuthContract.State,
    effectFlow: Flow<AuthContract.Effect>,
    onEventSent: (event: AuthContract.Event) -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow

    if (state.goDirectlyToTaskSolving) {
        when(state.task){
            Task.WHOAMI ->  navigator.push(AnswerScreen(state.task!!, state.token!!, TaskResponses.EmptyWhoamiResponse))
            Task.SEARCH ->  navigator.push(AnswerScreen(state.task!!, state.token!!, TaskResponses.EmptySearchResponse))
            else -> TODO()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(value = state.token ?: "", onValueChange = {
            onEventSent(AuthContract.Event.TokenUpdated(it))
        }, label = {
            Text("Input your token")
        })
        LazyVerticalGrid(
            modifier = Modifier.padding(vertical = 16.dp).weight(1f),
            columns = GridCells.Adaptive(128.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Task.entries) { task ->
                Card(modifier = Modifier.height(128.dp), onClick = {
                    onEventSent(AuthContract.Event.TaskUpdated(task))
                }) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            modifier = Modifier.padding(16.dp).align(Alignment.Center),
                            text = task.taskName.capitalize(Locale.current),
                            style = TextStyle(fontSize = 20.sp),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        if (state.task == task) {
                            Icon(
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                                imageVector = Icons.Default.Check,
                                tint = Color.Green,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }

        OutlinedButton(onClick = {
            onEventSent(AuthContract.Event.SubmitToken)
        }, enabled = state.token?.isNotBlank() == true && state.task != null) {
            Text("Submit")
        }
        AnimatedVisibility(state.response != null) {
            Text("${state.response}")
        }
        val composableScope = rememberCoroutineScope()
        composableScope.launch {
            effectFlow.collect {
                when (it) {
                    is AuthContract.Effect.Authed -> {
                        navigator.push(TaskScreen(it.task, it.token))
                    }
                }
            }
        }
    }
}