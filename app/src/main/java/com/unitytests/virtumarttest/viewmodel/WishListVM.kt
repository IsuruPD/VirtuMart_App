package com.unitytests.virtumarttest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.Product
import com.unitytests.virtumarttest.data.WishListProducts
import com.unitytests.virtumarttest.firebase.CartHandleFirebase
import com.unitytests.virtumarttest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishListVM @Inject constructor (
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: CartHandleFirebase
    ): ViewModel() {

    // Display products in wish list
    private val _wishListProducts= MutableStateFlow<Resource<List<WishListProducts>>>(Resource.Unspecified())
    val wishList = _wishListProducts.asStateFlow()

    private val _removeFromWishList = MutableStateFlow<Resource<WishListProducts>>(Resource.Unspecified())
    val removeFromWishList = _removeFromWishList.asStateFlow()

    // Check Product in Wish List
    private val _isProductInWishList = MutableStateFlow(false)
    val isProductInWishList = _isProductInWishList.asStateFlow()

    private var wishListProductDocuments = emptyList<DocumentSnapshot>()


    init{
        getWishListProducts()
    }

    private fun getWishListProducts(){
        viewModelScope.launch {
            _wishListProducts.emit(Resource.Loading())
        }
        firestore.collection("user").document(auth.uid!!).collection("wishlist")
        .addSnapshotListener { value, error ->
            if(error !=null || value == null){
                viewModelScope.launch {
                    _wishListProducts.emit(Resource.Error(error?.message.toString()))
                }
            }else{
                wishListProductDocuments = value.documents
                val wishListProducts = value.toObjects(WishListProducts::class.java)
                viewModelScope.launch{
                    _wishListProducts.emit(Resource.Success(wishListProducts))
                }
            }
        }
    }

    fun removeFromWishList(wishListProducts: WishListProducts) {
        viewModelScope.launch {
            _removeFromWishList.emit(Resource.Loading())
            firestore.collection("user").document(auth.uid!!).collection("wishlist")
                .whereEqualTo("product.productId", wishListProducts.product.productId)
                .get()
                .addOnSuccessListener {
                    it.documents.let { documents ->
                        val documentId = documents.first().id
                        removeProductFromWishList(documentId)
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch { _removeFromWishList.emit(Resource.Error(it.message.toString())) }
                }
        }
    }

    private fun removeProductFromWishList(documentId: String) {
        firestore.collection("user").document(auth.uid!!).collection("wishlist")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                viewModelScope.launch {
                    // Creating a placeholder WishListProducts object
                    val placeholderWishListProduct = WishListProducts(
                        product = Product(),
                        quantity = 0,
                        selectedColor = 0,
                        selectedSize = null
                    )
                    _removeFromWishList.emit(Resource.Success(placeholderWishListProduct))
                    _isProductInWishList.value = false
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _removeFromWishList.emit(Resource.Error(it.message.toString()))
                }
            }
    }
}