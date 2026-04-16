package com.daviddam.clickconnect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.daviddam.clickconnect.databinding.FragmentOblidatContrasenyaBinding
import kotlinx.coroutines.launch
import viewmodel.ResetContrasenyaViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OblidatContrasenyaFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OblidatContrasenyaFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var binding: FragmentOblidatContrasenyaBinding
    private val viewModel: ResetContrasenyaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOblidatContrasenyaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.botoEnrere.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEnviar.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            viewModel.enviarOtp(email)
        }

        binding.btnVerificar.setOnClickListener {
            val codi = binding.etCodi.text.toString().trim()
            val novaContra = binding.etNovaContrasenya.text.toString()
            if (novaContra.length < 8) {
                binding.textError.text = getString(R.string.contrasenya_curta)
                return@setOnClickListener
            }
            viewModel.verificarCodi(codi, novaContra)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.textError.text = when {
                        state.loading -> getString(R.string.carregant)
                        state.error != null -> state.error.asString(requireContext())
                        state.success && state.step == 2 -> getString(R.string.correu_codi_enviat)
                        state.success && state.step == 3 -> getString(R.string.contrasenya_actualitzada)
                        else -> ""
                    }
                    binding.btnEnviar.isEnabled = !state.loading
                    binding.btnVerificar.isEnabled = !state.loading

                    val mostrarInputs = state.step == 2
                    binding.codiInput.visibility = if (mostrarInputs) View.VISIBLE else View.GONE
                    binding.novaContrasenyaInput.visibility = if (mostrarInputs) View.VISIBLE else View.GONE
                    binding.btnVerificar.visibility = if (mostrarInputs) View.VISIBLE else View.GONE
                    binding.etEmail.isEnabled = !mostrarInputs
                    binding.btnEnviar.visibility = if (mostrarInputs) View.GONE else View.VISIBLE

                    if (state.success && state.step == 3) {
                        Toast.makeText(requireContext(), getString(R.string.contrasenya_actualitzada), Toast.LENGTH_SHORT).show()
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
         * @return A new instance of fragment OblidatContrasenyaFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OblidatContrasenyaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}