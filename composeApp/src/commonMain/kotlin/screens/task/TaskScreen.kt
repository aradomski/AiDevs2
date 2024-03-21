package screens.task

import Task
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import org.koin.core.parameter.parameterArrayOf

class TaskScreen(private val task: Task, private val token: String) : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<TaskScreenModel> {
            parameterArrayOf(task, token)
        }
        LaunchedEffect(Unit) {
            viewModel.setEvent(TaskContract.Event.Init)
        }
        TaskContent(
            state = viewModel.uiState.collectAsState().value,
            effectFlow = viewModel.effect,
            onEventSent = { event -> viewModel.setEvent(event) }
        )
    }
}