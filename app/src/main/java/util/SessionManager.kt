package util

import android.content.Context
import conexio.SupabaseClient
import daos.FcmTokenDao
import io.github.jan.supabase.auth.auth
import sharedPreference.SharedPreference

object SessionManager {
    suspend fun tancarSessio(context: Context) {
        val idUsuari = SharedPreference.obtenirUsuariLoguejat(context)
        val token = SharedPreference.obtenirFcmToken(context)

        if (!idUsuari.isNullOrBlank() && !token.isNullOrBlank()) {
            try {
                FcmTokenDao.desactivarTokenFcm(idUsuari, token)
            } catch (_: Exception) {
            }
        }

        try {
            SupabaseClient.client.auth.signOut()
        } catch (_: Exception) {
        }

        SharedPreference.tancarSessio(context)
    }
}
