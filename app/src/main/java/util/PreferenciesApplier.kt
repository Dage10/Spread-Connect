package util

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object PreferenciesApplier {

    fun applyLanguage(activity: Activity, llenguatge: String) {
        val tag = when (llenguatge) {
            "Català" -> "ca"
            "Español" -> "es"
            "Anglès" -> "en"
            else -> "es"
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }

    fun applyTheme(activity: Activity, tema: String) {
        val mode = if (tema == "Fosc") AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun applyLanguageAndTheme(activity: Activity, llenguatge: String, tema: String) {
        applyLanguage(activity, llenguatge)
        applyTheme(activity, tema)
        activity.recreate()
    }
}
