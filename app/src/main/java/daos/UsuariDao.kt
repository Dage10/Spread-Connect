package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import models.Usuari
import java.security.MessageDigest

class UsuariDao {

    private fun sha256(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(text.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun registreUsuari(nomUsuari: String, email: String, contrasenya: String): Usuari {
        val existents = SupabaseClient.client
            .from("usuaris")
            .select { filter { eq("nom_usuari", nomUsuari) } }
            .decodeList<Usuari>()

        if (existents.isNotEmpty()) {
            throw Exception("L'usuari ja existeix")
        }

        val nouUsuari = mapOf(
            "nom_usuari" to nomUsuari,
            "email" to email,
            "contrasenya_hash" to sha256(contrasenya)
        )

        return try {
            SupabaseClient.client
                .from("usuaris")
                .insert(nouUsuari) { select() }
                .decodeList<Usuari>()
                .firstOrNull() ?: throw Exception("Error en crear l'usuari")
        } catch (e: Exception) {
            throw Exception("Error al registrar l'usuari: ${e.message}")
        }
    }

    suspend fun loginUsuari(nomUsuari: String, contrasenya: String): Usuari {
        val hash = sha256(contrasenya)

        return try {
            val usuaris = SupabaseClient.client
                .from("usuaris")
                .select {
                    filter {
                        eq("nom_usuari", nomUsuari)
                        eq("contrasenya_hash", hash)
                    }
                }
                .decodeList<Usuari>()

            usuaris.firstOrNull() ?: throw Exception("Credencials incorrectes")
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getUsuariPerId(id: String): Usuari {
        return try {
            SupabaseClient.client
                .from("usuaris")
                .select { filter { eq("id", id) } }
                .decodeList<Usuari>()
                .firstOrNull() ?: throw Exception("Usuari no trobat")
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun actualitzarPerfil(
        idUsuari: String,
        nomUsuari: String,
        email: String,
        descripcio: String?,
        novaContrasenya: String?,
        avatarUrl: String? = null
    ): Usuari {
        val data = buildJsonObject {
            put("nom_usuari", nomUsuari)
            put("email", email)
            if (descripcio != null) {
                put("descripcio", descripcio)
            }
            if (!novaContrasenya.isNullOrBlank()) {
                put("contrasenya_hash", sha256(novaContrasenya))
            }
            if (avatarUrl != null) {
                put("avatar_url", avatarUrl)
            }
        }

        return try {
            val usuariActualitzat = SupabaseClient.client
                .from("usuaris")
                .update(data) {
                    filter { eq("id", idUsuari) }
                    select()
                }
                .decodeList<Usuari>()
                .firstOrNull() ?: throw Exception("Error en actualitzar el perfil")
            usuariActualitzat
        } catch (e: Exception) {
            throw e
        }
    }
}
