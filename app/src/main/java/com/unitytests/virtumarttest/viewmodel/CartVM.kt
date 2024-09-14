package com.unitytests.virtumarttest.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.CartProducts
import com.unitytests.virtumarttest.firebase.CartHandleFirebase
import com.unitytests.virtumarttest.helper.getProductPrice
import com.unitytests.virtumarttest.helper.getProductSubTotal
import com.unitytests.virtumarttest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartVM @Inject constructor (
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: CartHandleFirebase
    ): ViewModel() {

    private val _cartProducts= MutableStateFlow<Resource<List<CartProducts>>>(Resource.Unspecified())
    val cartProductsSF = _cartProducts.asStateFlow()

    private val _cartErrorState = MutableStateFlow<Resource<List<CartProducts>>>(Resource.Unspecified())
    val cartErrorState = _cartErrorState.asStateFlow()

    // Remove products from cart
    private val _deleteCartItem = MutableSharedFlow<CartProducts>()
    val deleteCartItem = _deleteCartItem.asSharedFlow()

    private var cartProductDocuments = emptyList<DocumentSnapshot>()

    val productsCost = cartProductsSF.map{
        when(it){
            is Resource.Success ->{
                calculateCost(it.data!!)
            }
            else -> null
        }
    }

    val productsDiscount = cartProductsSF.map{
        when(it){
            is Resource.Success ->{
                calculateDiscounts(it.data!!)
            }
            else -> null
        }
    }

    val productsSubTotal = cartProductsSF.map{
        when(it){
            is Resource.Success ->{
                calculateSubTotal(it.data!!)
            }
            else -> null
        }
    }

    fun deleteCartItem(cartProducts: CartProducts){
        val index = cartProductsSF.value.data?.indexOf((cartProducts))
        if(index !=null && index != -1) {
            val documentId = cartProductDocuments[index].id
            firestore.collection("user").document(auth.uid!!).collection("cart")
                .document(documentId).delete()
        }
    }
    private fun calculateSubTotal(data: List<CartProducts>): Float {
        return data.sumByDouble { cartProducts ->
            (cartProducts.product.offerPercentage.getProductSubTotal(cartProducts.product.price)*cartProducts.quantity).toDouble()
        }.toFloat()
    }

    private fun calculateDiscounts(data: List<CartProducts>): Float {
        return data.sumByDouble { cartProducts ->
            ((cartProducts.product.offerPercentage.getProductSubTotal(cartProducts.product.price)*cartProducts.quantity).toDouble()
                -(cartProducts.product.offerPercentage.getProductPrice(cartProducts.product.price)*cartProducts.quantity).toDouble())
        }.toFloat()
    }

    private fun calculateCost(data: List<CartProducts>): Float {
        return data.sumByDouble { cartProducts ->
            (cartProducts.product.offerPercentage.getProductPrice(cartProducts.product.price)*cartProducts.quantity).toDouble()
        }.toFloat()
    }


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
                Log.d("CartVM", "Cart Products: $cartProducts")
                viewModelScope.launch{
                    _cartProducts.emit(Resource.Success(cartProducts))
                }
            }
        }
    }

    fun changingQuantity(cartProducts: CartProducts, quantityChanging: CartHandleFirebase.QuantityChanging) {
        val index = cartProductsSF.value.data?.indexOf((cartProducts))
        if (index != null && index != -1) {
            val documentId = cartProductDocuments[index].id

            when (quantityChanging) {
                CartHandleFirebase.QuantityChanging.INCREASE -> {
                    Log.d("QuantityCount","Changing quantity method is called")

                    // Check stock availability before increasing quantity
                    firebaseCommon.checkStockAvailability(cartProducts) { isAvailable, exception ->
                        if (exception != null) {
                            viewModelScope.launch {
                                _cartErrorState.emit(Resource.Error(exception.message.toString()))
                            }
                        } else if (isAvailable) {
                            viewModelScope.launch {
                                _cartProducts.emit(Resource.Loading())
                                _cartErrorState.emit(Resource.Loading())
                            }
                            increaseQuantity(documentId)
                        } else {
                            // Show error message if stock limit is reached
                            viewModelScope.launch {
//                                _cartProducts.emit(Resource.Error("Stock limit reached"))
                                _cartErrorState.emit(Resource.Error("Stock limit reached"))

                            }
                        }
                    }
                }

                CartHandleFirebase.QuantityChanging.DECREASE -> {
                    if (cartProducts.quantity <= 1) {
                        viewModelScope.launch {
                            _deleteCartItem.emit(cartProducts)
                        }
                        return
                    }
                    viewModelScope.launch { _cartProducts.emit(Resource.Loading()) }
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