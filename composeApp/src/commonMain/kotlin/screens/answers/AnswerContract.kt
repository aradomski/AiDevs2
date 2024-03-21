package screens.answers

import Task
import api.model.answer.AnswerRequest
import api.model.answer.AnswerResponse
import api.model.task.TaskResponses
import util.mvi.UiEffect
import util.mvi.UiEvent
import util.mvi.UiState

interface AnswerContract {
    sealed interface Event : UiEvent {
        data object Init : Event
        data object LaunchSolution : Event
    }

    data class State(
        val token: String? = null,
        val task: Task? = null,
        val taskResponse: TaskResponses? = null,
        val answerRequest: AnswerRequest? = null,
        val answerResponse: AnswerResponse? = null,
        val isLoading: Boolean = false,
    ) : UiState


    sealed interface Effect : UiEffect {

    }
}