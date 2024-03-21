package api

import api.model.answer.AnswerRequest
import api.model.answer.AnswerResponse
import api.model.task.TaskResponses
import api.model.auth.AuthRequest
import api.model.auth.AuthResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AiDevs2Api(val httpClient: HttpClient, val host: String) {

    suspend fun auth(taskName: String, authRequest: AuthRequest) =
        httpClient.post("$host/token/$taskName") {
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }.body<AuthResponse>()


    suspend inline fun <reified T : TaskResponses> getTask(token: String) =
        httpClient.get("$host/task/$token") {
            contentType(ContentType.Application.Json)
        }.body<T>()

    suspend fun answer(token: String, answerRequest: AnswerRequest) =
        httpClient.post("$host/answer/$token") {
            contentType(ContentType.Application.Json)
            setBody(answerRequest)
        }.body<AnswerResponse>()
}