package screens.task

import Task
import api.aidevs.model.task.TaskResponses
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
                        Task.WHOAMI -> {
                            throw IllegalStateException("This task is time dependent so it will be solved in next screen ")
                        }

                        Task.SEARCH -> throw IllegalStateException("This task will be solved in next screen ")
                        Task.PEOPLE -> getTaskContent<TaskResponses.PeopleResponse>()
                        Task.KNOWLEDGE -> getTaskContent<TaskResponses.KnowledgeResponse>()
                        Task.TOOLS -> getTaskContent<TaskResponses.ToolsResponse>()
                        Task.GNOME -> getTaskContent<TaskResponses.GnomeResponse>()
                        Task.OWNAPI -> getTaskContent<TaskResponses.OwnApiResponse>()
                        Task.OWNAPIPRO -> getTaskContent<TaskResponses.OwnApiResponse>()
                        Task.MEME -> getTaskContent<TaskResponses.MemeResponse>()
                    }
                }
            }

            TaskContract.Event.Init -> {
                setState {
                    copy(
                        token = this@TaskScreenModel.token,
                        task = this@TaskScreenModel.task,
                        proceedToNextScreen = this@TaskScreenModel.task == Task.WHOAMI
                    )
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