package com.daviddam.clickconnect

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.daviddam.clickconnect.databinding.ActivityMainBinding
import androidx.navigation.NavController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import repository.Repository
import sharedPreference.SharedPreference

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(3000)
        installSplashScreen()
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController

        carregarIAplicarPreferencies()
        verificarSessioTempsReal()
    }

    private fun carregarIAplicarPreferencies() {
        val idUsuari = SharedPreference.obtenirUsuariLoguejat(this)
        if (idUsuari != null) {
            lifecycleScope.launch {
                try {
                    val prefs = Repository().preferenciesDao.getPerUsuari(idUsuari)
                    prefs?.let {
                        val tag = when (it.llenguatge) {
                            "Català" -> "ca"
                            "Español" -> "es"
                            "Anglès" -> "en"
                            else -> "es"
                        }
                        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                            androidx.core.os.LocaleListCompat.forLanguageTags(tag)
                        )
                        val mode = if (it.tema == "Fosc") 
                            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES 
                        else 
                            androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
                    }
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        Toast.makeText(this@MainActivity, getString(R.string.error_carregar_dades), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun verificarSessioTempsReal() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    val idUsuari = SharedPreference.obtenirUsuariLoguejat(this@MainActivity)

                    if (idUsuari != null) {
                        try {
                            Repository().usuariDao.getUsuariPerId(idUsuari)
                        } catch (e: Exception) {
                            val errorMissatge = e.message
                            if (errorMissatge == R.string.usuari_no_trobat.toString() || errorMissatge?.contains("404") == true) {
                                tancarSessioIAnarInici()
                            }
                        }
                    }
                    delay(1000)
                }
            }
        }
    }

    private fun tancarSessioIAnarInici() {
        SharedPreference.tancarSessio(this@MainActivity)
        navController.navigate(R.id.iniciFragment)
        Toast.makeText(this@MainActivity, getString(R.string.usuari_no_trobat), Toast.LENGTH_LONG).show()
    }
}
