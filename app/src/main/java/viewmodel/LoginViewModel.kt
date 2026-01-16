package viewmodel

import LoginUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import repository.Repository
import sharedPreference.SharedPreference


class LoginViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(nomUsuari: String, contrasenya: String,context: android.content.Context) {
        _uiState.value = LoginUiState(loading = true)

        viewModelScope.launch {
            try {
                val usuari = repo.loginUsuari(nomUsuari, contrasenya)
                SharedPreference.guardarUsuarioLoguejat(context, usuari.id)

                _uiState.value = LoginUiState(usuari = usuari)
            } catch (e: Exception) {
                _uiState.value = LoginUiState(error = e.message)
            }
        }
    }
}
