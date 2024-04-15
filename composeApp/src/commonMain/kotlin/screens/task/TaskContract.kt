package screens.task

import Task
import api.aidevs.model.task.TaskResponses
import util.mvi.UiEffect
import util.mvi.UiEvent
import util.mvi.UiState

interface TaskContract {
    sealed interface Event : UiEvent {
        data object Init : Event
        data object GrabTask : Event
    }

    data class State(
        val task: Task? = null,
        val token: String? = null,
        val taskContent: TaskResponses? = null,
        val proceedToNextScreen:Boolean=false
    ) : UiState


    sealed interface Effect : UiEffect
}