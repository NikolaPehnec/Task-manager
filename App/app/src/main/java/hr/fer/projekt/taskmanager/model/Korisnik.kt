package hr.fer.projekt.taskmanager.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("korisnickoIme") val email: String,
    @SerializedName("lozinka") val lozinka: String
)

data class RegisterRequest(
    @SerializedName("lozinka") val lozinka: String,
    @SerializedName("korisnickoIme") val email: String,
    @SerializedName("ime") val ime: String,
    @SerializedName("prezime") val prezime: String,
    @SerializedName("uloga") val uloga: String,
)

data class Korisnik(
    @SerializedName("idKorisnik")
    val userID: Int? = null,

    @SerializedName("ime")
    var ime: String?,

    @SerializedName("prezime")
    var prezime: String?,

    @SerializedName("korisnickoIme")
    var korisnickoIme: String?,

    @SerializedName("lozinka")
    var lozinka: String?,

    @SerializedName("uloga")
    var uloga: String?
)

data class KorisnikLijekoviRequest(
    @SerializedName("id_korisnik") val idKorisnik: Int,
)
