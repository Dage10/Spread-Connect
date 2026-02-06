package com.daviddam.clickconnect

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.daviddam.clickconnect.databinding.FragmentCrearPresentacioBinding
import conexio.SupabaseStorage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sharedPreference.SharedPreference
import viewmodel.CrearPresentacioViewModel

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CrearPresentacioFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CrearPresentacioFragment : Fragment() {

    private var selectedImageUri: Uri? = null
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCrearPresentacioBinding
    private val args: CrearPresentacioFragmentArgs by navArgs()
    private val viewModel: CrearPresentacioViewModel by viewModels()

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imgPreview.load(it)
            binding.imgPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCrearPresentacioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val idUsuari = SharedPreference.obtenirUsuariLoguejat(requireContext())

        if (idUsuari == null) {
            Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        setupClickListeners(idUsuari)
        setupObservers()
    }

    private fun setupClickListeners(idUsuari: String) {
        binding.btnSeleccionarImatge.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnGuardar.setOnClickListener {
            val titol = binding.etTitol.text.toString().trim()
            val contingut = binding.etContingut.text.toString().trim()

            if (titol.isEmpty() || contingut.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.omple_tots_camps), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val imatgeUrl = selectedImageUri?.let { uri ->
                        SupabaseStorage.penjarPresentacioImatge(uri) { u ->
                            requireContext().contentResolver.openInputStream(u)?.use { it.readBytes() } ?: byteArrayOf()
                        }
                    }
                    viewModel.crearPresentacio(idUsuari, args.areaId, titol, contingut, imatgeUrl)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.botoEnrere.setOnClickListener { findNavController().navigateUp() }
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collectLatest { state ->
                when {
                    state.error != null -> Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    state.presentacioCreada != null -> {
                        Toast.makeText(requireContext(), getString(R.string.presentacio_creada), Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CrearPresentacioFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CrearPresentacioFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
