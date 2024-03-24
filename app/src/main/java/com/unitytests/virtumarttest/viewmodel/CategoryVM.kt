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

    init{
        fetchTopProductsBase()
        fetchGalleryProductsBase()
    }
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

    fun fetchGalleryProductsBase(){
        viewModelScope.launch {
            _galleryProductsBase.emit(Resource.Loading())
        }
        firestore.collection("products")
            .whereEqualTo("category",category.category)
            //.whereNotEqualTo("offerPercentage",null)
            .get()
            .addOnSuccessListener {
                val products=it.toObjects(Product::class.java)
                viewModelScope.launch{
                    _galleryProductsBase.emit(Resource.Success(products))
                }
            }
            .addOnFailureListener{
                viewModelScope.launch {
                    _galleryProductsBase.emit(Resource.Error(it.message.toString()))
                }
            }
    }
}