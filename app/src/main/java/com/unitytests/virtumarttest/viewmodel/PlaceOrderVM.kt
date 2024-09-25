package com.unitytests.virtumarttest.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.unitytests.virtumarttest.data.orders.Order
import com.unitytests.virtumarttest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaceOrderVM @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _order = MutableStateFlow<Resource<Order>>(Resource.Unspecified())
    val order = _order.asStateFlow()

    fun placeOrder(order: Order) {
        viewModelScope.launch {
            _order.emit(Resource.Loading())
        }

        // Gather product details and stock levels asynchronously before the transaction
        val productStocks = mutableMapOf<String, Pair<DocumentReference, Long>>()  // Mapping productId to DocumentReference and stock

        val deferredResults = order.orderItems.map { cartProduct ->
            firestore.collection("products")
                .whereEqualTo("productId", cartProduct.product.productId)
                .get()
                .continueWith { task ->
                    val productSnapshot = task.result
                    if (productSnapshot != null && !productSnapshot.isEmpty) {
                        val productDocument = productSnapshot.documents[0]
                        val availableStock = productDocument.getLong("quantity") ?: 0L
                        val fetchedProductId = productDocument.getString("productId") ?: ""

                        if (fetchedProductId == cartProduct.product.productId) {
                            if (availableStock >= cartProduct.quantity) {
                                // Store productId, DocumentReference, and available stock
                                productStocks[fetchedProductId] = productDocument.reference to availableStock
                            } else {
                                throw FirebaseFirestoreException(
                                    "Not enough stock for product: ${cartProduct.product.productName}",
                                    FirebaseFirestoreException.Code.ABORTED
                                )
                            }
                        } else {
                            throw FirebaseFirestoreException(
                                "Product mismatch for productId: ${cartProduct.product.productId}",
                                FirebaseFirestoreException.Code.NOT_FOUND
                            )
                        }
                    } else {
                        throw FirebaseFirestoreException(
                            "Product not found for productId: ${cartProduct.product.productId}",
                            FirebaseFirestoreException.Code.NOT_FOUND
                        )
                    }
                }
        }

        // Wait for all product queries to complete
        Tasks.whenAllSuccess<Any>(deferredResults).addOnSuccessListener {
            // Run the transaction after all reads
            firestore.runTransaction { transaction ->

                Log.d("PlaceOrderVM", "Starting transaction after all product reads.")

                // Place the order and update stock
                val userOrdersRef = firestore.collection("user").document(auth.uid!!)
                    .collection("orders").document()
                transaction.set(userOrdersRef, order)

                val globalOrdersRef = firestore.collection("orders").document(auth.uid!!)
                    .collection("user_orders").document()
                transaction.set(globalOrdersRef, order)

                // Update stock quantities
                productStocks.forEach { (productId, stockInfo) ->
                    val (productRef, availableStock) = stockInfo
                    Log.d("PlaceOrderVM", "Updating stock for productId: $productId")

                    // Compare with productId from the order
                    val cartProduct = order.orderItems.firstOrNull { it.product.productId == productId }

                    if (cartProduct != null) {
                        val newStock = availableStock - cartProduct.quantity

                        Log.d("PlaceOrderVM", "Available Stock: $availableStock, Cart Quantity: ${cartProduct.quantity}, New Stock: $newStock")

                        // Update the product stock
                        transaction.update(productRef, "quantity", newStock)
                    } else {
                        Log.e("PlaceOrderVM", "E1: No cartProduct found for productId: $productId")
                        throw FirebaseFirestoreException(
                            "Firebase Exception: No matching product in the cart for productId: $productId",
                            FirebaseFirestoreException.Code.ABORTED
                        )
                    }
                }

                // If everything succeeds, the transaction will commit
                null
            }.addOnSuccessListener {
                // Clear cart after transaction
                firestore.collection("user").document(auth.uid!!)
                    .collection("cart")
                    .get()
                    .addOnSuccessListener { cartItems ->
                        cartItems.documents.forEach { cartItem ->
                            cartItem.reference.delete()
                        }
                    }

                viewModelScope.launch {
                    _order.emit(Resource.Success(order))
                }
            }.addOnFailureListener { e ->
                viewModelScope.launch {
                    _order.emit(Resource.Error("E1: ${e.message}"))
                }
            }
        }.addOnFailureListener { e ->
            viewModelScope.launch {
                _order.emit(Resource.Error("EFinal: Error fetching products: ${e.message}"))
            }
        }
    }
}