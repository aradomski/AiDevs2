package service

import Task
import api.model.answer.AnswerRequest
import api.model.answer.AnswerResponse
import api.model.task.TaskResponses
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.moderation.ModerationModel
import com.aallam.openai.api.moderation.moderationRequest
import com.aallam.openai.client.OpenAI

class TaskSolverService(
    private val openAI: OpenAI,
    private val aiDevs2Service: AiDevs2Service
) {
    suspend fun solve(
        token: String,
        task: Task,
        response: TaskResponses
    ): Pair<AnswerRequest, AnswerResponse> {
        return when (task) {
            Task.HELLO_API -> solveHelloApi(token, response as TaskResponses.HelloApiResponse)
            Task.MODERATION -> solveModeration(
                token,
                response as TaskResponses.ModerationResponse
            )

            Task.BLOGGER -> soleBlogger(token, task, response as TaskResponses.BloggerResponse)
        }
    }

    private suspend fun soleBlogger(
        token: String,
        task: Task,
        taskResponse: TaskResponses.BloggerResponse
    ): Pair<AnswerRequest, AnswerResponse> {
        val answers = taskResponse.blog.map {
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-3.5-turbo"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = "You are pizza maker blogger. Write blog posts based on its titles"
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = it
                    )
                )
            )
            openAI.chatCompletion(chatCompletionRequest)
        }.mapNotNull { it.choices[0].message.content }
        val answerRequest = AnswerRequest.Blogger(answers)
        return answerRequest to aiDevs2Service.answer(token, answerRequest)
    }

    private suspend fun solveModeration(
        token: String,
        taskResponse: TaskResponses.ModerationResponse
    ): Pair<AnswerRequest, AnswerResponse> {
        val moderationRequest = moderationRequest {
            input = taskResponse.input
            model = ModerationModel.Latest
        }
        val answers = openAI.moderations(moderationRequest).results.map {
            if (it.flagged) {
                1
            } else {
                0
            }
        }


        val answerRequest = AnswerRequest.Moderation(answers)
        return answerRequest to aiDevs2Service.answer(token, answerRequest)
    }

    private suspend fun solveHelloApi(
        token: String,
        taskResponse: TaskResponses.HelloApiResponse
    ): Pair<AnswerRequest, AnswerResponse> {
        val answerRequest = AnswerRequest.HelloApi(taskResponse.cookie)
        return answerRequest to aiDevs2Service.answer(token, answerRequest)
    }
}