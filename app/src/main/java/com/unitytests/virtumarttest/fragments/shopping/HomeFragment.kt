package com.unitytests.virtumarttest.fragments.shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.adapters.HomeViewPagerFragmentAdapter
import com.unitytests.virtumarttest.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayoutMediator


class HomeFragment: Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchCategoriesFromFirestore()
    }

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

        val categoriesFragments = categoryList.map { CategoryFragment.newInstance(it) }

        val adapter = HomeViewPagerFragmentAdapter(this, categoriesFragments)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = categoryList[position]
        }.attach()
    }
}