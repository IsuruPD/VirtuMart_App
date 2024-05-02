package com.unitytests.virtumarttest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.orders.Order
import com.unitytests.virtumarttest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersVM @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
): ViewModel() {

    private val _allOrders = MutableStateFlow<Resource<List<Order>>>(Resource.Unspecified())
    val allOrders = _allOrders.asStateFlow()

    init{
        getOrders()
    }

    fun getOrders(){

        viewModelScope.launch {
            _allOrders.emit(Resource.Loading())
        }

        //Fetch from the orders sub collection in users
        /*
        firestore.collection("user").document(auth.uid!!).collection("orders")
            .get()
            .addOnSuccessListener {
                val orders= it.toObjects(Order::class.java)
                viewModelScope.launch{
                    _allOrders.emit(Resource.Success(orders))
                }
            }
            .addOnFailureListener{
                viewModelScope.launch{
                    _allOrders.emit(Resource.Error(it.message.toString()))
                }
            }*/

        //Fetch from the orders collection

        firestore.collection("orders").document(auth.uid!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Retrieve the order data from the document
                    val orderData = documentSnapshot.toObject(Order::class.java)
                    if (orderData != null) {
                        viewModelScope.launch {
                            _allOrders.emit(Resource.Success(listOf(orderData))) // Wrap the order in a list
                        }
                    } else {
                        // No orders found for the user
                        viewModelScope.launch {
                            _allOrders.emit(Resource.Success(emptyList()))
                        }
                    }
                } else {
                    // No document found for the user
                    viewModelScope.launch {
                        _allOrders.emit(Resource.Success(emptyList()))
                    }
                }
            }
            .addOnFailureListener { e ->
                viewModelScope.launch {
                    _allOrders.emit(Resource.Error(e.message ?: "Failed to retrieve orders"))
                }
            }
    }
}