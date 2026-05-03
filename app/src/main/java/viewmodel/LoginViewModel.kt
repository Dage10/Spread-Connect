package viewmodel

import android.Manifest
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import daos.FcmTokenDao
import models.LoginUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daviddam.spreadconnect.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import repository.Repository
import sharedPreference.SharedPreference
import util.UiText


class LoginViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, contrasenya: String, context: android.content.Context) {

        if (email.isBlank() || contrasenya.isBlank()) {
            _uiState.value = LoginUiState(error = UiText.StringResource(R.string.omple_tots_camps))
            return
        }

        _uiState.value = LoginUiState(loading = true)

        viewModelScope.launch {
            try {
                val usuari = repo.usuariDao.loginUsuari(email, contrasenya)
                SharedPreference.guardarUsuariLoguejat(context, usuari.id)

                val permissioNotificacionsConcedida = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }

                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val preferenciesExistents = repo.preferenciesDao.getPerUsuari(usuari.id)
                        if (preferenciesExistents == null) {
                            repo.preferenciesDao.insertPreferencies(
                                usuari.id,
                                llenguatge = "Español",
                                tema = "Clar",
                                rebreNotificacions = permissioNotificacionsConcedida
                            )
                        }
                    } catch (_: Exception) {
                    }
                }

                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    if (token.isNullOrBlank()) return@addOnSuccessListener

                    SharedPreference.guardarFcmToken(context, token)
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            FcmTokenDao.guardarTokenFcm(usuari.id, token)
                        } catch (_: Exception) {
                        }
                    }
                }

                _uiState.value = LoginUiState(usuari = usuari)
            } catch (_: Exception) {
                _uiState.value = LoginUiState(error = UiText.StringResource(R.string.credenciales_incorrectas))
            }
        }
    }
}
