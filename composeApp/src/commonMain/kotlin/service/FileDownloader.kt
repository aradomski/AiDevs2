package service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class FileDownloader(private val httpClient: HttpClient) {

    suspend fun downloadFile(url: String): ByteArray {
        return httpClient.get(url).body()
    }
}