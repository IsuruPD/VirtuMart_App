package com.unitytests.virtumarttest.fragments.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.Category
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.viewmodel.CategoryVM
import com.unitytests.virtumarttest.viewmodel.factory.BaseCategoryVMFac
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class KitchenFragment: BaseCategoryFragment() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    val viewModel by viewModels<CategoryVM>{
        BaseCategoryVMFac(firestore, Category.Kitchenware)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.topProductsBase.collectLatest {
                when(it){
                    is Resource.Loading ->{

                    }
                    is Resource.Success ->{
                        topBaseAdapter.differ.submitList(it.data)
                    }
                    is Resource.Error ->{
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.galleryProductsBase.collectLatest {
                when(it){
                    is Resource.Loading ->{

                    }
                    is Resource.Success ->{
                        catalogBaseAdapter.differ.submitList(it.data)
                    }
                    is Resource.Error ->{
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}