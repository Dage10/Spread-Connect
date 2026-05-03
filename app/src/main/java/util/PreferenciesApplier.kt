package util

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object PreferenciesApplier {

    private val languageTagMap = mapOf(
        "Català" to "ca",
        "Español" to "es",
        "Anglès" to "en"
    )

    fun applyLanguage(llenguatge: String) {
        val tag = languageTagMap[llenguatge] ?: "es"
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }

    fun applyTheme(tema: String) {
        val mode = if (tema == "Fosc") AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun applyLanguageAndTheme(activity: Activity? = null, llenguatge: String, tema: String) {
        applyLanguage(llenguatge)
        applyTheme(tema)
        activity?.recreate()
    }
}
