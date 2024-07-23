package com.unitytests.virtumarttest.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.core.widget.doOnTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.adapters.SearchProductsAdapter
import com.unitytests.virtumarttest.databinding.FragmentSearchBinding
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.util.hideNavBarVisibility
import com.unitytests.virtumarttest.util.showNavBarVisibility
import com.unitytests.virtumarttest.viewmodel.SharedSearchVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchInputEditText: EditText
    private val sharedViewModel: SharedSearchVM by activityViewModels()
    private lateinit var searchProductsAdapter: SearchProductsAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

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
        drawerLayout = view.findViewById(R.id.drawer_layout)
        navView = view.findViewById(R.id.nav_view)

        // Display products when opened
        val startSearchQuery = searchInputEditText.text.toString()
        sharedViewModel.searchProducts(startSearchQuery)

        // Set up the navigation view
        navView.setNavigationItemSelectedListener(this)

        // Fetch categories
        sharedViewModel.fetchCategories()

        //Get the drawer elements
        val menu = navView.menu.findItem(R.id.navProduct_category).subMenu


        // Inflate and add the custom title view
        val categoryTitleView = LayoutInflater.from(context).inflate(R.layout.navdrawer_title_styling, null) as TextView
        categoryTitleView.text = "Categories"
        navView.menu.findItem(R.id.navProduct_category).actionView = categoryTitleView

        // Inflate and add the radio group for sorting options
        val radioGroupView = LayoutInflater.from(context).inflate(R.layout.navdrawer_radio_buttons, null) as RadioGroup
        navView.menu.findItem(R.id.navToggle_sort_price).actionView = radioGroupView

        // Inflate and add the action buttons layout
        val actionButtonsView = LayoutInflater.from(context).inflate(R.layout.navdraw_buttons_styling, null) as LinearLayout
        navView.menu.findItem(R.id.nav_action_buttons).actionView = actionButtonsView

        // Set listeners for buttons
        val applyFiltersButton = actionButtonsView.findViewById<Button>(R.id.apply_filters)
        val clearFiltersButton = actionButtonsView.findViewById<Button>(R.id.clear_filters)

        applyFiltersButton.setOnClickListener {
            applyFilters()
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        clearFiltersButton.setOnClickListener {
            clearFilters()
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        // Handle drawer state changes
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                //binding.dummyNavBar.visibility = View.GONE
            }

            override fun onDrawerOpened(drawerView: View) {
                binding.dummyNavBar.visibility = View.VISIBLE
                hideNavBarVisibility()
            }

            override fun onDrawerClosed(drawerView: View) {
                binding.dummyNavBar.visibility = View.GONE
                showNavBarVisibility()
            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })

        // Handle the filter button click
        view.findViewById<ImageView>(R.id.itemFilterSF).setOnClickListener {
            binding.dummyNavBar.visibility = View.VISIBLE
            hideNavBarVisibility()
            drawerLayout.openDrawer(GravityCompat.END)
        }


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
                        binding.prgbrSearchProducts.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        searchProductsAdapter.submitList(it.data)
                        if (it.data.isNullOrEmpty()) {
                            binding.prgbrSearchProducts.visibility = View.GONE
                            binding.txtNoSearchResultsMessage.visibility = View.VISIBLE
                        } else {
                            binding.prgbrSearchProducts.visibility = View.GONE
                            binding.txtNoSearchResultsMessage.visibility = View.GONE
                        }
                    }
                    is Resource.Error -> {
                        binding.prgbrSearchProducts.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error occurred retrieving data: " + it.message.toString(), Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

        // Observe the categories
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.categories.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val categories = resource.data ?: emptyList()
                        if (menu != null) {
                            populateMenu(menu, categories)
                        }
                    }
                    is Resource.Error -> {
                        // Handle error
                    }
                    is Resource.Loading -> {
                        // Handle loading
                    }
                    is Resource.Unspecified -> {
                        // Handle unspecified
                    }
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

    private fun populateMenu(menu: Menu, categories: List<String>) {
        menu.clear()
        categories.forEach { category ->
            menu.add(Menu.NONE, Menu.NONE, Menu.NONE, category).apply {
                isCheckable = true
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle filter and sort item selection
//        when (item.itemId) {
//            R.id.navbtn_apply_filters -> {
//                // Apply the selected filters
//                applyFilters()
//                drawerLayout.closeDrawer(GravityCompat.END)
//                return true
//            }
//            R.id.navbtn_clear_filters -> {
//                // Clear all filters
//                clearFilters()
//                drawerLayout.closeDrawer(GravityCompat.END)
//                return true
//            }
//            else -> {
//                // Handle other filter/sort selections
//                return false
//            }
//        }
        return true
    }

    private fun applyFilters() {
        // Implement the logic to apply the selected filters
    }

    private fun clearFilters() {
        // Implement the logic to clear all filters
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
