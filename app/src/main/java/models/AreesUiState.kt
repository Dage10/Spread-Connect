package models

data class AreesUiState(
    val loading: Boolean = false,
    val areas: List<Area> = emptyList(),
    val areaSeleccionada: Area? = null,
    val posts: List<Post> = emptyList(),
    val presentacions: List<Presentacio> = emptyList(),
    val nomUsuari: String? = null,
    val avatarUrl: String? = null,
    val error: String? = null
)