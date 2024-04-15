package api.renderform.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RenderRequest(
//    @SerialName("batchName")
//    val batchName: String,
    @SerialName("data")
    val `data`: Data,
//    @SerialName("fileName")
//    val fileName: String,
//    @SerialName("metadata")
//    val metadata: Metadata,
    @SerialName("template")
    val template: String,
//    @SerialName("version")
//    val version: String,
//    @SerialName("webhookUrl")
//    val webhookUrl: String
)