package api.model.answer


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnswerResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("msg")
    val msg: String,
    @SerialName("note")
    val note: String? = null
)