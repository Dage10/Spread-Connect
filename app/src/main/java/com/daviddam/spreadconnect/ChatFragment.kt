package com.daviddam.spreadconnect

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import conexio.SupabaseStorage
import com.daviddam.spreadconnect.databinding.FragmentChatBinding
import adapter.MissatgeAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sharedPreference.SharedPreference
import util.ImageExtension.loadImageOrDefault
import viewmodel.ChatViewModel

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var missatgeAdapter: MissatgeAdapter
    private val viewModel: ChatViewModel by viewModels()
    private val args: ChatFragmentArgs by navArgs()
    private var imatgeUriSeleccionada: Uri? = null

    private val seleccionarImatge = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imatgeUriSeleccionada = it
            Toast.makeText(requireContext(), "Imatge seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val idUsuariLoguejat = SharedPreference.obtenirUsuariLoguejat(requireContext())
        missatgeAdapter = MissatgeAdapter(emptyList(), idUsuariLoguejat)

        binding.rvMissatges.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = missatgeAdapter
        }

        binding.btnEnrere.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEnviar.setOnClickListener {
            val missatge = binding.etMissatge.text?.toString()?.trim()
            if (imatgeUriSeleccionada != null && idUsuariLoguejat != null) {
                lifecycleScope.launch {
                    try {
                        val url = SupabaseStorage.penjarMissatgeImatge(imatgeUriSeleccionada!!) { uri ->
                            requireContext().contentResolver.openInputStream(uri)?.readBytes() ?: ByteArray(0)
                        }
                        viewModel.enviarMissatge(args.idConversa, idUsuariLoguejat, missatge, url)
                        imatgeUriSeleccionada = null
                        binding.etMissatge.text?.clear()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error penjant imatge", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (!missatge.isNullOrEmpty() && idUsuariLoguejat != null) {
                viewModel.enviarMissatge(args.idConversa, idUsuariLoguejat, missatge)
                binding.etMissatge.text?.clear()
            }
        }

        binding.btnEnviarImatge.setOnClickListener {
            seleccionarImatge.launch("image/*")
        }

        if (idUsuariLoguejat != null) {
            viewModel.carregarConversa(args.idConversa, idUsuariLoguejat)
        } else {
            Toast.makeText(requireContext(), "Error al carregar conversa", Toast.LENGTH_SHORT).show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    if (state.error != null) {
                        Toast.makeText(requireContext(), state.error.asString(requireContext()), Toast.LENGTH_SHORT).show()
                    }

                    state.altreUsuari?.let { altreUsuari ->
                        binding.tvNomUsuari.text = altreUsuari.nom_usuari.orEmpty()
                        binding.imgAvatarUser.loadImageOrDefault(altreUsuari.avatar_url, isProfile = true)
                    }

                    if (state.missatges.isNotEmpty()) {
                        missatgeAdapter.updateData(state.missatges)
                        binding.rvMissatges.scrollToPosition(state.missatges.size - 1)
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ChatFragment()
    }
}
