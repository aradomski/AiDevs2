package screens.answers

import Task
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import api.aidevs.model.task.TaskResponses
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import org.koin.core.parameter.parameterArrayOf

class AnswerScreen(
    private val task: Task,
    private val token: String,
    private val response: TaskResponses
) : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<AnswerScreenModel> {
            parameterArrayOf(task, token, response)
        }
        LaunchedEffect(Unit) {
            viewModel.setEvent(AnswerContract.Event.Init)
        }
        AnswerContent(
            state = viewModel.uiState.collectAsState().value,
            effectFlow = viewModel.effect,
            onEventSent = { event -> viewModel.setEvent(event) }
        )
    }
}