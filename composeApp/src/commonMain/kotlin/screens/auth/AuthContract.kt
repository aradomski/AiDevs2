package screens.auth

import AI_DEVS_KEY
import Task
import util.mvi.UiEffect
import util.mvi.UiEvent
import util.mvi.UiState

interface AuthContract {
    sealed interface Event : UiEvent {
        data object Init : Event
        data class TokenUpdated(val token: String) : Event
        data class TaskUpdated(val task: Task) : Event
        data object SubmitToken : Event
    }

    data class State(
        val token: String? = AI_DEVS_KEY,
        val task: Task? = null,
        val response: String? = null,
        val goDirectlyToTaskSolving: Boolean = false,
    ) : UiState


    sealed interface Effect : UiEffect {
        data class Authed(val task: Task, val token: String) : Effect
    }
}