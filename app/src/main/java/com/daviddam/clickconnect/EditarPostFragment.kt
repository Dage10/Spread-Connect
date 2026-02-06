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
import com.daviddam.clickconnect.databinding.FragmentEditarPostBinding
import conexio.SupabaseStorage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import viewmodel.EditarPostViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditarPostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditarPostFragment : Fragment() {

    private var selectedImageUri: Uri? = null
    private var param1: String? = null
    private var param2: String? = null

    private val launcherImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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

    private lateinit var binding: FragmentEditarPostBinding
    private val args: EditarPostFragmentArgs by navArgs()
    private val viewModelEditarPost: EditarPostViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditarPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val postId = args.postId
        viewModelEditarPost.carregarPost(postId)

        binding.btnSeleccionarImatge.setOnClickListener { launcherImage.launch("image/*") }

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
                    } ?: viewModelEditarPost.uiState.value.post?.imatge_url
                    viewModelEditarPost.editarPost(postId, titol, desc, imatgeUrl)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "${getString(R.string.error)}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.botoEnrere.setOnClickListener {
            findNavController().navigateUp()
        }

        lifecycleScope.launchWhenStarted {
            viewModelEditarPost.uiState.collectLatest { state ->
                when {
                    state.error != null -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }
                    state.post != null && binding.etTitol.text.isNullOrBlank() -> {
                        binding.etTitol.setText(state.post.titol)
                        binding.etDescripcio.setText(state.post.descripcio)
                        state.post.imatge_url?.let { url ->
                            binding.imgPreview.load(url)
                            binding.imgPreview.visibility = View.VISIBLE
                        }
                    }
                    state.postActualitzat != null -> {
                        Toast.makeText(requireContext(), getString(R.string.post_actualitzat), Toast.LENGTH_SHORT).show()
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
         * @return A new instance of fragment EditarPostFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditarPostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}