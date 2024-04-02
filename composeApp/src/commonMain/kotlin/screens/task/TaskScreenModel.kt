package screens.task

import Task
import api.model.task.TaskResponses
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import service.AiDevs2Service
import util.mvi.BaseViewModel

class TaskScreenModel(
    private val task: Task,
    private val token: String,
    private val aiDevs2Service: AiDevs2Service
) : BaseViewModel<TaskContract.Event, TaskContract.State, TaskContract.Effect>() {

    override fun createInitialState(): TaskContract.State =
        TaskContract.State()

    override fun handleEvent(event: TaskContract.Event) {
        when (event) {
            TaskContract.Event.GrabTask -> {
                screenModelScope.launch {
                    when (task) {
                        Task.HELLO_API -> getTaskContent<TaskResponses.HelloApiResponse>()
                        Task.MODERATION -> getTaskContent<TaskResponses.ModerationResponse>()
                        Task.BLOGGER -> getTaskContent<TaskResponses.BloggerResponse>()
                        Task.LIAR -> getTaskContent<TaskResponses.LiarResponse>()
                        Task.INPROMPT -> getTaskContent<TaskResponses.InpromptResponse>()
                        Task.EMBEDDING -> getTaskContent<TaskResponses.EmbeddingResponse>()
                        Task.WHISPER -> getTaskContent<TaskResponses.WhisperResponse>()
                        Task.FUNCTIONS -> getTaskContent<TaskResponses.FunctionsResponse>()
                        Task.RODO -> getTaskContent<TaskResponses.RodoResponse>()
                        Task.SCRAPER -> getTaskContent<TaskResponses.ScraperResponse>()
                    }
                }
            }

            TaskContract.Event.Init -> {
                setState {
                    copy(token = this@TaskScreenModel.token, task = this@TaskScreenModel.task)
                }
            }
        }
    }

    private suspend inline fun <reified T : TaskResponses> getTaskContent() {
        val helloApiResponse = aiDevs2Service.getTask<T>(token)
        setState {
            copy(taskContent = helloApiResponse)
        }
    }
}