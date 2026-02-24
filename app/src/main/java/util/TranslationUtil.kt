package util

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

object TranslationUtil {

    private fun getLanguageCode(llenguatge: String): String {
        return when (llenguatge) {
            "Català" -> "ca"
            "Español" -> "es"
            "Anglès" -> "en"
            else -> "es"
        }
    }

    suspend fun translateList(texts: List<String>, targetLanguage: String): List<String> = coroutineScope {
        val targetCode = getLanguageCode(targetLanguage)
        val sourceCode = TranslateLanguage.CATALAN


        if (targetCode == sourceCode && targetLanguage == "Castellà") {
            return@coroutineScope texts
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceCode)
            .setTargetLanguage(targetCode)
            .build()

        val translator = Translation.getClient(options)
        
        try {
            translator.downloadModelIfNeeded().await()
            
            val result = texts.map { text ->
                async {
                    try {
                        val t = translator.translate(text).await()
                        t
                    } catch (e: Exception) {
                        text
                    }
                }
            }.awaitAll()
            
            result
        } catch (e: Exception) {
            texts
        } finally {
            translator.close()
        }
    }
}
