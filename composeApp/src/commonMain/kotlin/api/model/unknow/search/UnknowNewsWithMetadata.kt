package api.model.unknow.search


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UnknowNewsWithMetadata(
    @SerialName("date")
    val date: String,
    @SerialName("info")
    val info: String,
    @SerialName("title")
    val title: String,
    @SerialName("url")
    val url: String,
    @SerialName("uuid")
    val uuid: Int,
    @SerialName("source")
    val source: String,
    @SerialName("vector")
    val vector: List<Double>,
)