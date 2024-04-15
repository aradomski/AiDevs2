package api.renderform

import RENDER_FORM_API_KEY
import api.renderform.model.RenderRequest
import api.renderform.model.RenderResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RenderfromApi(private val httpClient: HttpClient, private val host: String) {


    suspend fun render(renderRequest: RenderRequest) =
        httpClient.post("$host/api/v2/render") {
            url {
                parameters.append("output", "json")
            }
            headers.append("x-api-key", RENDER_FORM_API_KEY)
            contentType(ContentType.Application.Json)
            setBody(renderRequest)
        }.body<RenderResponse>()
}