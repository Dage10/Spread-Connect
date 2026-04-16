package com.daviddam.clickconnect

import android.content.Intent
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
import conexio.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch
import repository.Repository
import sharedPreference.SharedPreference

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val repo = Repository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController

        carregarIAplicarPreferencies()
        observarSessio()
    }

    private fun carregarIAplicarPreferencies() {
        val idUsuari = SharedPreference.obtenirUsuariLoguejat(this) ?: return
        lifecycleScope.launch {
            try {
                val prefs = repo.preferenciesDao.getPerUsuari(idUsuari)
                prefs?.let {
                    val tag = when (it.llenguatge) {
                        "Català" -> "ca"
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
            } catch (_: Exception) {}
        }
    }

    private fun observarSessio() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                SupabaseClient.client.auth.sessionStatus.collect { status ->
                    if (status is SessionStatus.NotAuthenticated) {
                        if (SharedPreference.obtenirUsuariLoguejat(this@MainActivity) != null) {
                            tancarSessioIAnarInici()
                        }
                    }
                }
            }
        }
    }

    private fun tancarSessioIAnarInici() {
        SharedPreference.tancarSessio(this@MainActivity)
        navController.navigate(R.id.iniciFragment)
        Toast.makeText(this, getString(R.string.usuari_no_trobat), Toast.LENGTH_LONG).show()
    }
}
