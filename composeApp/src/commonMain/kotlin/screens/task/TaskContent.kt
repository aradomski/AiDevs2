package screens.task

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import api.model.task.TaskResponses
import cafe.adriel.voyager.navigator.LocalNavigator
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
        AiDevsAppBar {
            navigator.pop()
        }
    }) {
        Column(modifier = Modifier.padding(16.dp).padding(it)) {
            Row {
                AnimatedVisibility(state.taskContent == null) {
                    OutlinedButton(onClick = { onEventSent(TaskContract.Event.GrabTask) }) {
                        Text("Grab task content")
                    }
                }
                AnimatedVisibility(state.taskContent != null) {
                    state.taskContent?.let {
                        OutlinedButton(onClick = {
                            navigator.push(AnswerScreen(state.task!!, state.token!!, it))
                        }) {
                            Text("Solve task")
                        }
                    }
                }
            }
            AnimatedVisibility(state.taskContent != null) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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

                        is TaskResponses.LiarResponse -> Liar(state, state.taskContent)
                        is TaskResponses.LiarResponseForQuestion -> { //not displayed
                        }

                        is TaskResponses.InpromptResponse -> {
                            Inprompt(state, state.taskContent)
                        }

                        is TaskResponses.EmbeddingResponse -> Embedding(
                            state, state.taskContent
                        )

                        is TaskResponses.WhisperResponse -> Whisper(state, state.taskContent)
                        null -> Text("no task content yet")
                    }
                }
            }
        }
    }
}

@Composable
fun Whisper(state: TaskContract.State, taskContent: TaskResponses.WhisperResponse) {
    Text(taskContent.msg)
    Text(taskContent.hint)
}

@Composable
fun Embedding(state: TaskContract.State, taskContent: TaskResponses.EmbeddingResponse) {
    Text(taskContent.msg)
    Text(taskContent.hint1)
    Text(taskContent.hint2)
    Text(taskContent.hint3)
}

@Composable
fun Inprompt(state: TaskContract.State, taskContent: TaskResponses.InpromptResponse) {
    Text(taskContent.question, style = TextStyle(fontSize = 18.sp))
    taskContent.input.forEach {
        Text(it)
    }
}

@Composable
fun Liar(state: TaskContract.State, taskContent: TaskResponses.LiarResponse) {
    Text("Code: ${taskContent.code}")
    Text("Message: ${taskContent.msg}")
    Text("Hints: ")
    Text(taskContent.hint1)
    Text(taskContent.hint2)
    Text(taskContent.hint3)
}

@Composable
fun Moderation(state: TaskContract.State, taskContent: TaskResponses.ModerationResponse) {
    Text("Code: ${taskContent.code}")
    Text("Message: ${taskContent.msg}")
    Text("Input: ")
    taskContent.input.forEach { Text(it) }
}

@Composable
fun Blogger(state: TaskContract.State, taskContent: TaskResponses.BloggerResponse) {
    Text("Code: ${taskContent.code}")
    Text("Message: ${taskContent.msg}")
    Text("Blog:")
    taskContent.blog.forEach { Text(it) }
}

@Composable
fun HelloApi(state: TaskContract.State, taskContent: TaskResponses.HelloApiResponse) {
    Text("Code: ${taskContent.code}")
    Text("Message: ${taskContent.msg}")
    Text("Cookie: ${taskContent.cookie}")
}
