package com.daviddam.clickconnect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.daviddam.clickconnect.databinding.FragmentComentarisBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import sharedPreference.SharedPreference
import util.ImageExtension.loadImageOrDefault
import adapter.ComentarisAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import models.Post
import models.Presentacio
import models.Comentari

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ComentarisFragment : Fragment() {

    private lateinit var binding: FragmentComentarisBinding
    private val viewModel: viewmodel.ComentarisViewModel by viewModels()
    private lateinit var adapter: ComentarisAdapter
    private var idUsuariLoguejat: String? = null
    
    private val args: ComentarisFragmentArgs by navArgs()

    private var selectedImageUri: android.net.Uri? = null
    private val imagePickerLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imgPreviewComentari.loadImageOrDefault(it.toString(), isProfile = false)
            binding.cardPreviewComentari.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentComentarisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        idUsuariLoguejat = SharedPreference.obtenirUsuariLoguejat(requireContext())

        binding.botoEnrere.setOnClickListener { findNavController().navigateUp() }

        binding.btnEliminarPreview.setOnClickListener {
            selectedImageUri = null
            binding.cardPreviewComentari.visibility = View.GONE
        }

        adapter = ComentarisAdapter(
            emptyList(),
            idUsuariLoguejat,
            onEditar = { comentari -> mostrarDialogEditar(comentari) },
            onEliminar = { comentari -> confirmarEliminar(comentari) },
            onLike = { comentari ->
                idUsuariLoguejat?.let { viewModel.reaccionarComentari(comentari.id, args.targetId, it, "like", args.targetType) }
            },
            onDislike = { comentari ->
                idUsuariLoguejat?.let { viewModel.reaccionarComentari(comentari.id, args.targetId, it, "dislike", args.targetType) }
            },
            onRespostes = { parentComment ->
                val action = ComentarisFragmentDirections.actionComentarisFragmentSelf(parentComment.id, "comment")
                findNavController().navigate(action)
            }
        )

        binding.rvComentaris.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = this@ComentarisFragment.adapter
        }

        binding.btnSeleccionarImatgeComentari.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnEnviarComentari.setOnClickListener {
            val text = binding.etNouComentari.text.toString().trim()
            if (text.isNotEmpty() && idUsuariLoguejat != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val imatgeUrl = selectedImageUri?.let { uri ->
                            conexio.SupabaseStorage.penjarComentariImatge(uri) { u ->
                                requireContext().contentResolver.openInputStream(u)?.use { it.readBytes() } ?: byteArrayOf()
                            }
                        }
                        viewModel.enviarComentari(args.targetId, idUsuariLoguejat!!, text, imatgeUrl, args.targetType)
                        binding.etNouComentari.text?.clear()
                        selectedImageUri = null
                        binding.cardPreviewComentari.visibility = View.GONE
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    state.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
                    
                    binding.itemPostPrincipal.root.visibility = View.GONE
                    
                    when (args.targetType) {
                        "post" -> state.post?.let { mostrarPost(it) }
                        "presentacio" -> state.presentacio?.let { mostrarPresentacio(it) }
                        "comment" -> state.comentariPare?.let { mostrarParentComment(it) }
                    }
                    
                    adapter.updateData(state.comentaris)
                }
            }
        }

        viewModel.carregarDades(args.targetId, idUsuariLoguejat, args.targetType)
    }

    private fun mostrarPost(post: Post) {
        binding.itemPostPrincipal.root.visibility = View.VISIBLE
        binding.itemPostPrincipal.apply {
            imgAvatarUser.loadImageOrDefault(post.avatar_url, isProfile = true)
            tvUsuari.text = post.nom_usuari ?: getString(R.string.usuari)
            tvData.text = post.created_at.take(10)
            tvTitol.text = post.titol
            tvDescripcio.text = post.descripcio
            imgPost.loadImageOrDefault(post.imatge_url, isProfile = false)
            imgPost.visibility = if (post.imatge_url.isNullOrEmpty()) View.GONE else View.VISIBLE
            textLikeComptador.text = post.likes.toString()
            tvDislikeComptador.text = post.dislikes.toString()
            
            if (post.reaccioActual == "like") btnLike.setColorFilter(android.graphics.Color.RED) else btnLike.clearColorFilter()
            if (post.reaccioActual == "dislike") btnDislike.setColorFilter(android.graphics.Color.BLUE) else btnDislike.clearColorFilter()

            btnLike.setOnClickListener { idUsuariLoguejat?.let { u -> viewModel.reaccionarPost(post.id, u, "like") } }
            btnDislike.setOnClickListener { idUsuariLoguejat?.let { u -> viewModel.reaccionarPost(post.id, u, "dislike") } }
        }
    }

    private fun mostrarPresentacio(pres: Presentacio) {
        binding.itemPostPrincipal.root.visibility = View.VISIBLE
        binding.itemPostPrincipal.apply {
            imgAvatarUser.loadImageOrDefault(pres.avatar_url, isProfile = true)
            tvUsuari.text = pres.nom_usuari ?: getString(R.string.usuari)
            tvData.text = pres.created_at.take(10)
            tvTitol.text = pres.titol
            tvDescripcio.text = pres.contingut_presentacio
            imgPost.loadImageOrDefault(pres.imatge_url, isProfile = false)
            imgPost.visibility = if (pres.imatge_url.isNullOrEmpty()) View.GONE else View.VISIBLE
            textLikeComptador.text = pres.likes.toString()
            tvDislikeComptador.text = pres.dislikes.toString()

            if (pres.reaccioActual == "like") btnLike.setColorFilter(android.graphics.Color.RED) else btnLike.clearColorFilter()
            if (pres.reaccioActual == "dislike") btnDislike.setColorFilter(android.graphics.Color.BLUE) else btnDislike.clearColorFilter()

            btnLike.setOnClickListener { idUsuariLoguejat?.let { u -> viewModel.reaccionarPresentacio(pres.id, u, "like") } }
            btnDislike.setOnClickListener { idUsuariLoguejat?.let { u -> viewModel.reaccionarPresentacio(pres.id, u, "dislike") } }
        }
    }

    private fun mostrarParentComment(comment: Comentari) {
        binding.itemPostPrincipal.root.visibility = View.VISIBLE
        binding.itemPostPrincipal.apply {
            imgAvatarUser.loadImageOrDefault(comment.avatar_url, isProfile = true)
            tvUsuari.text = comment.nom_usuari ?: getString(R.string.usuari)
            tvData.text = comment.created_at.take(10)
            tvTitol.visibility = View.GONE
            tvDescripcio.text = comment.contingut
            imgPost.loadImageOrDefault(comment.imatge_url, isProfile = false)
            imgPost.visibility = if (comment.imatge_url.isNullOrEmpty()) View.GONE else View.VISIBLE
            textLikeComptador.text = comment.likes.toString()
            tvDislikeComptador.text = comment.dislikes.toString()

            if (comment.reaccioActual == "like") btnLike.setColorFilter(android.graphics.Color.RED) else btnLike.clearColorFilter()
            if (comment.reaccioActual == "dislike") btnDislike.setColorFilter(android.graphics.Color.BLUE) else btnDislike.clearColorFilter()

            btnLike.setOnClickListener { idUsuariLoguejat?.let { u -> viewModel.reaccionarComentari(comment.id, args.targetId, u, "like", "comment") } }
            btnDislike.setOnClickListener { idUsuariLoguejat?.let { u -> viewModel.reaccionarComentari(comment.id, args.targetId, u, "dislike", "comment") } }
        }
    }

    private fun mostrarDialogEditar(comentari: Comentari) {
        val editText = android.widget.EditText(requireContext()).apply {
            setText(comentari.contingut)
            setSelection(comentari.contingut.length)
        }
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.editar_comentari))
            .setView(editText)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val nouText = editText.text.toString().trim()
                if (nouText.isNotEmpty()) {
                    viewModel.editarComentari(comentari.id, nouText, args.targetId, idUsuariLoguejat, args.targetType)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun confirmarEliminar(comentari: Comentari) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.esborrar_comentari))
            .setMessage(getString(R.string.confirma_esborrar_comentari))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.eliminarComentari(comentari.id, args.targetId, idUsuariLoguejat, args.targetType)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ComentarisFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ComentarisFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
