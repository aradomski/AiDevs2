package api.renderform.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    @SerialName("title.text")
    val title: String,
    @SerialName("image.src")
    val image: String
)