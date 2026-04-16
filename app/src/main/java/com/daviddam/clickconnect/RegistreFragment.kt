package com.daviddam.clickconnect

import android.os.Bundle
import android.util.Patterns
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
import com.daviddam.clickconnect.databinding.FragmentRegistreBinding
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegistreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegistreFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentRegistreBinding
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
        binding = FragmentRegistreBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val viewModelRegistre: viewmodel.RegistreViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.botoEnrere.setOnClickListener { findNavController().navigateUp() }

        binding.btnRegistre.setOnClickListener {
            val campUsuari = binding.etUsuari.text.toString().trim()
            val campEmail = binding.etEmail.text.toString().trim()
            val campContrasenya = binding.etConstrasenya.text.toString().trim()
            val campRepetir = binding.etReConstrasenya.text.toString().trim()

            if (campUsuari.isEmpty() || campEmail.isEmpty() || campContrasenya.isEmpty() || campRepetir.isEmpty()) {
                binding.textError.text = getString(R.string.omple_tots_camps)
                return@setOnClickListener
            }

            if(campUsuari.length > 30){
                binding.textError.text = getString(R.string.usuari_molt_llarg)
                return@setOnClickListener
            }

            if(!Patterns.EMAIL_ADDRESS.matcher(campEmail).matches()){
                binding.textError.text = getString(R.string.email_no_valid)
                return@setOnClickListener
            }

            if(campContrasenya != campRepetir){
                binding.textError.text = getString(R.string.contrasenyes_no_conceidexen)
                return@setOnClickListener
            }

            if(campContrasenya.length < 8){
                binding.textError.text = getString(R.string.contrasenya_curta)
                return@setOnClickListener
            }

            if(campContrasenya.length > 255){
                binding.textError.text = getString(R.string.contrasenya_molt_llarga)
                return@setOnClickListener
            }
            
            viewModelRegistre.registre(campUsuari, campEmail, campContrasenya, campRepetir)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelRegistre.uiState.collect { state ->
                    binding.btnRegistre.isEnabled = !state.loading
                    binding.textError.text = when {
                        state.loading -> getString(R.string.carregant)
                        state.error != null -> state.error.asString(requireContext())
                        else -> ""
                    }

                    binding.btnRegistre.isEnabled = !state.loading

                    if (state.isSuccess) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.usuari_creat_correctament),
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(RegistreFragmentDirections.actionRegistreFragmentToLoginFragment())
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
         * @return A new instance of fragment RegistreFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegistreFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
