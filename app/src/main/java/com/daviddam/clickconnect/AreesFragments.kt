package com.daviddam.clickconnect

import adapter.AreesAdapter
import adapter.PostAdapter
import adapter.PresentacioAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.daviddam.clickconnect.databinding.FragmentAreesFragmentsBinding
import kotlinx.coroutines.flow.collectLatest
import models.Area
import androidx.core.widget.addTextChangedListener
import sharedPreference.SharedPreference
import util.ImageExtension.loadImageOrDefault
import viewmodel.AreesViewModel
import viewmodel.EditarPostViewModel
import viewmodel.EditarPresentacioViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AreesFragments.newInstance] factory method to
 * create an instance of this fragment.
 */
class AreesFragments : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentAreesFragmentsBinding
    private lateinit var areesAdapter: AreesAdapter
    private lateinit var presentacioAdapter: PresentacioAdapter
    private lateinit var postAdapter: PostAdapter

    private val viewModelAreesViewModel: AreesViewModel by viewModels()
    private val viewModelEliminarPost: EditarPostViewModel by viewModels()
    private val viewModelEliminarPresentacio: EditarPresentacioViewModel by viewModels()

    private var totesArees: List<Area> = emptyList()
    private var indexInici = 0
    private val maxPerPagina = 3
    private var modePresentacions = false

    private var totsPosts: List<models.Post> = emptyList()
    private var filtreUsuari: String? = null
    private var filtreData: String? = null

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
        binding = FragmentAreesFragmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val idUsuariLoguejat = SharedPreference.obtenirUsuariLoguejat(requireContext())

        binding.layoutUsuari.setOnClickListener { mostrarMenuUsuari(it) }

        areesAdapter = AreesAdapter(emptyList()) { area, v ->
            val actual = viewModelAreesViewModel.uiState.value.areaSeleccionada
            if (actual?.id == area.id) {
                mostrarSubmenuArea(v)
            } else {
                viewModelAreesViewModel.seleccionarArea(area)
            }
        }

        presentacioAdapter = PresentacioAdapter(
            emptyList(),
            idUsuariLoguejat,
            onEditar = { p ->
                findNavController().navigate(AreesFragmentsDirections.actionAreesFragmentsToEditarPresentacioFragment(p.id))
            },
            onEliminar = { p -> viewModelEliminarPresentacio.eliminarPresentacio(p) }
        )

        postAdapter = PostAdapter(
            emptyList(),
            idUsuariLoguejat,
            onEditar = { p ->
                findNavController().navigate(AreesFragmentsDirections.actionAreesFragmentsToEditarPostFragment(p.id))
            },
            onEliminar = { p -> viewModelEliminarPost.eliminarPost(p) }
        )

        binding.rvArees.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = areesAdapter
        }

        binding.rvPresentacions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = presentacioAdapter
        }

        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }

        binding.btnPrevArea.setOnClickListener {
            if (indexInici - maxPerPagina >= 0) {
                indexInici -= maxPerPagina
                actualitzarAreesPaginades()
            }
        }

        binding.btnNextArea.setOnClickListener {
            if (indexInici + maxPerPagina < totesArees.size) {
                indexInici += maxPerPagina
                actualitzarAreesPaginades()
            }
        }

        binding.fabAccio.setOnClickListener {
            val area = viewModelAreesViewModel.uiState.value.areaSeleccionada
            if (area == null) {
                Toast.makeText(requireContext(), getString(R.string.selecciona_area), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (modePresentacions) {
                findNavController().navigate(AreesFragmentsDirections.actionAreesFragmentsToCrearPresentacioFragment(area.id))
            } else {
                findNavController().navigate(AreesFragmentsDirections.actionAreesFragmentsToCrearPostFragment(area.id))
            }
        }


        binding.etFiltreUsuari.addTextChangedListener {
            filtreUsuari = it?.toString()?.trim().takeIf { s -> !s.isNullOrEmpty() }
            applyFilters()
        }

        binding.etFiltreData.setOnClickListener {
            showDateDialog()
        }

        binding.etFiltreData.addTextChangedListener {
            filtreData = it?.toString()?.trim().takeIf { s -> !s.isNullOrEmpty() }
            applyFilters()
        }


        lifecycleScope.launchWhenStarted {
            viewModelAreesViewModel.uiState.collectLatest { state ->
                state.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
                binding.tvNomUsuari.text = state.nomUsuari ?: getString(R.string.usuari)
                binding.imgAvatar.loadImageOrDefault(state.avatarUrl, R.drawable.avatar)
                totesArees = state.areas
                actualitzarAreesPaginades()
                areesAdapter.setSelected(state.areaSeleccionada?.id)
                presentacioAdapter.updateData(state.presentacions)


                totsPosts = state.posts
                applyFilters()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModelEliminarPost.uiState.collect { state ->
                if (state.postEliminat) {
                    viewModelAreesViewModel.refrescarArea()
                    Toast.makeText(requireContext(), getString(R.string.post_eliminat), Toast.LENGTH_SHORT).show()
                    viewModelEliminarPost.resetPostEliminat()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModelEliminarPresentacio.uiState.collect { state ->
                if (state.presentacioEliminada) {
                    viewModelAreesViewModel.refrescarArea()
                    Toast.makeText(requireContext(), getString(R.string.presentacio_eliminada), Toast.LENGTH_SHORT).show()
                    viewModelEliminarPresentacio.resetPresentacioEliminada()
                }
            }
        }

        viewModelAreesViewModel.carregarArees(idUsuariLoguejat)
        actualitzarMode()
    }

    private fun actualitzarMode() {
        binding.rvPresentacions.visibility = if (modePresentacions) View.VISIBLE else View.GONE
        binding.rvPosts.visibility = if (modePresentacions) View.GONE else View.VISIBLE
        binding.layoutFiltres.visibility = if (modePresentacions) View.GONE else View.VISIBLE
    }

    private fun actualitzarAreesPaginades() {
        val fi = (indexInici + maxPerPagina).coerceAtMost(totesArees.size)
        areesAdapter.updateData(if (indexInici < fi) totesArees.subList(indexInici, fi) else emptyList())
        val hiHaMesDeTres = totesArees.size > maxPerPagina
        binding.btnPrevArea.visibility = if (hiHaMesDeTres && indexInici > 0) View.VISIBLE else View.INVISIBLE
        binding.btnNextArea.visibility = if (hiHaMesDeTres && indexInici + maxPerPagina < totesArees.size) View.VISIBLE else View.INVISIBLE
    }

    private fun applyFilters() {
        var filtrat = totsPosts
        filtreUsuari?.let { usuari ->
            filtrat = filtrat.filter {
                it.nom_usuari?.contains(usuari, ignoreCase = true) == true
            }
        }
        filtreData?.let { data ->
            filtrat = filtrat.filter { it.created_at.startsWith(data) }
        }
        postAdapter.updateData(filtrat)
    }

    private fun showDateDialog() {
        val now = java.util.Calendar.getInstance()
        val year = now.get(java.util.Calendar.YEAR)
        val month = now.get(java.util.Calendar.MONTH)
        val day = now.get(java.util.Calendar.DAY_OF_MONTH)
        val dialog = android.app.DatePickerDialog(requireContext(), { _, y, m, d ->
            val formatted = String.format("%04d-%02d-%02d", y, m + 1, d)
            binding.etFiltreData.setText(formatted)
        }, year, month, day)
        dialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "Clear") { _, _ ->
            binding.etFiltreData.setText("")
        }
        dialog.show()
    }

    private fun mostrarMenuUsuari(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add(0, 1, 0, getString(R.string.perfil_usuari))
        popup.menu.add(0, 2, 1, getString(R.string.tancar_sessio))
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> findNavController().navigate(R.id.action_areesFragments_to_editarPerfilFragment)
                2 -> {
                    SharedPreference.tancarSessio(requireContext())
                    findNavController().navigate(R.id.iniciFragment)
                }
            }
            true
        }
        popup.show()
    }

    private fun mostrarSubmenuArea(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add(0, 1, 0, getString(R.string.posts))
        popup.menu.add(0, 2, 1, getString(R.string.presentacions))
        popup.setOnMenuItemClickListener { item ->
            modePresentacions = (item.itemId == 2)
            actualitzarMode()
            true
        }
        popup.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AreesFragments.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AreesFragments().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
