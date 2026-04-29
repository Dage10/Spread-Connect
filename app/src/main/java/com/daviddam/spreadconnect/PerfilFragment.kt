package com.daviddam.spreadconnect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.daviddam.spreadconnect.databinding.FragmentPerfilBinding
import adapter.PostAdapter
import adapter.PresentacioAdapter
import repository.Repository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sharedPreference.SharedPreference
import util.ImageExtension.loadImageOrDefault
import viewmodel.PerfilViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PerfilFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PerfilFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentPerfilBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var presentacioAdapter: PresentacioAdapter
    private val repo = Repository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private val viewModel: PerfilViewModel by viewModels()
    private val args: PerfilFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val idUsuariLoguejat = SharedPreference.obtenirUsuariLoguejat(requireContext())

        binding.btnSeguir.visibility = View.GONE
        binding.btnEnviarMissatge.visibility = View.GONE

        postAdapter = PostAdapter(
            emptyList(),
            idUsuariLoguejat,
            onEditar = {},
            onEliminar = {},
            onComentaris = { p ->
                findNavController().navigate(PerfilFragmentDirections.actionPerfilFragmentToComentarisFragment(p.id, "post"))
            },
            mostrarBotons = false
        )

        presentacioAdapter = PresentacioAdapter(
            emptyList(),
            idUsuariLoguejat,
            onEditar = {},
            onEliminar = {},
            onComentaris = { p ->
                findNavController().navigate(PerfilFragmentDirections.actionPerfilFragmentToComentarisFragment(p.id, "presentacio"))
            },
            mostrarBotons = false
        )

        binding.rvPostsPerfil.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }

        binding.rvPresentacionsPerfil.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = presentacioAdapter
        }

        binding.botoEnrere.setOnClickListener { findNavController().navigateUp() }

        binding.btnSeguir.setOnClickListener {
            idUsuariLoguejat?.let { idLoguejat ->
                viewModel.toggleSeguir(idLoguejat)
            } ?: Toast.makeText(requireContext(), getString(R.string.login), Toast.LENGTH_SHORT).show()
        }

        binding.btnEnviarMissatge.setOnClickListener {
            idUsuariLoguejat?.let { idLoguejat ->
                val usuari = viewModel.uiState.value.usuari
                usuari?.let { u ->
                    lifecycleScope.launch {
                        try {
                            val conversaExisteix = repo.missatgeriaDao.trobarConversaExistents(idLoguejat, u.id)
                            val conversa = conversaExisteix ?: repo.missatgeriaDao.crearConversa(idLoguejat, u.id)
                            findNavController().navigate(PerfilFragmentDirections.actionPerfilFragmentToChatFragment(conversa.id))
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } ?: Toast.makeText(requireContext(), getString(R.string.login), Toast.LENGTH_SHORT).show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    state.error?.let {
                        Toast.makeText(requireContext(), it.asString(requireContext()), Toast.LENGTH_SHORT).show()
                    }

                    state.usuari?.let { usuari ->
                        binding.tvUsername.text = usuari.nom_usuari
                        binding.tvDescripcio.text = usuari.descripcio ?: ""
                        binding.imgAvatar.loadImageOrDefault(usuari.avatar_url, isProfile = true)

                        if (usuari.id == idUsuariLoguejat) {
                            binding.btnSeguir.visibility = View.GONE
                            binding.btnEnviarMissatge.visibility = View.GONE
                        } else {
                            binding.btnSeguir.visibility = View.VISIBLE
                            binding.btnEnviarMissatge.visibility = View.VISIBLE
                            binding.btnSeguir.text = if (state.isSeguint) getString(R.string.seguint) else getString(R.string.seguir)
                        }
                    }

                    if (state.numSeguidors != null) {
                        binding.tvNumSeguidors.text = state.numSeguidors.toString()
                    }
                    if (state.numSeguint != null) {
                        binding.tvNumSeguint.text = state.numSeguint.toString()
                    }

                    if (state.posts.isNotEmpty()) {
                        binding.ultimsPosts.visibility = View.VISIBLE
                        binding.rvPostsPerfil.visibility = View.VISIBLE
                        postAdapter.updateData(state.posts)
                    }
                    if (state.presentacions.isNotEmpty()) {
                        binding.ultimesPresentacions.visibility = View.VISIBLE
                        binding.rvPresentacionsPerfil.visibility = View.VISIBLE
                        presentacioAdapter.updateData(state.presentacions)
                    }
                }
            }
        }

        viewModel.carregarPerfil(args.idUsuari, idUsuariLoguejat)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PerfilFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PerfilFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
