package api.model.unknow.people


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonWithMetadata(
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
    val wiek: Int,
    @SerialName("uuid")
    val uuid: Int,
    @SerialName("source")
    val source: String,
    @SerialName("vector")
    val vector: List<Double>,
) {
    companion object {
        fun fromPerson(person: Person, id: Int, vector: List<Double>): PersonWithMetadata {
            return PersonWithMetadata(
                imie = person.imie,
                nazwisko = person.nazwisko,
                oMnie = person.oMnie,
                ulubionaPostacZKapitanaBomby = person.ulubionaPostacZKapitanaBomby,
                ulubionyFilm = person.ulubionyFilm,
                ulubionyKolor = person.ulubionyKolor,
                ulubionySerial = person.ulubionySerial,
                wiek = person.wiek,
                uuid = id,
                source = "people",
                vector = vector
            )
        }
    }
}