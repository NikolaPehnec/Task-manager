package hr.fer.projekt.taskmanager.model


data class Zadatak(
    val id: String,
    val naziv: String,
    val opis: String,
    val status: String,
    val popis: String,
    val vrijeme: String,
    val datum: String?,
    val grupa: Grupa?,
    var ponavljajuciZadatak: Boolean = false,
    var aktualniZadatak: Boolean = true,
    var napomena: String? = null,
    var daniPonavljanja: String? = null,

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Zadatak

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}



