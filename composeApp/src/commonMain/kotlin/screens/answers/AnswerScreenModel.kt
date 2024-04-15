package screens.answers

import Task
import api.aidevs.model.task.TaskResponses
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import service.ExtraTaskPayload
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
                    val taskPayload = when (task) {
                        Task.LIAR -> ExtraTaskPayload.Liar(currentState.question)
                        else -> null
                    }
                    val answer = taskSolverService.solve(token, task, response, taskPayload)
                    setState {
                        copy(
                            answerRequest = answer.answerRequest,
                            answerResponse = answer.answerResponse,
                            intermediateData = answer.intermediateData,
                            isLoading = false
                        )
                    }
                }
            }

            is AnswerContract.Event.QuestionUpdated -> setState {
                copy(question = event.question)
            }
        }
    }
}