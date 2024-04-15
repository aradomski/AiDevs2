package service

import api.aidevs.AiDevs2Api
import api.aidevs.model.answer.AnswerRequest
import api.aidevs.model.auth.AuthRequest
import api.aidevs.model.task.TaskResponses


class AiDevs2Service(val aiDevs2Api: AiDevs2Api) {
    suspend fun auth(taskName: String, authRequest: AuthRequest) =
        aiDevs2Api.auth(taskName, authRequest)

    suspend inline fun <reified T : TaskResponses> getTask(token: String): T {
        return aiDevs2Api.getTask(token)
    }

    suspend inline fun <reified T : TaskResponses> getTask(token: String, question: String): T {
        return aiDevs2Api.getTask(token, question)
    }

    suspend fun answer(token: String, answerRequest: AnswerRequest) =
        aiDevs2Api.answer(token, answerRequest)
}