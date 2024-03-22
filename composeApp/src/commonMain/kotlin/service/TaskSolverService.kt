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
        response: TaskResponses,
        taskPayload: ExtraTaskPayload? = null,
    ): SolvingData {
        return when (task) {
            Task.HELLO_API -> solveHelloApi(token, response as TaskResponses.HelloApiResponse)
            Task.MODERATION -> solveModeration(
                token,
                response as TaskResponses.ModerationResponse
            )

            Task.BLOGGER -> solveBlogger(token, task, response as TaskResponses.BloggerResponse)
            Task.LIAR -> solveLiar(
                token,
                task,
                response as TaskResponses.LiarResponse,
                taskPayload as ExtraTaskPayload.Liar
            )
        }
    }

    private suspend fun solveLiar(
        token: String,
        task: Task,
        liarResponse: TaskResponses.LiarResponse,
        taskPayload: ExtraTaskPayload.Liar
    ): SolvingData {
        val liarResponseForQuestion = aiDevs2Service.getTask<TaskResponses.LiarResponseForQuestion>(
            token,
            taskPayload.question
        )
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "You are judge that verifies if the given answer corresponds to the given question"
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = "Return only YES or NO whenever question: \n ${taskPayload.question} \n matches given answer: \n ${liarResponseForQuestion.answer}"
                )
            )
        )
        val chatCompletion = openAI.chatCompletion(chatCompletionRequest)

        val answerRequest = AnswerRequest.Liar(chatCompletion.choices[0].message.content ?: "NO")
        return SolvingData(
            answerRequest,
            aiDevs2Service.answer(token, answerRequest),
            IntermediateData.LiarIntermediateData(taskPayload.question, liarResponseForQuestion)
        )
    }

    private suspend fun solveBlogger(
        token: String,
        task: Task,
        taskResponse: TaskResponses.BloggerResponse
    ): SolvingData {
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
        return SolvingData(answerRequest, aiDevs2Service.answer(token, answerRequest))
    }

    private suspend fun solveModeration(
        token: String,
        taskResponse: TaskResponses.ModerationResponse
    ): SolvingData {
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
        return SolvingData(answerRequest, aiDevs2Service.answer(token, answerRequest))
    }

    private suspend fun solveHelloApi(
        token: String,
        taskResponse: TaskResponses.HelloApiResponse
    ): SolvingData {
        val answerRequest = AnswerRequest.HelloApi(taskResponse.cookie)
        return SolvingData(answerRequest, aiDevs2Service.answer(token, answerRequest))
    }
}

sealed interface ExtraTaskPayload {
    data class Liar(val question: String) :
        ExtraTaskPayload
}

sealed interface IntermediateData {
    data class LiarIntermediateData(
        val question: String,
        val answer: TaskResponses.LiarResponseForQuestion
    ) : IntermediateData
}

data class SolvingData(
    val answerRequest: AnswerRequest,
    val answerResponse: AnswerResponse,
    val intermediateData: IntermediateData? = null
)