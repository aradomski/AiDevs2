package service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

class FileDownloader(val httpClient: HttpClient, val json: Json,val jsonHttpClient: HttpClient) {

    suspend fun downloadFile(url: String): ByteArray {
        return httpClient.get(url).body()
    }

    suspend fun downloadText(url: String): String {
        return httpClient.get(url).bodyAsText()
    }

    suspend inline fun <reified T> downloadJson(url: String): T {
        val bodyAsText = httpClient.get(url).bodyAsText()
        return json.decodeFromString(bodyAsText)
    }


}