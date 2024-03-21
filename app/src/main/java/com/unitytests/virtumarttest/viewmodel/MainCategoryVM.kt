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

    init{
        fetchTopProducts()
        fetchDealsProducts()
        fetchGalleryProducts()
    }
    fun fetchTopProducts(){
        viewModelScope.launch{
            _topProducts.emit(Resource.Loading())
        }
        //Make the selection here using whereEqualTo("field","value") after collection
        firestore.collection("products").get().addOnSuccessListener { result->
            val topProductsList=result.toObjects((Product::class.java))
            viewModelScope.launch {
                _topProducts.emit(Resource.Success(topProductsList))
            }
        }.addOnFailureListener{
            viewModelScope.launch {
                _topProducts.emit(Resource.Error(it.message.toString()))
            }
        }
    }

    fun fetchDealsProducts(){
        viewModelScope.launch{
            _dealsProducts.emit(Resource.Loading())
        }
        //Make the selection here using whereEqualTo("field","value") after collection
        firestore.collection("products").get().addOnSuccessListener { result->
            val dealsProductsList=result.toObjects((Product::class.java))
            viewModelScope.launch {
                _dealsProducts.emit(Resource.Success(dealsProductsList))
            }
        }.addOnFailureListener{
            viewModelScope.launch {
                _dealsProducts.emit(Resource.Error(it.message.toString()))
            }
        }
    }

    fun fetchGalleryProducts(){
        viewModelScope.launch{
            _galleryProducts.emit(Resource.Loading())
        }
        //Make the selection here using whereEqualTo("field","value") after collection
        firestore.collection("products").get().addOnSuccessListener { result->
            val galleryProductsList=result.toObjects((Product::class.java))
            viewModelScope.launch {
                _galleryProducts.emit(Resource.Success(galleryProductsList))
            }
        }.addOnFailureListener{
            viewModelScope.launch {
                _galleryProducts.emit(Resource.Error(it.message.toString()))
            }
        }
    }
}