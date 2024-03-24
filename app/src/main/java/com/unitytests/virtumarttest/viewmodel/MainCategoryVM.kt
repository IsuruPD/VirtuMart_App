package com.unitytests.virtumarttest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.Product
import com.unitytests.virtumarttest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainCategoryVM @Inject constructor(
    private val firestore: FirebaseFirestore
): ViewModel() {

    private val _topProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val topProduct: StateFlow<Resource<List<Product>>> = _topProducts

    private val _dealsProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val dealsProducts: StateFlow<Resource<List<Product>>> = _dealsProducts

    private val _galleryProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val galleryProducts: StateFlow<Resource<List<Product>>> = _galleryProducts

    private val pagingInfo  = PagingInfo()
    init{
        fetchTopProducts()
        fetchDealsProducts()
        fetchGalleryProducts()
    }
    fun fetchTopProducts(){
        if (!pagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _topProducts.emit(Resource.Loading())
            }
            val pageSize = 10 // Number of items per page
            val startIndex = (pagingInfo.page - 1) * pageSize
            val endIndex = startIndex + pageSize
            //Make the selection here using whereEqualTo("field","value") after collection
            firestore.collection("products")
                .limit(endIndex.toLong())
                .get()
                .addOnSuccessListener { result ->
                    val topProductsList = result.toObjects((Product::class.java))
                    viewModelScope.launch {
                        if (topProductsList.size < pageSize) {
                            // All items have been fetched
                            pagingInfo.isPagingEnd = true
                        }
                        _topProducts.emit(Resource.Success(topProductsList))
                    }
                    if (!pagingInfo.isPagingEnd) {
                        pagingInfo.page++
                    }
                }.addOnFailureListener {
                viewModelScope.launch {
                    _topProducts.emit(Resource.Error(it.message.toString()))
                }
            }
        }
    }

    fun fetchDealsProducts() {
        if (!pagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _dealsProducts.emit(Resource.Loading())
            }
            val pageSize = 10 // Number of items per page
            val startIndex = (pagingInfo.page - 1) * pageSize
            val endIndex = startIndex + pageSize
            firestore.collection("products")
                .limit(endIndex.toLong()) // Fetch items up to the end index
                .get()
                .addOnSuccessListener { result ->
                    val dealsProductsList = result.toObjects(Product::class.java)
                    viewModelScope.launch {
                        if (dealsProductsList.size < pageSize) {
                            // All items have been fetched
                            pagingInfo.isPagingEnd = true
                        }
                        _dealsProducts.emit(Resource.Success(dealsProductsList))
                    }
                    if (!pagingInfo.isPagingEnd) {
                        pagingInfo.page++
                    }
                }
                .addOnFailureListener { e ->
                    viewModelScope.launch {
                        _dealsProducts.emit(Resource.Error(e.message.toString()))
                    }
                }
        }
    }


    fun fetchGalleryProducts(){
        if(!pagingInfo.isPagingEnd){
            viewModelScope.launch{
                _galleryProducts.emit(Resource.Loading())
            }
            //Make the selection here using whereEqualTo("field","value") after collection
            firestore.collection("products").limit(pagingInfo.page * 10).get().addOnSuccessListener { result->
                val galleryProductsList=result.toObjects((Product::class.java))
                //
                pagingInfo.isPagingEnd = galleryProductsList == pagingInfo.prevGalleryProducts
                pagingInfo.prevGalleryProducts = galleryProductsList
                //
                viewModelScope.launch {
                    _galleryProducts.emit(Resource.Success(galleryProductsList))
                }
                pagingInfo.page++
            }.addOnFailureListener{
                viewModelScope.launch {
                    _galleryProducts.emit(Resource.Error(it.message.toString()))
                }
            }
        }
    }
}

// Page number initializing before
internal data class PagingInfo(
    var page: Long = 1,
    //To avoid making more requests from firebase if the items are over to save bandwidth
    var prevGalleryProducts: List<Product> = emptyList(),
    var isPagingEnd: Boolean = false

)