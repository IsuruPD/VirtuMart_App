package com.unitytests.virtumarttest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.CartProducts
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
    private val _addToCart = MutableStateFlow<Resource<CartProducts>>(Resource.Unspecified())
    val addToCart = _addToCart.asStateFlow()

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
}