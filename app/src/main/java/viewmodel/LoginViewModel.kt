package viewmodel

import models.LoginUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daviddam.spreadconnect.R
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

                _uiState.value = LoginUiState(usuari = usuari)
            } catch (_: Exception) {
                _uiState.value = LoginUiState(error = UiText.StringResource(R.string.credenciales_incorrectas))
            }
        }
    }
}
