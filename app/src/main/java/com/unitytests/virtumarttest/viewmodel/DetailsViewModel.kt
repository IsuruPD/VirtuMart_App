package com.unitytests.virtumarttest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.CartProducts
import com.unitytests.virtumarttest.data.Product
import com.unitytests.virtumarttest.data.WishListProducts
import com.unitytests.virtumarttest.firebase.FirebaseCommonClass
import com.unitytests.virtumarttest.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommonClass
): ViewModel() {
    // Add to Cart
    private val _addToCart = MutableStateFlow<Resource<CartProducts>>(Resource.Unspecified())
    val addToCart = _addToCart.asStateFlow()

    // Add to Wish List
    private val _addToWishList = MutableStateFlow<Resource<WishListProducts>>(Resource.Unspecified())
    val addToWishList = _addToWishList.asStateFlow()

    private val _removeFromWishList = MutableStateFlow<Resource<WishListProducts>>(Resource.Unspecified())
    val removeFromWishList = _removeFromWishList.asStateFlow()

    // Check Product in Wish List
    private val _isProductInWishList = MutableStateFlow(false)
    val isProductInWishList = _isProductInWishList.asStateFlow()

    fun addUpdateProductInCart(cartProducts: CartProducts){
        viewModelScope.launch { _addToCart.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("cart")
            .whereEqualTo("product.productId", cartProducts.product.productId)
            .get()
            .addOnSuccessListener{
                it.documents.let{
                    if(it.isEmpty()){ // Add the item to the cart
                        addNewProduct(cartProducts)
                    }else{
                        val product =it.first().toObject(CartProducts::class.java)
                        if(product==cartProducts){ //If the product already exists, then increase the quantity
                            val documentId= it.first().id
                            increaseQuantity(documentId, cartProducts)
                        }else{ //Add a new item to the cart
                            addNewProduct(cartProducts)
                        }
                    }
                }
            }.addOnFailureListener{
                viewModelScope.launch { _addToCart.emit(Resource.Error(it.message.toString())) }
            }
    }

    private fun addNewProduct(cartProducts: CartProducts){
        firebaseCommon.addProductToCart(cartProducts){ addProducts, e->
            viewModelScope.launch{
                if(e== null){
                    _addToCart.emit(Resource.Success(addProducts!!))
                }else{
                    _addToCart.emit(Resource.Error(e.message.toString()))
                }
            }
        }
    }

    private fun increaseQuantity(documentId: String, cartProducts: CartProducts){
        firebaseCommon.increaseQuantity(documentId){ _, e->
            viewModelScope.launch{
                if(e== null){
                    _addToCart.emit(Resource.Success(cartProducts!!))
                }else{
                    _addToCart.emit(Resource.Error(e.message.toString()))
                }
            }
        }
    }


    // Add to WishList
    fun toggleWishList(wishListProducts: WishListProducts) {
        viewModelScope.launch {
            _addToWishList.emit(Resource.Loading())
            firestore.collection("user").document(auth.uid!!).collection("wishlist")
                .whereEqualTo("product.productId", wishListProducts.product.productId)
                .get()
                .addOnSuccessListener {
                    it.documents.let { documents ->
                        if (documents.isEmpty()) {
                            addNewProductToWishList(wishListProducts)
                        } else {
                            val documentId = documents.first().id
                            removeProductFromWishList(documentId)
                        }
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch { _addToWishList.emit(Resource.Error(it.message.toString())) }
                }
        }
    }

    private fun addNewProductToWishList(wishListProducts: WishListProducts) {
        firestore.collection("user").document(auth.uid!!).collection("wishlist")
            .document()
            .set(wishListProducts)
            .addOnSuccessListener {
                viewModelScope.launch {
                    _addToWishList.emit(Resource.Success(wishListProducts))
                    _isProductInWishList.value = true
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _addToWishList.emit(Resource.Error(it.message.toString()))
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


    private fun addProductWishList(wishListProducts: WishListProducts, onResult: (WishListProducts?, Exception?) -> Unit) {
        firestore.collection("user").document(auth.uid!!).collection("wishlist")
            .document()
            .set(wishListProducts)
            .addOnSuccessListener {
                onResult(wishListProducts, null)
            }.addOnFailureListener {
                onResult(null, it)
            }
    }

    fun addToWishList(wishListProducts: WishListProducts) {
        viewModelScope.launch { _addToWishList.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("wishlist")
            .whereEqualTo("product.productId", wishListProducts.product.productId)
            .get()
            .addOnSuccessListener {
                it.documents.let {
                    if (it.isEmpty()) { // Add the item to the wishlist
                        addNewProductToWishList(wishListProducts)
                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch { _addToWishList.emit(Resource.Error(it.message.toString())) }
            }
    }

    // Check Wish List for Product Presence
    fun checkIfProductInWishList(productId: String) {
        viewModelScope.launch {
            firestore.collection("user").document(auth.uid!!).collection("wishlist")
                .whereEqualTo("product.productId", productId)
                .get()
                .addOnSuccessListener {
                    _isProductInWishList.value = !it.isEmpty
                }
                .addOnFailureListener {
                    _isProductInWishList.value = false
                }
        }
    }
}