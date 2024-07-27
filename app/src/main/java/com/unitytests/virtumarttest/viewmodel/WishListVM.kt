package com.unitytests.virtumarttest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.CartProducts
import com.unitytests.virtumarttest.data.WishListProducts
import com.unitytests.virtumarttest.firebase.FirebaseCommonClass
import com.unitytests.virtumarttest.helper.getProductPrice
import com.unitytests.virtumarttest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class WishListVM @Inject constructor (
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommonClass
    ): ViewModel() {

    // Display products in wish list
    private val _wishListProducts= MutableStateFlow<Resource<List<WishListProducts>>>(Resource.Unspecified())
    val wishList = _wishListProducts.asStateFlow()

    private var wishListProductDocuments = emptyList<DocumentSnapshot>()

    // Remove products from cart
    private val _deleteCartItem = MutableSharedFlow<CartProducts>()
    val deleteCartItem = _deleteCartItem.asSharedFlow()


    fun deleteCartItem(wishListProducts: WishListProducts){
        val index = wishList.value.data?.indexOf((wishListProducts))
        if(index !=null && index != -1) {
            val documentId = wishListProductDocuments[index].id
            firestore.collection("user").document(auth.uid!!).collection("wishlist")
                .document(documentId).delete()
        }
    }

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
}