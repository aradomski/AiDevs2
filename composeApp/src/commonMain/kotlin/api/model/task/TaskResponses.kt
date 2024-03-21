package api.model.task

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class TaskResponses {

    @Serializable
    data class HelloApiResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("cookie")
        val cookie: String,
        @SerialName("msg")
        val msg: String
    ) : TaskResponses()

    @Serializable
    data class ModerationResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("input")
        val input: List<String>,
        @SerialName("msg")
        val msg: String
    ) : TaskResponses()

    @Serializable
    data class BloggerResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("blog")
        val blog: List<String>,
        @SerialName("msg")
        val msg: String
    ) : TaskResponses()
}