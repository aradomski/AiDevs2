package api.aidevs.model.unknow.search


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UnknowNews(
    @SerialName("date")
    val date: String,
    @SerialName("info")
    val info: String,
    @SerialName("title")
    val title: String,
    @SerialName("url")
    val url: String
)