package daos

import conexio.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import models.Usuari
import java.security.MessageDigest
import java.util.Properties
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuariDao {

    private val otpCodis: MutableMap<String, String> = mutableMapOf()

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

    suspend fun enviarOtp(email: String) {
        SupabaseClient.client
            .from("usuaris")
            .select { filter { eq("email", email) } }
            .decodeList<Usuari>()
            .firstOrNull()
            ?: throw Exception("No s'ha trobat cap usuari amb aquest correu")

        val codi = (100_000..999_999).random().toString()
        otpCodis[email] = codi


        withContext(Dispatchers.IO) {
            try {
                val propietats = Properties().apply {
                    put("mail.smtp.host", "10.0.2.2")
                    put("mail.smtp.port", "25")
                    put("mail.smtp.auth", "false")
                }
                val sessio = Session.getInstance(propietats)
                val missatge = MimeMessage(sessio).apply {
                    setFrom(InternetAddress("no-reply@clickconnect.local"))
                    addRecipient(Message.RecipientType.TO, InternetAddress(email))
                    setSubject("Codi de recuperació")
                    setText("El teu codi és: $codi")
                }
                Transport.send(missatge)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun verificarOtpICanviar(email: String, codi: String, novaContrasenya: String) {
        val guardat = otpCodis[email] ?: throw Exception("No s'ha sol·licitat cap codi")
        if (guardat != codi) throw Exception("Codi incorrecte")

        val hash = sha256(novaContrasenya)

        try {
            SupabaseClient.client
                .from("usuaris")
                .update(mapOf("contrasenya_hash" to hash)) {
                    filter { eq("email", email) }
                }
                .decodeList<Usuari>()
        } catch (_: Exception) {

        }

        otpCodis.remove(email)
    }
}
