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
import com.daviddam.clickconnect.databinding.FragmentEditarPerfilBinding
import kotlinx.coroutines.flow.collectLatest
import sharedPreference.SharedPreference
import util.ImageExtension.loadImageOrDefault
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
            binding.imgAvatar.loadImageOrDefault(it.toString(), isProfile = true)
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
            Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_SHORT).show()
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


        val idiomesAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_selected, idiomesLabels)
        idiomesAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown)
        binding.spinnerIdioma.adapter = idiomesAdapter

        val temesAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_selected, temesLabels)
        temesAdapter.setDropDownViewResource(R.layout.spinner_item_dropdown)
        binding.spinnerTema.adapter = temesAdapter

        binding.botoEnrere.setOnClickListener { findNavController().navigateUp() }
        binding.btnCanviarAvatar.setOnClickListener { launcherAvatar.launch("image/*") }

        binding.btnGuardar.setOnClickListener {
            val avatarBytes = selectedAvatarUri?.let { u ->
                requireContext().contentResolver.openInputStream(u)?.use { it.readBytes() }
            }

            viewModelEditarPerfil.guardarCanvis(
                idUsuari, 
                binding.etNom.text.toString().trim(),
                binding.etEmail.text.toString().trim(),
                binding.etDescripcio.text.toString().trim().ifBlank { null },
                binding.etContrasenyaAntiga.text.toString().trim().ifBlank { null },
                binding.etContrasenya.text.toString().trim().ifBlank { null },
                idiomesKeys[binding.spinnerIdioma.selectedItemPosition],
                temesKeys[binding.spinnerTema.selectedItemPosition],
                binding.switchNotificacions.isChecked,
                avatarImageBytes = avatarBytes
            )
        }

        binding.btnEliminar.setOnClickListener {
            SharedPreference.tancarSessio(requireContext())
            findNavController().navigate(R.id.iniciFragment)
        }

        lifecycleScope.launchWhenStarted {
            viewModelEditarPerfil.uiState.collectLatest { state ->
                state.error?.let { Toast.makeText(requireContext(), it.asString(requireContext()), Toast.LENGTH_SHORT).show() }

                if (state.usuari != null && binding.etNom.text.isNullOrBlank()) {
                    binding.etNom.setText(state.usuari.nom_usuari)
                    binding.etEmail.setText(state.usuari.email)
                    binding.etDescripcio.setText(state.usuari.descripcio ?: "")
                    binding.imgAvatar.loadImageOrDefault(state.usuari.avatar_url, isProfile = true)

                    state.preferencies?.let { prefs ->
                        binding.spinnerIdioma.setSelection(idiomesKeys.indexOf(prefs.llenguatge).coerceAtLeast(0))
                        binding.spinnerTema.setSelection(temesKeys.indexOf(prefs.tema).coerceAtLeast(0))
                        binding.switchNotificacions.isChecked = prefs.rebre_notificacions
                    }
                }

                if (state.usuariActualitzat != null) {
                    PreferenciesApplier.applyLanguageAndTheme(
                        requireActivity(),
                        idiomesKeys[binding.spinnerIdioma.selectedItemPosition],
                        temesKeys[binding.spinnerTema.selectedItemPosition]
                    )
                    Toast.makeText(requireContext(), getString(R.string.perfil_actualitzat), Toast.LENGTH_SHORT).show()
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
