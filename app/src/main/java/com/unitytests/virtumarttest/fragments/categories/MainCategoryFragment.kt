package com.unitytests.virtumarttest.fragments.categories

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.adapters.DealsProductsAdapter
import com.unitytests.virtumarttest.adapters.GalleryProductsAdapter
import com.unitytests.virtumarttest.adapters.SearchProductsAdapter
import com.unitytests.virtumarttest.adapters.TopProductsAdapter
import com.unitytests.virtumarttest.data.Product
import com.unitytests.virtumarttest.databinding.FragmentMainCategoryBinding
import com.unitytests.virtumarttest.fragments.shopping.ProductDetailsFragment
import com.unitytests.virtumarttest.fragments.shopping.SearchableFragment
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.util.showNavBarVisibility
import com.unitytests.virtumarttest.viewmodel.MainCategoryVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainCategoryFragment: Fragment(R.layout.fragment_main_category), SearchableFragment {
    private lateinit var binding: FragmentMainCategoryBinding
    private lateinit var topProductsAdapter: TopProductsAdapter
    private lateinit var dealsProductsAdapter: DealsProductsAdapter
    private lateinit var galleryProductsAdapter: GalleryProductsAdapter
    private lateinit var searchProductsAdapter: SearchProductsAdapter
    private val viewModel by viewModels<MainCategoryVM>()

    //Category Tabs
    companion object {
        private const val argCategoryName = "categoryName"

        fun newInstance(categoryName: String): MainCategoryFragment {
            val fragment = MainCategoryFragment()
            val args = Bundle()
            args.putString(argCategoryName, categoryName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainCategoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTopProductsView()
        setupDealsProductsView()
        setupGalleryProductsView()

        setupSearchProductsView()

        // Navigate to product view page
        topProductsAdapter.onClick = {
            val bundle= Bundle().apply{putParcelable("product",it)}
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, bundle)
        }
        dealsProductsAdapter.onClick = {
            val bundle= Bundle().apply{putParcelable("product",it)}
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, bundle)
        }
        galleryProductsAdapter.onClick = {
            val bundle= Bundle().apply{putParcelable("product",it)}
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, bundle)
        }


        // For the top products recycler
        lifecycleScope.launchWhenStarted {
            viewModel.topProduct.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.prgbrTopUpdateMainCategory.visibility=View.VISIBLE
                    }
                    is Resource.Success -> {
                        topProductsAdapter.differ.submitList(it.data)
                        binding.prgbrTopUpdateMainCategory.visibility=View.GONE
                    }
                    is Resource.Error -> {
                        binding.prgbrTopUpdateMainCategory.visibility=View.GONE
                        Log.e("MainCategoryFragment", it.message.toString())
                        Toast.makeText(requireContext(), "Error occurred retrieving data: " + it.message.toString(), Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
             }
        }

        // For the deals products recycler
        lifecycleScope.launchWhenStarted {
            viewModel.dealsProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.prgbrDealsUpdateMainCategory.visibility=View.VISIBLE
                    }
                    is Resource.Success -> {
                        dealsProductsAdapter.differ.submitList(it.data)
                        binding.prgbrDealsUpdateMainCategory.visibility=View.GONE
                    }
                    is Resource.Error -> {
                        binding.prgbrDealsUpdateMainCategory.visibility=View.GONE
                        Log.e("MainCategoryFragment", it.message.toString())
                        Toast.makeText(requireContext(), "Error occurred retrieving data: " + it.message.toString(), Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

        // For the gallery products recycler
        lifecycleScope.launchWhenStarted {
            viewModel.galleryProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.prgbrGalleryUpdateMainCategory.visibility=View.VISIBLE
                    }
                    is Resource.Success -> {
                        galleryProductsAdapter.differ.submitList(it.data)
                        binding.prgbrGalleryUpdateMainCategory.visibility=View.GONE
                    }
                    is Resource.Error -> {
                        binding.prgbrGalleryUpdateMainCategory.visibility=View.GONE
                        Log.e("MainCategoryFragment", it.message.toString())
                        Toast.makeText(requireContext(), "Error occurred retrieving data: " + it.message.toString(), Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

    // Top products end reach
    binding.rvTopProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (!recyclerView.canScrollHorizontally(1)) {
                // End of RecyclerView reached by swiping to the right
                viewModel.fetchTopProductsMain()
            }
        }
    })

    // Deals end reach
    binding.rvDealsProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (!recyclerView.canScrollHorizontally(1)) {
                // End of RecyclerView reached by swiping to the right
                viewModel.fetchDealsProductsMain()
            }
        }
    })

    // Gallery bottom reach
    binding.nestedScrollMainCategory.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener{v,_, scrollY,_,_ ->
        //Check if the end of the page is reached
        if(v.getChildAt(0).bottom <= v.height+scrollY){
            //if yes fetch the next batch of products from firebase
            viewModel.fetchGalleryProductsMain()
        }
    })

    // Category Tabs
        val categoryName = arguments?.getString(argCategoryName) ?: ""
        // Customizing fragment based on the category name
    }

    private fun setupTopProductsView(){
        topProductsAdapter = TopProductsAdapter()
        binding.rvTopProducts.apply{
            layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter=topProductsAdapter
        }
    }

    private fun setupDealsProductsView() {
        dealsProductsAdapter = DealsProductsAdapter()
        binding.rvDealsProducts.apply{
            layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter=dealsProductsAdapter
        }
    }
    private fun setupGalleryProductsView() {
        galleryProductsAdapter= GalleryProductsAdapter()
        binding.rvProductsCatalogue.apply{
            layoutManager=GridLayoutManager(requireContext(),2,GridLayoutManager.VERTICAL, false)
            adapter=galleryProductsAdapter
        }
    }

    private fun setupSearchProductsView() {
        searchProductsAdapter = SearchProductsAdapter()
        binding.rvProductsCatalogue.apply {
            layoutManager = GridLayoutManager(requireContext(),2, GridLayoutManager.VERTICAL, false)
            adapter = searchProductsAdapter
        }
    }

    override fun onSearchQueryChanged(query: String) {
        viewModel.filterProducts(query)
    }

    private fun hideLoading(){
        binding.prgbrMainCategory.visibility=View.GONE
    }

    private fun showLoading(){
        binding.prgbrMainCategory.visibility=View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        showNavBarVisibility()
    }
}