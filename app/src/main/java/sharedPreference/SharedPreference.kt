package sharedPreference

import android.content.Context

object SharedPreference {
    private const val PREFS_NAME = "prefs_login"
    private const val KEY_USER_ID = "user_id"

    fun guardarUsuariLoguejat(context: Context, usuariId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USER_ID, usuariId).apply()
    }

    fun obtenirUsuariLoguejat(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_ID, null)
    }

    fun estaLoguejat(context: Context): Boolean {
        return obtenirUsuariLoguejat(context) != null
    }

    fun tancarSessio(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }


}