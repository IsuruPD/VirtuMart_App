package com.unitytests.virtumarttest.fragments.shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.adapters.HomeViewPagerFragmentAdapter
import com.unitytests.virtumarttest.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.unitytests.virtumarttest.fragments.categories.AirConditionersFragment
import com.unitytests.virtumarttest.fragments.categories.CleaningFragment
import com.unitytests.virtumarttest.fragments.categories.EntertainmentFragment
import com.unitytests.virtumarttest.fragments.categories.HealthandWellnessFragment
import com.unitytests.virtumarttest.fragments.categories.HomeDecorFragment
import com.unitytests.virtumarttest.fragments.categories.KitchenFragment
import com.unitytests.virtumarttest.fragments.categories.LaundryFragment
import com.unitytests.virtumarttest.fragments.categories.MainCategoryFragment
import com.unitytests.virtumarttest.fragments.categories.PersonalCareFragment
import com.unitytests.virtumarttest.viewmodel.MainCategoryVM
import com.unitytests.virtumarttest.viewmodel.SharedSearchVM


class HomeFragment: Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    //private val sharedViewModel: SharedSearchVM by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //fetchCategoriesFromFirestore()

        val searchInputEditText = view.findViewById<EditText>(R.id.searchEditText)

        searchInputEditText.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

//        searchInputEditText.doOnTextChanged { text, _, _, _ ->
//            val query = text.toString()
//            sharedViewModel.searchProducts(query)
//
//            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
//        }

        //Disable the swipe motion to different tabs
        binding.viewPagerHome.isUserInputEnabled=false

        val categoriesFragments = arrayListOf<Fragment>(
            MainCategoryFragment(),
            KitchenFragment(),
            LaundryFragment(),
            CleaningFragment(),
            EntertainmentFragment(),
            AirConditionersFragment(),
            HomeDecorFragment(),
            PersonalCareFragment(),
            HealthandWellnessFragment()
        )
        val viewPager2Adapter = HomeViewPagerFragmentAdapter(categoriesFragments,childFragmentManager, lifecycle)
        binding.viewPagerHome.adapter=viewPager2Adapter
        TabLayoutMediator(binding.tabLayoutHome, binding.viewPagerHome){tab, position->
            when(position){
                0-> tab.text="All"
                1-> tab.text="Kitchenware"
                2-> tab.text="Laundry"
                3-> tab.text="Cleaning"
                4-> tab.text="Entertainment"
                5-> tab.text="Air Conditioners"
                6-> tab.text="Home Decor"
                7-> tab.text="Personal Care"
                8-> tab.text="Health and Wellness"
            }
        }.attach()
    }
/*
    private fun fetchCategoriesFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val categoriesRef = db.collection("Categories")

        categoriesRef.orderBy("rank")//Created the field "rank" in documents to order the tabs
            .get()
            .addOnSuccessListener { result ->
                if (result != null) {
                    val categoryList = mutableListOf<String>()

                    for (document in result.documents) {
                        val categoryName = document.getString("name")
                        if (categoryName != null) {
                            categoryList.add(categoryName)
                            //Log.i("Name:", categoryName)
                        }
                    }

                    setupTabs(categoryList)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error fetching categories: $exception")
                // Mandatory
                // Handle errors
                // What to be done if the categories were not found
                // Logout or kill the app
            }
    }

    private fun setupTabs(categoryList: List<String>) {
        val tabLayout = binding.tabLayoutHome
        val viewPager = binding.viewPagerHome

        val categoriesFragments = categoryList.map { MainCategoryFragment.newInstance(it) }

        val adapter = HomeViewPagerFragmentAdapter(this, categoriesFragments)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = categoryList[position]
        }.attach()
    }
*/
}