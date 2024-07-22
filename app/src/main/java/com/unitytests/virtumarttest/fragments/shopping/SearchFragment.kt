package com.unitytests.virtumarttest.fragments.shopping

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.adapters.SearchProductsAdapter
import com.unitytests.virtumarttest.databinding.FragmentSearchBinding
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.util.showNavBarVisibility
import com.unitytests.virtumarttest.viewmodel.SharedSearchVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchInputEditText: EditText
    private val sharedViewModel: SharedSearchVM by activityViewModels()
    private lateinit var searchProductsAdapter: SearchProductsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchInputEditText = view.findViewById(R.id.searchFragmentEdt)

        //Display products when opened
        val startSearchQuery= searchInputEditText.text.toString()
        sharedViewModel.searchProducts(startSearchQuery)

        setupSearchProductsView()

        searchProductsAdapter.onClick = {
            val bundle = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_searchFragment_to_productDetailsFragment, bundle)
        }

        searchInputEditText.doOnTextChanged { text, _, _, _ ->
            val query = text.toString()
            sharedViewModel.searchProducts(query)
        }

        binding.rvSearchProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    val query = searchInputEditText.text.toString()
                    sharedViewModel.searchProducts(query)
                }
            }
        })

        // Observe the search results
        lifecycleScope.launchWhenStarted {
            sharedViewModel.searchResults.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.prgbrSearchProducts.visibility=View.VISIBLE
                    }
                    is Resource.Success -> {
                        searchProductsAdapter.submitList(it.data)
                        if (it.data.isNullOrEmpty()) {
                            binding.prgbrSearchProducts.visibility=View.GONE
                            binding.txtNoSearchResultsMessage.visibility = View.VISIBLE
                        } else {
                            binding.prgbrSearchProducts.visibility=View.GONE
                            binding.txtNoSearchResultsMessage.visibility = View.GONE
                        }
                    }
                    is Resource.Error -> {
                        binding.prgbrSearchProducts.visibility=View.GONE
                        Toast.makeText(requireContext(), "Error occurred retrieving data: " + it.message.toString(), Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setupSearchProductsView() {
        searchProductsAdapter = SearchProductsAdapter()
        binding.rvSearchProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = searchProductsAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        showNavBarVisibility()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
