package api.aidevs.model.unknow.people


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Person(
    @SerialName("imie")
    val imie: String,
    @SerialName("nazwisko")
    val nazwisko: String,
    @SerialName("o_mnie")
    val oMnie: String,
    @SerialName("ulubiona_postac_z_kapitana_bomby")
    val ulubionaPostacZKapitanaBomby: String,
    @SerialName("ulubiony_film")
    val ulubionyFilm: String,
    @SerialName("ulubiony_kolor")
    val ulubionyKolor: String,
    @SerialName("ulubiony_serial")
    val ulubionySerial: String,
    @SerialName("wiek")
    val wiek: Int
)