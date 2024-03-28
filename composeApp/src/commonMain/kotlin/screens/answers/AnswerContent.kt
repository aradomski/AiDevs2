package screens.answers

import Task
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import api.model.answer.AnswerRequest
import api.model.answer.AnswerResponse
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import composables.AiDevsAppBar
import kotlinx.coroutines.flow.Flow
import service.IntermediateData

@Composable
fun AnswerContent(
    state: AnswerContract.State,
    effectFlow: Flow<AnswerContract.Effect>,
    onEventSent: (event: AnswerContract.Event) -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow
    Scaffold(
        topBar = {
            AiDevsAppBar {
                navigator.popUntilRoot()
            }
        }
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(16.dp).padding(it)
            ) {
                when (state.task) {
                    Task.LIAR -> {
                        item {
                            OutlinedTextField(state.question, onValueChange = {
                                onEventSent(AnswerContract.Event.QuestionUpdated(it))
                            })
                        }
                    }

                    else -> {}
                }

                item {
                    LaunchButton(onEventSent)
                }
                if (state.intermediateData != null) {
                    item {
                        IntermediateDataView(state.intermediateData)
                    }
                }
                if (state.answerResponse != null) {
                    item { AnswerResponseView(state.answerResponse) }
                }
                if (state.answerRequest != null) {
                    item { AnswerRequestView(state.answerRequest) }
                }

            }
        }
    }
}

@Composable
fun IntermediateDataView(intermediateData: IntermediateData) {
    when (intermediateData) {
        is IntermediateData.LiarIntermediateData -> {
            Text(intermediateData.question)
            Text(intermediateData.answer.toString())
        }

        is IntermediateData.InpromptIntermediateData -> {
            Text(intermediateData.foundName)
        }

        is IntermediateData.EmbeddingIntermediateData -> {
            intermediateData.embeddingResponse.embeddings.forEach {
                Text(it.toString())
            }
        }

        is IntermediateData.WhisperIntermediateData -> {
            Text(intermediateData.transcription.duration.toString())
            Text(intermediateData.transcription.language.toString())
            Text(intermediateData.transcription.text)
        }
    }
}

@Composable
fun LaunchButton(onEventSent: (event: AnswerContract.Event) -> Unit) {
    OutlinedButton(onClick = {
        onEventSent(AnswerContract.Event.LaunchSolution)
    }) {
        Text("Perform solving the task")
    }
}

@Composable
fun AnswerResponseView(answerResponse: AnswerResponse?) {
    Text("AiDevs response:", style = TextStyle(fontSize = 20.sp, color = Color.Red))
    Text("$answerResponse")
    AnimatedVisibility(answerResponse?.note == "CORRECT") {
        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Green)
    }
}

@Composable
fun AnswerRequestView(answerRequest: AnswerRequest?) {
    Text("Data sent to  AiDevs:", style = TextStyle(fontSize = 20.sp, color = Color.Red))
    Text("$answerRequest")
}