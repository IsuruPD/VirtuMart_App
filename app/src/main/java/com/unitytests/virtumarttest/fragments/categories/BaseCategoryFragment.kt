package com.unitytests.virtumarttest.fragments.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.adapters.GalleryProductsAdapter
import com.unitytests.virtumarttest.adapters.TopProductsAdapter
import com.unitytests.virtumarttest.databinding.FragmentBaseCategoryBinding
import com.unitytests.virtumarttest.util.showNavBarVisibility

open class BaseCategoryFragment: Fragment(R.layout.fragment_base_category) {

    lateinit var bindingBase: FragmentBaseCategoryBinding
    protected val topBaseAdapter: TopProductsAdapter by lazy{TopProductsAdapter()}
    protected val catalogBaseAdapter: GalleryProductsAdapter by lazy{GalleryProductsAdapter()}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingBase= FragmentBaseCategoryBinding.inflate(inflater)
        return bindingBase.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRvTopProductsBase()
        setupRvCategoryBase()

        // Display product views
        topBaseAdapter.onClick = {
            val bundle= Bundle().apply{putParcelable("product",it)}
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, bundle)
        }

        catalogBaseAdapter.onClick = {
            val bundle= Bundle().apply{putParcelable("product",it)}
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment, bundle)
        }

        // Top products base end reach
        bindingBase.rvTopProductsBaseCategory.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView,dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(!recyclerView.canScrollVertically(1) && dx != 0){
                    onTopBasePagingRequest()
                }
            }
        })
        // Gallery products base bottom reach
        bindingBase.nestedScrollBaseCategory.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener{v,_, scrollY,_,_ ->
            //Check if the end of the page is reached
            if(v.getChildAt(0).bottom <= v.height+scrollY){
                //if yes fetch the next batch of products from firebase
                onGalleryBasePagingRequest()
            }
        })
    }


    fun showTopBasePrgBrLoading(){
        bindingBase.prgbrTopUpdateBaseCategory.visibility=View.VISIBLE
    }

    fun showGalleryBasePrgBrLoading(){
        bindingBase.prgbrGalleryUpdateBaseCategory.visibility=View.VISIBLE
    }

    fun hideTopBasePrgBrLoading(){
        bindingBase.prgbrTopUpdateBaseCategory.visibility =View.GONE
    }

    fun hideGalleryBasePrgBrLoading(){
        bindingBase.prgbrGalleryUpdateBaseCategory.visibility=View.GONE
    }

    open fun onTopBasePagingRequest(){

    }

    open fun onGalleryBasePagingRequest(){

    }

    private fun setupRvTopProductsBase() {
        bindingBase.rvTopProductsBaseCategory.apply{
            layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter=topBaseAdapter
        }
    }

    private fun setupRvCategoryBase() {
        bindingBase.rvProductsCatalogueBaseCategory.apply{
            layoutManager= GridLayoutManager(requireContext(),2, GridLayoutManager.VERTICAL, false)
            adapter=catalogBaseAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        showNavBarVisibility()
    }
}