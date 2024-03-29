package api.model.answer

import com.aallam.openai.api.chat.FunctionTool
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

    @Serializable
    data class Inprompt(
        @SerialName("answer")
        val answer: String
    ) : AnswerRequest()


    @Serializable
    data class Embedding(
        @SerialName("answer")
        val answer: List<Double>
    ) : AnswerRequest()


    @Serializable
    data class Whisper(
        @SerialName("answer")
        val answer: String
    ) : AnswerRequest()

    @Serializable
    data class Functions(
        @SerialName("answer")
        val answer: FunctionTool
    ) : AnswerRequest()

    @Serializable
    data class Rodo(
        @SerialName("answer")
        val answer: String
    ) : AnswerRequest()
}


