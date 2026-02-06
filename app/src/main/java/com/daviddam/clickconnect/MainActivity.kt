package com.daviddam.clickconnect

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.daviddam.clickconnect.databinding.ActivityMainBinding
import androidx.navigation.NavController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import repository.Repository
import sharedPreference.SharedPreference

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController

        carregarIAplicarPreferencies()
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
                            "Castellà" -> "es"
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
                        Toast.makeText(this@MainActivity, "Error al carregar les preferències", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}