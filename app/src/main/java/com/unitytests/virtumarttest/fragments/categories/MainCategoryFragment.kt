package com.unitytests.virtumarttest.fragments.categories

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unitytests.virtumarttest.databinding.FragmentMainCategoryBinding

class MainCategoryFragment: Fragment() {
    private lateinit var binding: FragmentMainCategoryBinding

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
    ): View? {
        binding = FragmentMainCategoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryName = arguments?.getString(argCategoryName) ?: ""
        // Customize your fragment based on the category name
    }
}