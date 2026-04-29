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
import androidx.recyclerview.widget.LinearLayoutManager
import com.daviddam.spreadconnect.databinding.FragmentChatsBinding
import adapter.ConversaAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sharedPreference.SharedPreference
import viewmodel.ChatsViewModel

class ChatsFragment : Fragment() {

    private lateinit var binding: FragmentChatsBinding
    private lateinit var conversaAdapter: ConversaAdapter
    private val viewModel: ChatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val idUsuariLoguejat = SharedPreference.obtenirUsuariLoguejat(requireContext())

        conversaAdapter = ConversaAdapter(
            emptyList(),
            idUsuariLoguejat
        ) { conversa ->
            findNavController().navigate(ChatsFragmentDirections.actionChatsFragmentToChatFragment(conversa.id))
        }

        binding.rvConverses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conversaAdapter
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.chatsFragment -> true
                R.id.areesFragments -> { findNavController().navigate(R.id.areesFragments); true }
                R.id.historialFragment -> { findNavController().navigate(R.id.historialFragment); true }
                else -> false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    state.error?.let {
                        Toast.makeText(requireContext(), it.asString(requireContext()), Toast.LENGTH_SHORT).show()
                    }

                    if (state.converses.isEmpty() && !state.loading) {
                        binding.tvBuit.visibility = View.VISIBLE
                        binding.rvConverses.visibility = View.GONE
                    } else {
                        binding.tvBuit.visibility = View.GONE
                        binding.rvConverses.visibility = View.VISIBLE
                        conversaAdapter.updateData(state.converses)
                    }
                }
            }
        }

        idUsuariLoguejat?.let { viewModel.carregarConverses(it) }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigationView.selectedItemId = R.id.chatsFragment
    }

    companion object {
        @JvmStatic
        fun newInstance() = ChatsFragment()
    }
}