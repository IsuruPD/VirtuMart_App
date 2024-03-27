package com.unitytests.virtumarttest.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.databinding.FragmentProductDetailsBinding

class ProductDetailsFragment: Fragment(R.layout.fragment_product_details) {
    private lateinit var binding: FragmentProductDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductDetailsBinding.inflate(inflater)
        return binding.root
    }
}