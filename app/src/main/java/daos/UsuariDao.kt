package daos

import conexio.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import models.Usuari
import java.time.Instant

class UsuariDao {

    suspend fun registreUsuari(nom: String, email: String, pass: String): String {
        val nomNet = nom.trim()

        try {
            SupabaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
                this.data = buildJsonObject {
                    put("nom_usuari", nomNet)
                    put("username", nomNet)
                    put("full_name", nomNet)
                }
            }

            val user = SupabaseClient.client.auth.currentUserOrNull()
                ?: throw Exception("No s'ha pogut obtenir l'usuari després del registre")

            return user.id

        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun loginUsuari(identificador: String, contrasenya: String): Usuari {
        val emailFinal = if (identificador.contains("@")) identificador else {
            SupabaseClient.client.from("usuaris")
                .select { filter { eq("nom_usuari", identificador) } }
                .decodeSingleOrNull<Usuari>()?.email ?: throw Exception("USUARI_NO_TROBAT")
        }

        SupabaseClient.client.auth.signInWith(Email) {
            this.email = emailFinal
            this.password = contrasenya
        }

        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: throw Exception("Sessió no trobada")
        return getUsuariPerId(userId)
    }

    suspend fun getUsuariPerId(id: String): Usuari =
        SupabaseClient.client.from("usuaris").select { filter { eq("id", id) } }.decodeSingle()

    suspend fun actualitzarPerfil(id: String, nom: String, desc: String?, pass: String?, avatar: String?,contrasenyaAntiga: String?,emailAntic: String): Usuari {
        if (!pass.isNullOrBlank()) {
            SupabaseClient.client.auth.updateUser {
                this.password = pass
            }
        }

        val data = buildJsonObject {
            put("nom_usuari", nom)
            put("updated_at", Instant.now().toString())
            desc?.let { put("descripcio", it) }
            avatar?.let { put("avatar_url", it) }
        }

        return SupabaseClient.client.from("usuaris")
            .update(data) {
                filter { eq("id", id) }
                select()
            }
            .decodeSingle()
    }

    suspend fun enviarResetPassword(email: String) = SupabaseClient.client.auth.resetPasswordForEmail(email)
}
