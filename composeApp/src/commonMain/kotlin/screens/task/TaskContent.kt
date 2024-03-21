package screens.task

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import api.model.task.TaskResponses
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.Flow
import screens.answers.AnswerScreen

@Composable
fun TaskContent(
    state: TaskContract.State,
    effectFlow: Flow<TaskContract.Effect>,
    onEventSent: (event: TaskContract.Event) -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow
    Scaffold(
        topBar = {
            TopAppBar {
                Icon(
                    modifier = Modifier.clickable { navigator.pop() },
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = null
                )
                Text("Back")
            }
        }
    ) {
        Column(modifier = Modifier.padding(16.dp).padding(it)) {
            OutlinedButton(onClick = { onEventSent(TaskContract.Event.GrabTask) }) {
                Text("Grab task content")
            }

            AnimatedVisibility(state.taskContent != null) {
                when (state.taskContent) {
                    is TaskResponses.HelloApiResponse -> HelloApi(
                        state,
                        state.taskContent,
                        navigator
                    )

                    is TaskResponses.BloggerResponse -> Blogger(state, state.taskContent, navigator)
                    is TaskResponses.ModerationResponse -> Moderation(
                        state,
                        state.taskContent,
                        navigator
                    )

                    null -> Text("no task content yet")

                }
            }
        }
    }
}

@Composable
fun Moderation(
    state: TaskContract.State,
    taskContent: TaskResponses.ModerationResponse,
    navigator: Navigator
) {
    Column {
        Text("Code: ${taskContent.code}")
        Text("Message: ${taskContent.msg}")
        Text("Input: ")
        taskContent.input.forEach { Text(it) }
        OutlinedButton(onClick = {
            navigator.push(AnswerScreen(state.task!!, state.token!!, taskContent))
        }) {
            Text("Solve task")
        }
    }
}

@Composable
fun Blogger(
    state: TaskContract.State,
    taskContent: TaskResponses.BloggerResponse,
    navigator: Navigator
) {
    Column {
        Text("Code: ${taskContent.code}")
        Text("Message: ${taskContent.msg}")
        Text("Blog:")
        taskContent.blog.forEach { Text(it) }
        OutlinedButton(onClick = {
            navigator.push(AnswerScreen(state.task!!, state.token!!, taskContent))
        }) {
            Text("Solve task")
        }
    }
}

@Composable
fun HelloApi(
    state: TaskContract.State,
    taskContent: TaskResponses.HelloApiResponse,
    navigator: Navigator
) {
    Column {
        Text("Code: ${taskContent.code}")
        Text("Message: ${taskContent.msg}")
        Text("Cookie: ${taskContent.cookie}")
        OutlinedButton(onClick = {
            navigator.push(AnswerScreen(state.task!!, state.token!!, taskContent))
        }) {
            Text("Solve task")
        }
    }
}
