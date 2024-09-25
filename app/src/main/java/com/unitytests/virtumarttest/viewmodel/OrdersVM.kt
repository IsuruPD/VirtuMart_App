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
//    viewModelScope.launch {
//        _allOrders.emit(Resource.Loading())
//    }
//    firestore.collection("orders").document(auth.uid!!).collection("orders")
//        .get()
//        .addOnSuccessListener {
//            val orders= it.toObjects(Order::class.java)
//            viewModelScope.launch{
//                _allOrders.emit(Resource.Success(orders))
//            }
//        }
//        .addOnFailureListener{
//            viewModelScope.launch{
//                _allOrders.emit(Resource.Error(it.message.toString()))
//            }
//        }

        viewModelScope.launch {
            _allOrders.emit(Resource.Loading())
        }

        // Fetch orders from the "user_orders" subcollection under the user's document
        firestore.collection("orders").document(auth.uid!!)
            .collection("user_orders")
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Convert each document snapshot to an Order object
                    val ordersList = mutableListOf<Order>()
                    for (document in querySnapshot.documents) {
                        val order = document.toObject(Order::class.java)
                        order?.let { ordersList.add(it) }
                    }

                    viewModelScope.launch {
                        _allOrders.emit(Resource.Success(ordersList))
                    }
                } else {
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