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
class OrderStatusManagementVM @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
): ViewModel() {


    private val _modifyOrderStatus = MutableStateFlow<Resource<List<Order>>>(Resource.Unspecified())
    val modifyOrderStatus = _modifyOrderStatus.asStateFlow()


    // Method to update order status
    fun updateOrderStatus(orderId: Long, newStatus: String) {

        viewModelScope.launch {
            _modifyOrderStatus.emit(Resource.Loading())
        }

        firestore.collection("orders").document(auth.uid!!)
            .collection("user_orders")
            .whereEqualTo("orderId", orderId) // Use where clause to match orderId
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.first()
                    val userOrderRef = document.reference

                    userOrderRef.update("orderStatus", newStatus)
                        .addOnSuccessListener {
                            viewModelScope.launch {
                                _modifyOrderStatus.emit(Resource.Success(emptyList()))
                            }
                        }
                        .addOnFailureListener { e ->
                            viewModelScope.launch {
                                _modifyOrderStatus.emit(Resource.Error(e.message ?: "Failed to update status"))
                            }
                        }
                } else {
                    viewModelScope.launch {
                        _modifyOrderStatus.emit(Resource.Success(emptyList()))
                    }
                }
            }
            .addOnFailureListener { e ->
                viewModelScope.launch {
                    _modifyOrderStatus.emit(Resource.Error(e.message ?: "Failed to retrieve orders"))
                }
            }
    }
}