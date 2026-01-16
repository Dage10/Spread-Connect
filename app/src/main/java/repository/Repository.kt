package repository

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import models.Area
import models.Presentacio
import models.Usuari
import java.security.MessageDigest

class Repository {

    private fun sha256(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(text.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) } }
    suspend fun registreUsuari(nomUsuari: String, email: String, contrasenya: String): Usuari {
        val nouUsuari = mapOf(
            "nom_usuari" to nomUsuari,
            "email" to email,
            "contrasenya_hash" to sha256(contrasenya)
        )
        return try {
            SupabaseClient.client
                .from("usuaris")
                .insert(nouUsuari) {
                    select()
                    single()
                }
                .decodeSingle()
        }catch (e: Exception){
            throw Exception("Aquest usuari o email ja existeix")
        }
    }

    suspend fun loginUsuari(nomUsuari: String, contrasenya: String): Usuari {

        val hash = sha256(contrasenya)

        val usuaris = SupabaseClient.client
            .from("usuaris")
            .select {
                filter {
                    eq("nom_usuari", nomUsuari)
                    eq("contrasenya_hash", hash)
                }
            }
            .decodeList<Usuari>()

        return usuaris.firstOrNull() ?: throw Exception("Credencials incorrectes")
    }


    suspend fun getArees(): List<Area> =
        SupabaseClient.client.from("areas").select().decodeList()

    suspend fun getPresentacionsPerArea(areaId: String): List<Presentacio> =
        SupabaseClient.client
            .from("presentacions")
            .select {
                filter { eq("area_id", areaId) }
            }
            .decodeList()

    suspend fun crearPresentacio(
        idUsuari: String,
        titol: String,
        contingut: String,
        areaId: String,
        imatgeUrl: String? = null
    ): Presentacio {
        val nova = mapOf(
            "id_usuari" to idUsuari,
            "titol" to titol,
            "contingut_presentacio" to contingut,
            "area_id" to areaId,
            "imatge_url" to imatgeUrl
        )
        return SupabaseClient.client
            .from("presentacions")
            .insert(nova) {
                select()
                single()
            }
            .decodeSingle()
    }
}
