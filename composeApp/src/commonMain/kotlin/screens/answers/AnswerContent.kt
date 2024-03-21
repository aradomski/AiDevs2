package screens.answers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import kotlinx.coroutines.flow.Flow

@Composable
fun AnswerContent(
    state: AnswerContract.State,
    effectFlow: Flow<AnswerContract.Effect>,
    onEventSent: (event: AnswerContract.Event) -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow
    Scaffold(
        topBar = {
            TopAppBar {
                Icon(
                    modifier = Modifier.clickable { navigator.popUntilRoot() },
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = null
                )
                Text("Back")
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
                item {
                    LaunchButton(onEventSent)
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
}

@Composable
fun AnswerRequestView(answerRequest: AnswerRequest?) {
    Text("Data sent to  AiDevs:", style = TextStyle(fontSize = 20.sp, color = Color.Red))
    Text("$answerRequest")
}