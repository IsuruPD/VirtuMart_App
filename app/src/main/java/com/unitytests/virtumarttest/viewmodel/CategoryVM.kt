package com.unitytests.virtumarttest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.Category
import com.unitytests.virtumarttest.data.Product
import com.unitytests.virtumarttest.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryVM constructor(
    private val firestore: FirebaseFirestore,
    private val category: Category

):ViewModel(){
    private val _topProductsBase= MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val topProductsBase=_topProductsBase.asStateFlow()

    private val _galleryProductsBase= MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val galleryProductsBase=_galleryProductsBase.asStateFlow()

    private val topPagingInfoBase  = CategoriesPagingInfo()
    private val galleryPagingInfoBase  = CategoriesPagingInfo()


    init{
        fetchTopProductsBase()
        fetchGalleryProductsBase()
    }

    /*
    // Default top product fetching function

    fun fetchTopProductsBase(){
        viewModelScope.launch {
            _topProductsBase.emit(Resource.Loading())
        }
        firestore.collection("products")
            .whereEqualTo("category",category.category)
            //.whereNotEqualTo("offerPercentage",null)
            .get()
            .addOnSuccessListener {
                val products=it.toObjects(Product::class.java)
                viewModelScope.launch{
                    _topProductsBase.emit(Resource.Success(products))
                }
            }
            .addOnFailureListener{
                viewModelScope.launch {
                    _topProductsBase.emit(Resource.Error(it.message.toString()))
                }
            }
    }
     */

    // *****Fixed******
    //This method needs an update as for every swipe, the loading method is called
    fun fetchTopProductsBase(){
        if(!topPagingInfoBase.isPagingEnd){
            viewModelScope.launch{
                _topProductsBase.emit(Resource.Loading())
            }
            //Make the selection here using whereEqualTo("field","value") after collection
            firestore.collection("products")
                .whereEqualTo("category",category.category)
                .limit(topPagingInfoBase.page * 10)
                .get()
                .addOnSuccessListener { result->
                    val topProductsList=result.toObjects((Product::class.java))
                    //
                    topPagingInfoBase.isPagingEnd = topProductsList == topPagingInfoBase.prevTopProducts
                    topPagingInfoBase.prevTopProducts = topProductsList
                    //
                    viewModelScope.launch {
                        _topProductsBase.emit(Resource.Success(topProductsList))
                    }
                    topPagingInfoBase.page++
                }.addOnFailureListener{
                    viewModelScope.launch {
                        _topProductsBase.emit(Resource.Error(it.message.toString()))
                }
            }
        }
    }

    fun fetchGalleryProductsBase(){
        if(!galleryPagingInfoBase.isPagingEnd){
            viewModelScope.launch{
                _galleryProductsBase.emit(Resource.Loading())
            }
            //Make the selection here using whereEqualTo("field","value") after collection
            firestore.collection("products")
                .whereEqualTo("category",category.category)
                .limit(galleryPagingInfoBase.page * 10)
                .get()
                .addOnSuccessListener { result->
                    val galleryProductsList=result.toObjects((Product::class.java))
                    //
                    galleryPagingInfoBase.isPagingEnd = galleryProductsList == galleryPagingInfoBase.prevGalleryProducts
                    galleryPagingInfoBase.prevGalleryProducts = galleryProductsList
                    //
                    viewModelScope.launch {
                        _galleryProductsBase.emit(Resource.Success(galleryProductsList))
                    }
                    galleryPagingInfoBase.page++
                }.addOnFailureListener{
                    viewModelScope.launch {
                        _galleryProductsBase.emit(Resource.Error(it.message.toString()))
                    }
            }
        }
    }

}

// Page number initializing before
internal data class CategoriesPagingInfo(
    var page: Long = 1,
    //To avoid making more requests from firebase if the items are over to save bandwidth
    var prevTopProducts: List<Product> = emptyList(),
    var prevGalleryProducts: List<Product> = emptyList(),
    var isPagingEnd: Boolean = false
)