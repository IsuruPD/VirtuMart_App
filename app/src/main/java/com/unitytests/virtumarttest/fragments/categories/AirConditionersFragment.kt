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
class AirConditionersFragment: BaseCategoryFragment() {
    @Inject
    lateinit var firestore: FirebaseFirestore

    val viewModel by viewModels<CategoryVM>{
        BaseCategoryVMFac(firestore, Category.AirConditioner)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.topProductsBase.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        showTopBasePrgBrLoading()
                    }
                    is Resource.Success ->{
                        topBaseAdapter.differ.submitList(it.data)
                        hideTopBasePrgBrLoading()
                    }
                    is Resource.Error ->{
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG).show()
                        hideTopBasePrgBrLoading()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.galleryProductsBase.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        showGalleryBasePrgBrLoading()
                    }
                    is Resource.Success ->{
                        catalogBaseAdapter.differ.submitList(it.data)
                        hideGalleryBasePrgBrLoading()
                    }
                    is Resource.Error ->{
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_LONG).show()
                        hideGalleryBasePrgBrLoading()
                    }
                    else -> Unit
                }
            }
        }
    }
    override fun onTopBasePagingRequest() {
        // Category Top End Reach
        viewModel.fetchTopProductsBase()
    }

    override fun onGalleryBasePagingRequest() {
        // Gallery Bottom Reach
        //if yes fetch the next batch of products from firebase
        viewModel.fetchGalleryProductsBase()

    }
}