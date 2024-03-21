package service

import api.AiDevs2Api
import api.model.answer.AnswerRequest
import api.model.auth.AuthRequest
import api.model.task.TaskResponses


class AiDevs2Service(val aiDevs2Api: AiDevs2Api)  {
     suspend fun auth(taskName: String, authRequest: AuthRequest) =
        aiDevs2Api.auth(taskName, authRequest)

     suspend inline fun <reified T : TaskResponses> getTask(token: String): T {
        return aiDevs2Api.getTask(token)
    }

     suspend fun answer(token: String, answerRequest: AnswerRequest) =
        aiDevs2Api.answer(token, answerRequest)
}