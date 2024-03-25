package com.unitytests.virtumarttest.fragments.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.adapters.GalleryProductsAdapter
import com.unitytests.virtumarttest.adapters.TopProductsAdapter
import com.unitytests.virtumarttest.databinding.FragmentBaseCategoryBinding

open class BaseCategoryFragment: Fragment(R.layout.fragment_base_category) {

    private lateinit var binding: FragmentBaseCategoryBinding
    protected val topBaseAdapter: TopProductsAdapter by lazy{TopProductsAdapter()}
    protected val catalogBaseAdapter: GalleryProductsAdapter by lazy{GalleryProductsAdapter()}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentBaseCategoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRvTopProductsBase()
        setupRvCategoryBase()

        // Top products base end reach
        binding.rvTopProductsBaseCategory.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView,dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(!recyclerView.canScrollVertically(1) && dx != 0){
                    onTopBasePagingRequest()
                }
            }
        })
        // Gallery products base bottom reach
        binding.nestedScrollBaseCategory.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener{v,_, scrollY,_,_ ->
            //Check if the end of the page is reached
            if(v.getChildAt(0).bottom <= v.height+scrollY){
                //if yes fetch the next batch of products from firebase
                onGalleryBasePagingRequest()
            }
        })
    }


    fun showTopBasePrgBrLoading(){
        binding.prgbrTopUpdateBaseCategory.visibility=View.VISIBLE
    }

    fun showGalleryBasePrgBrLoading(){
        binding.prgbrGalleryUpdateBaseCategory.visibility=View.VISIBLE
    }

    fun hideTopBasePrgBrLoading(){
        binding.prgbrTopUpdateBaseCategory.visibility =View.GONE
    }

    fun hideGalleryBasePrgBrLoading(){
        binding.prgbrGalleryUpdateBaseCategory.visibility=View.GONE
    }

    open fun onTopBasePagingRequest(){

    }

    open fun onGalleryBasePagingRequest(){

    }

    private fun setupRvTopProductsBase() {
        binding.rvTopProductsBaseCategory.apply{
            layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter=topBaseAdapter
        }
    }

    private fun setupRvCategoryBase() {
        binding.rvProductsCatalogueBaseCategory.apply{
            layoutManager= GridLayoutManager(requireContext(),2, GridLayoutManager.VERTICAL, false)
            adapter=catalogBaseAdapter
        }
    }
}