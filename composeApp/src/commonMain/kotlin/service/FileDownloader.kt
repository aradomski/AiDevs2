package service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class FileDownloader(private val httpClient: HttpClient) {

    suspend fun downloadFile(url: String): ByteArray {
        return httpClient.get(url).body()
    }

    suspend fun downloadText(url: String): String {
        return httpClient.get(url).bodyAsText()
    }
}