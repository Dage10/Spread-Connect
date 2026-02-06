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
import com.daviddam.clickconnect.databinding.FragmentEditarPresentacioBinding
import conexio.SupabaseStorage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import viewmodel.EditarPresentacioViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditarPresentacioFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditarPresentacioFragment : Fragment() {

    private var selectedImageUri: Uri? = null

    private val launcherImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imgPreview.load(it)
            binding.imgPreview.visibility = View.VISIBLE
        }
    }

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var binding: FragmentEditarPresentacioBinding
    private val args: EditarPresentacioFragmentArgs by navArgs()
    private val viewModelEditarPresentacio: EditarPresentacioViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditarPresentacioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val presentacioId = args.presentacioId
        viewModelEditarPresentacio.carregarPresentacio(presentacioId)

        binding.btnSeleccionarImatge.setOnClickListener { launcherImage.launch("image/*") }

        binding.btnGuardar.setOnClickListener {
            val titol = binding.etTitol.text.toString().trim()
            val contingut = binding.etContingut.text.toString().trim()

            lifecycleScope.launch {
                try {
                    val imatgeUrl = selectedImageUri?.let { uri ->
                        SupabaseStorage.penjarPresentacioImatge(uri) { u ->
                            requireContext().contentResolver.openInputStream(u)?.use { it.readBytes() } ?: byteArrayOf()
                        }
                    } ?: viewModelEditarPresentacio.uiState.value.presentacio?.imatge_url

                    viewModelEditarPresentacio.editarPresentacio(presentacioId, titol, contingut, imatgeUrl)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.botoEnrere.setOnClickListener {
            findNavController().navigateUp()
        }

        lifecycleScope.launchWhenStarted {
            viewModelEditarPresentacio.uiState.collectLatest { state ->

                when {

                    state.error != null -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }
                    state.presentacio != null && binding.etTitol.text.isNullOrBlank() -> {
                        binding.etTitol.setText(state.presentacio.titol)
                        binding.etContingut.setText(state.presentacio.contingut_presentacio)
                        state.presentacio.imatge_url?.let { url ->
                            binding.imgPreview.load(url)
                            binding.imgPreview.visibility = View.VISIBLE
                        }
                    }
                    state.presentacioActualitzada != null -> {
                        Toast.makeText(requireContext(), "Presentacio actualitzada", Toast.LENGTH_SHORT).show()
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
         * @return A new instance of fragment EditarPresentacioFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditarPresentacioFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}