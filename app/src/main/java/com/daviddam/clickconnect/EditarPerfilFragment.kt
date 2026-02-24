package com.daviddam.clickconnect

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.daviddam.clickconnect.databinding.FragmentEditarPerfilBinding
import kotlinx.coroutines.flow.collectLatest
import sharedPreference.SharedPreference
import util.PreferenciesApplier
import viewmodel.EditarPerfilViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditarPerfilFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditarPerfilFragment : Fragment() {

    private var selectedAvatarUri: Uri? = null

    private val launcherAvatar = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedAvatarUri = it
            binding.imgAvatar.load(it)
        }
    }

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentEditarPerfilBinding
    private val viewModelEditarPerfil: EditarPerfilViewModel by viewModels()

    private val idiomesKeys = listOf("Català", "Español", "Anglès")
    private val temesKeys = listOf("Clar", "Fosc")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditarPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val idUsuari = SharedPreference.obtenirUsuariLoguejat(requireContext())
        if (idUsuari == null) {
            Toast.makeText(requireContext(), "Has de fer login", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        val idiomesLabels = listOf(
            getString(R.string.idioma_catala),
            getString(R.string.idioma_castella),
            getString(R.string.idioma_angles)
        )
        val temesLabels = listOf(
            getString(R.string.tema_clar),
            getString(R.string.tema_fosc)
        )

        val adapterIdiomes = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item_selected,
            idiomesLabels
        )
        adapterIdiomes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerIdioma.adapter = adapterIdiomes

        val adapterTemes = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item_selected,
            temesLabels
        )
        adapterTemes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTema.adapter = adapterTemes

        binding.botoEnrere.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCanviarAvatar.setOnClickListener { launcherAvatar.launch("image/*") }

        binding.btnGuardar.setOnClickListener {
            val nom = binding.etNom.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val desc = binding.etDescripcio.text.toString().trim()
            val contrasenyaAntiga = binding.etContrasenyaAntiga.text.toString().trim().ifBlank {
                null
            }
            val novaPass = binding.etContrasenya.text.toString().trim().ifBlank {
                null
            }

            val llenguatge = idiomesKeys[binding.spinnerIdioma.selectedItemPosition]
            val tema = temesKeys[binding.spinnerTema.selectedItemPosition]
            val rebreNotificacions = binding.switchNotificacions.isChecked

            val avatarBytes = selectedAvatarUri?.let { u ->
                requireContext().contentResolver.openInputStream(u)?.use { it.readBytes() }
            }

            viewModelEditarPerfil.guardarCanvis(
                idUsuari, nom, email, desc.ifBlank { null },
                contrasenyaAntiga, novaPass,
                llenguatge, tema, rebreNotificacions,
                avatarImageBytes = avatarBytes
            )
        }

        binding.btnEliminar.setOnClickListener {
            SharedPreference.tancarSessio(requireContext())
            findNavController().navigate(R.id.iniciFragment)
        }

        lifecycleScope.launchWhenStarted {
            viewModelEditarPerfil.uiState.collectLatest { state ->
                when {

                    state.error != null -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }

                    state.usuari != null && binding.etNom.text.isNullOrBlank() -> {
                        binding.etNom.setText(state.usuari.nom_usuari)
                        binding.etEmail.setText(state.usuari.email)
                        binding.etDescripcio.setText(state.usuari.descripcio ?: "")

                        state.usuari.avatar_url?.let { url ->
                            if (!url.isNullOrEmpty()) {
                                try {
                                    binding.imgAvatar.load(url) {
                                        crossfade(true)
                                        error(R.drawable.avatar)
                                    }
                                } catch (e: Exception) {
                                    binding.imgAvatar.setImageResource(R.drawable.avatar)
                                }
                            } else {
                                binding.imgAvatar.setImageResource(R.drawable.avatar)
                            }
                        }

                        state.preferencies?.let { prefs ->
                            val idxIdioma = idiomesKeys.indexOf(prefs.llenguatge).takeIf {
                                it >= 0
                            } ?: 0
                            val idxTema = temesKeys.indexOf(prefs.tema).takeIf {
                                it >= 0
                            } ?: 0
                            binding.spinnerIdioma.setSelection(idxIdioma)
                            binding.spinnerTema.setSelection(idxTema)
                            binding.switchNotificacions.isChecked = prefs.rebre_notificacions
                        }
                    }

                    state.usuariActualitzat != null -> {
                        val llenguatge = idiomesKeys[binding.spinnerIdioma.selectedItemPosition]
                        val tema = temesKeys[binding.spinnerTema.selectedItemPosition]
                        PreferenciesApplier.applyLanguageAndTheme(requireActivity(), llenguatge, tema)
                        Toast.makeText(requireContext(), getString(R.string.perfil_actualitzat), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModelEditarPerfil.carregarUsuari(idUsuari)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditarPerfilFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditarPerfilFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
