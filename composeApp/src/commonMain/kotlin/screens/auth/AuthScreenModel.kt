package screens.auth

import api.model.auth.AuthRequest
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import service.AiDevs2Service
import util.mvi.BaseViewModel

class AuthScreenModel(private val aiDevs2Service: AiDevs2Service) :
    BaseViewModel<AuthContract.Event, AuthContract.State, AuthContract.Effect>() {
    override fun createInitialState(): AuthContract.State = AuthContract.State()

    override fun handleEvent(event: AuthContract.Event) {
        when (event) {
            AuthContract.Event.Init -> {}
            is AuthContract.Event.TokenUpdated -> {
                setState {
                    copy(token = event.token)
                }
            }

            is AuthContract.Event.TaskUpdated -> {
                setState {
                    copy(task = event.task)
                }
            }

            AuthContract.Event.SubmitToken -> {
                val task = currentState.task
                val token = currentState.token
                if (task != null && token != null) {
                    screenModelScope.launch {

                        val tokenForTask = aiDevs2Service.auth(
                            task.taskName,
                            AuthRequest(token!!)
                        )
                        setState {
                            copy(response = tokenForTask.toString())
                        }
                        setEffect {
                            AuthContract.Effect.Authed(task, tokenForTask.token)
                        }
                    }
                }
            }
        }
    }
}