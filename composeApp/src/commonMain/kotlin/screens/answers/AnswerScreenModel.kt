package screens.answers

import Task
import api.model.task.TaskResponses
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import service.TaskSolverService
import util.mvi.BaseViewModel

class AnswerScreenModel(
    private val task: Task,
    private val token: String,
    private val response: TaskResponses,
    private val taskSolverService: TaskSolverService,
) : BaseViewModel<AnswerContract.Event, AnswerContract.State, AnswerContract.Effect>() {
    override fun createInitialState(): AnswerContract.State = AnswerContract.State()

    override fun handleEvent(event: AnswerContract.Event) {
        when (event) {
            AnswerContract.Event.Init -> setState {
                copy(
                    token = this@AnswerScreenModel.token,
                    task = this@AnswerScreenModel.task,
                    taskResponse = this@AnswerScreenModel.response
                )
            }

            AnswerContract.Event.LaunchSolution -> {
                setState {
                    copy(isLoading = true)
                }
                screenModelScope.launch {
                    val answer = taskSolverService.solve(token, task, response)
                    setState {
                        copy(
                            answerRequest = answer.first,
                            answerResponse = answer.second,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}