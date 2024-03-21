package api.model.auth


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    @SerialName("code")
    val code: Int,
    @SerialName("msg")
    val msg: String,
    @SerialName("token")
    val token: String
)