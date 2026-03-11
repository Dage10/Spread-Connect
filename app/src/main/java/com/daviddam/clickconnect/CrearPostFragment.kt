package com.daviddam.clickconnect

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import adapter.EtiquetaAdapter
import com.daviddam.clickconnect.databinding.FragmentCrearPostBinding
import conexio.SupabaseStorage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sharedPreference.SharedPreference
import viewmodel.CrearPostViewModel

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CrearPostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CrearPostFragment : Fragment() {

    private var selectedImageUri: Uri? = null
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCrearPostBinding
    private lateinit var etiquetaAdapter: EtiquetaAdapter
    private val args: CrearPostFragmentArgs by navArgs()
    private val viewModel: CrearPostViewModel by viewModels()

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
        binding = FragmentCrearPostBinding.inflate(inflater, container, false)
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
            val desc = binding.etDescripcio.text.toString().trim()
            
            if (titol.isEmpty() || desc.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.omple_tots_camps), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val imatgeUrl = selectedImageUri?.let { uri ->
                        SupabaseStorage.penjarPostImatge(uri) { u ->
                            requireContext().contentResolver.openInputStream(u)?.use { it.readBytes() } ?: byteArrayOf()
                        }
                    }
                    viewModel.crearPost(idUsuari, args.areaId, titol, desc, imatgeUrl)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_LONG).show()
                }
            }
        }

        etiquetaAdapter = EtiquetaAdapter(
            llistaEtiquetes = emptyList(),
            onEditar = { nomActual ->
                val editText = EditText(requireContext()).apply {
                    setText(nomActual)
                    setSelection(nomActual.length)
                }
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.etiqueta))
                    .setView(editText)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val nouNom = editText.text.toString().trim()
                        if (nouNom.isNotEmpty()) {
                            viewModel.treureEtiqueta(nomActual)
                            viewModel.afegirEtiqueta(nouNom)
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            },
            onEliminar = { nom ->
                viewModel.treureEtiqueta(nom)
            }
        )

        binding.rvEtiquetes.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = etiquetaAdapter
        }

        binding.botoCrearEtiqueta.setOnClickListener {
            val editText = EditText(requireContext())
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.etiqueta))
                .setView(editText)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val text = editText.text.toString()
                    viewModel.afegirEtiqueta(text)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        binding.botoEnrere.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            launch {
                viewModel.uiState.collectLatest { state ->
                    when {
                        state.error != null -> Toast.makeText(
                            requireContext(),
                            state.error.asString(requireContext()),
                            Toast.LENGTH_SHORT
                        ).show()

                        state.postCreat != null -> {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.post_creat),
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigateUp()
                        }
                    }
                }
            }

            launch {
                viewModel.etiquetes.collectLatest { etiquetes ->
                    etiquetaAdapter.updateData(etiquetes)
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
         * @return A new instance of fragment CrearPostFragment.
         */
        // TODO: Rename and change types and number of parameters
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CrearPostFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CrearPostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
