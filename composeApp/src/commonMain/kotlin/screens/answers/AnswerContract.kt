package screens.answers

import Task
import api.aidevs.model.answer.AnswerRequest
import api.aidevs.model.answer.AnswerResponse
import api.aidevs.model.task.TaskResponses
import service.IntermediateData
import util.mvi.UiEffect
import util.mvi.UiEvent
import util.mvi.UiState

interface AnswerContract {
    sealed interface Event : UiEvent {
        data object Init : Event
        data object LaunchSolution : Event
        data class QuestionUpdated(val question: String) : Event
    }

    data class State(
        val token: String? = null,
        val task: Task? = null,
        val taskResponse: TaskResponses? = null,
        val answerRequest: AnswerRequest? = null,
        val answerResponse: AnswerResponse? = null,
        val intermediateData: IntermediateData? = null,
        val isLoading: Boolean = false,
        val question: String = "What is highest-grossing film ever at the time of its release in 1993?",
    ) : UiState


    sealed interface Effect : UiEffect {

    }
}