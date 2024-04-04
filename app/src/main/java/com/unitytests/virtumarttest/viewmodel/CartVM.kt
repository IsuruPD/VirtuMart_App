package com.unitytests.virtumarttest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.CartProducts
import com.unitytests.virtumarttest.firebase.FirebaseCommonClass
import com.unitytests.virtumarttest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartVM @Inject constructor (
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommonClass
    ): ViewModel() {

    private val _cartProducts
        = MutableStateFlow<Resource<List<CartProducts>>>(Resource.Unspecified())
    val cartProductsSF = _cartProducts.asStateFlow()

    private var cartProductDocuments = emptyList<DocumentSnapshot>()

    init{
        getCartProducts()
    }

    private fun getCartProducts(){

        viewModelScope.launch {
            _cartProducts.emit(Resource.Loading())
        }
        firestore.collection("user").document(auth.uid!!).collection("cart")
        .addSnapshotListener { value, error ->
            if(error !=null || value == null){
                viewModelScope.launch {
                    _cartProducts.emit(Resource.Error(error?.message.toString()))
                }
            }else{
                cartProductDocuments = value.documents
                val cartProducts = value.toObjects(CartProducts::class.java)
                viewModelScope.launch{
                    _cartProducts.emit(Resource.Success(cartProducts))
                }
            }
        }
    }

    fun changingQuantity(cartProducts: CartProducts, quantityChanging: FirebaseCommonClass.QuantityChanging){
        val index = cartProductsSF.value.data?.indexOf((cartProducts))
        if(index !=null && index != -1){
            val documentId = cartProductDocuments[index].id
            when(quantityChanging){
                FirebaseCommonClass.QuantityChanging.INCREASE ->{
                    increaseQuantity(documentId)
                }
                FirebaseCommonClass.QuantityChanging.DECREASE ->{
                    decreaseQuantity(documentId)
                }
            }
        }
    }
    private fun decreaseQuantity(documentId: String){
        firebaseCommon.decreaseQuantity(documentId){result, exception ->
            if(exception != null){
                viewModelScope.launch {
                    _cartProducts.emit(Resource.Error(exception.message.toString()))
                }
            }
        }
    }

    private fun increaseQuantity(documentId: String){
        firebaseCommon.increaseQuantity(documentId){result, exception ->
            if(exception != null){
                viewModelScope.launch {
                    _cartProducts.emit(Resource.Error(exception.message.toString()))
                }
            }
        }
    }
}