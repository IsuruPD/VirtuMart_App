package com.unitytests.virtumarttest.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.Product
import com.unitytests.virtumarttest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainCategoryVM @Inject constructor(
    private val firestore: FirebaseFirestore): ViewModel() {

    private val _topProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val topProduct: StateFlow<Resource<List<Product>>> = _topProducts

    private val _dealsProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val dealsProducts: StateFlow<Resource<List<Product>>> = _dealsProducts

    private val _galleryProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val galleryProducts: StateFlow<Resource<List<Product>>> = _galleryProducts

    private val _searchResults = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val searchResults: StateFlow<Resource<List<Product>>> = _searchResults


    private val topPagingInfoMain  = PagingInfo()
    private val dealsPagingInfoMain  = PagingInfo()
    private val galleryPagingInfoMain  = PagingInfo()
//    private val searchPagingInfoMain  = PagingInfo()
    init{
        fetchTopProductsMain()
        fetchDealsProductsMain()
        fetchGalleryProductsMain()
    }
//    fun fetchTopProducts(){
//        if (!pagingInfo.isPagingEnd) {
//            viewModelScope.launch {
//                _topProducts.emit(Resource.Loading())
//            }
//            val pageSize = 10 // Number of items per page
//            val startIndex = (pagingInfo.page - 1) * pageSize
//            val endIndex = startIndex + pageSize
//            //Make the selection here using whereEqualTo("field","value") after collection
//            firestore.collection("products")
//                .limit(endIndex.toLong())
//                .get()
//                .addOnSuccessListener { result ->
//                    val topProductsList = result.toObjects((Product::class.java))
//                    viewModelScope.launch {
//                        if (topProductsList.size < pageSize) {
//                            // All items have been fetched
//                            pagingInfo.isPagingEnd = true
//                        }
//                        _topProducts.emit(Resource.Success(topProductsList))
//                    }
//                    if (!pagingInfo.isPagingEnd) {
//                        pagingInfo.page++
//                    }
//                }.addOnFailureListener {
//                viewModelScope.launch {
//                    _topProducts.emit(Resource.Error(it.message.toString()))
//                }
//            }
//        }
//    }

//    fun fetchDealsProducts() {
//        if (!pagingInfo.isPagingEnd) {
//            viewModelScope.launch {
//                _dealsProducts.emit(Resource.Loading())
//            }
//            val pageSize = 10 // Number of items per page
//            val startIndex = (pagingInfo.page - 1) * pageSize
//            val endIndex = startIndex + pageSize
//            firestore.collection("products")
//                .limit(endIndex.toLong()) // Fetch items up to the end index
//                .get()
//                .addOnSuccessListener { result ->
//                    val dealsProductsList = result.toObjects(Product::class.java)
//                    viewModelScope.launch {
//                        if (dealsProductsList.size < pageSize) {
//                            // All items have been fetched
//                            pagingInfo.isPagingEnd = true
//                        }
//                        _dealsProducts.emit(Resource.Success(dealsProductsList))
//                    }
//                    if (!pagingInfo.isPagingEnd) {
//                        pagingInfo.page++
//                    }
//                }
//                .addOnFailureListener { e ->
//                    viewModelScope.launch {
//                        _dealsProducts.emit(Resource.Error(e.message.toString()))
//                    }
//                }
//        }
//    }
    fun fetchTopProductsMain(){
        if(!topPagingInfoMain.isPagingEnd){
            viewModelScope.launch{
                _topProducts.emit(Resource.Loading())
            }
            //Make the selection here using whereEqualTo("field","value") after collection
            firestore.collection("products").limit(topPagingInfoMain.page * 10).get().addOnSuccessListener { result->
                val topProductsList=result.toObjects((Product::class.java))
                //
                topPagingInfoMain.isPagingEnd = topProductsList == topPagingInfoMain.prevTopProducts
                topPagingInfoMain.prevTopProducts = topProductsList
                //
                viewModelScope.launch {
                    _topProducts.emit(Resource.Success(topProductsList))
                }
                topPagingInfoMain.page++
            }.addOnFailureListener{
                viewModelScope.launch {
                    _topProducts.emit(Resource.Error(it.message.toString()))
                }
            }
        }
    }

    fun fetchDealsProductsMain(){
        if(!dealsPagingInfoMain.isPagingEnd){
            viewModelScope.launch{
                _dealsProducts.emit(Resource.Loading())
            }
            //Make the selection here using whereEqualTo("field","value") after collection
            firestore.collection("products").limit(dealsPagingInfoMain.page * 10).get().addOnSuccessListener { result->
                val dealsProductsList=result.toObjects((Product::class.java))
                //
                dealsPagingInfoMain.isPagingEnd = dealsProductsList == dealsPagingInfoMain.prevDealsProducts
                dealsPagingInfoMain.prevDealsProducts = dealsProductsList
                //
                viewModelScope.launch {
                    _dealsProducts.emit(Resource.Success(dealsProductsList))
                }
                dealsPagingInfoMain.page++
            }.addOnFailureListener{
                viewModelScope.launch {
                    _dealsProducts.emit(Resource.Error(it.message.toString()))
                }
            }
        }
    }

    fun fetchGalleryProductsMain(){
        if(!galleryPagingInfoMain.isPagingEnd){
            viewModelScope.launch{
                _galleryProducts.emit(Resource.Loading())
            }
            //Make the selection here using whereEqualTo("field","value") after collection
            firestore.collection("products").limit(galleryPagingInfoMain.page * 10).get().addOnSuccessListener { result->
                val galleryProductsList=result.toObjects((Product::class.java))
                //
                galleryPagingInfoMain.isPagingEnd = galleryProductsList == galleryPagingInfoMain.prevGalleryProducts
                galleryPagingInfoMain.prevGalleryProducts = galleryProductsList
                //
                viewModelScope.launch {
                    _galleryProducts.emit(Resource.Success(galleryProductsList))
                }
                galleryPagingInfoMain.page++
            }.addOnFailureListener{
                viewModelScope.launch {
                    _galleryProducts.emit(Resource.Error(it.message.toString()))
                }
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _searchResults.emit(Resource.Loading())
            try {
                val products = firestore.collection("products")
                    .get()
                    .await()
                    .toObjects(Product::class.java)

                val results = products.filter {
                    it.productName.contains(query, ignoreCase = true)
                }

                _searchResults.emit(Resource.Success(results))
            } catch (e: Exception) {
                _searchResults.emit(Resource.Error(e.message ?: "Unknown error"))
            }
        }
    }
}

// Page number initializing before
internal data class PagingInfo(
    var page: Long = 1,
    //To avoid making more requests from firebase if the items are over to save bandwidth
    var prevTopProducts: List<Product> = emptyList(),
    var prevDealsProducts: List<Product> = emptyList(),
    var prevGalleryProducts: List<Product> = emptyList(),
    var prevSearchProducts: List<Product> = emptyList(),
    var isPagingEnd: Boolean = false

)