package api.model.answer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class AnswerRequest {
    @Serializable
    data class HelloApi(
        @SerialName("answer")
        val answer: String
    ) : AnswerRequest()

    @Serializable
    data class Moderation(
        @SerialName("answer")
        val answer: List<Int>
    ) : AnswerRequest()

    @Serializable
    data class Blogger(
        @SerialName("answer")
        val answer: List<String>
    ) : AnswerRequest()

    @Serializable
    data class Liar(
        @SerialName("answer")
        val answer: String
    ) : AnswerRequest()
}


