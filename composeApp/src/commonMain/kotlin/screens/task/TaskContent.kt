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
import composables.AiDevsAppBar
import kotlinx.coroutines.flow.Flow
import screens.answers.AnswerScreen

@Composable
fun TaskContent(
    state: TaskContract.State,
    effectFlow: Flow<TaskContract.Effect>,
    onEventSent: (event: TaskContract.Event) -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow
    Scaffold(topBar = {
        AiDevsAppBar{
            navigator.pop()
        }
    }) {
        Column(modifier = Modifier.padding(16.dp).padding(it)) {
            OutlinedButton(onClick = { onEventSent(TaskContract.Event.GrabTask) }) {
                Text("Grab task content")
            }
            AnimatedVisibility(state.taskContent != null) {
                Column {
                    when (state.taskContent) {
                        is TaskResponses.HelloApiResponse -> HelloApi(
                            state, state.taskContent
                        )

                        is TaskResponses.BloggerResponse -> Blogger(
                            state, state.taskContent
                        )

                        is TaskResponses.ModerationResponse -> Moderation(
                            state, state.taskContent
                        )

                        is TaskResponses.LiarResponse -> Liar(state, state.taskContent, navigator)
                        is TaskResponses.LiarResponseForQuestion -> {/*TODO*/
                        }

                        null -> Text("no task content yet")

                    }

                    state.taskContent?.let {
                        OutlinedButton(onClick = {
                            navigator.push(AnswerScreen(state.task!!, state.token!!, it))
                        }) {
                            Text("Solve task")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Liar(
    state: TaskContract.State, taskContent: TaskResponses.LiarResponse, navigator: Navigator
) {
    Text("Code: ${taskContent.code}")
    Text("Message: ${taskContent.msg}")
    Text("Hints: ")
    Text(taskContent.hint1)
    Text(taskContent.hint2)
    Text(taskContent.hint3)
}

@Composable
fun Moderation(
    state: TaskContract.State, taskContent: TaskResponses.ModerationResponse
) {
    Text("Code: ${taskContent.code}")
    Text("Message: ${taskContent.msg}")
    Text("Input: ")
    taskContent.input.forEach { Text(it) }
}

@Composable
fun Blogger(
    state: TaskContract.State, taskContent: TaskResponses.BloggerResponse
) {
    Text("Code: ${taskContent.code}")
    Text("Message: ${taskContent.msg}")
    Text("Blog:")
    taskContent.blog.forEach { Text(it) }
}

@Composable
fun HelloApi(
    state: TaskContract.State, taskContent: TaskResponses.HelloApiResponse
) {
    Text("Code: ${taskContent.code}")
    Text("Message: ${taskContent.msg}")
    Text("Cookie: ${taskContent.cookie}")
}
