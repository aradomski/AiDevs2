package api.renderform.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RenderResponse(
    @SerialName("href")
    val href: String,
    @SerialName("requestId")
    val requestId: String
)