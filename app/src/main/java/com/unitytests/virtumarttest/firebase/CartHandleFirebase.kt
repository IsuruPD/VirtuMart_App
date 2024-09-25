package com.unitytests.virtumarttest.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.CartProducts
import java.lang.Exception

class CartHandleFirebase (
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
    ) {

    private val cartCollection = firestore.collection("user").document(auth.uid!!).collection("cart")

    fun addProductToCart(cartProducts: CartProducts, onResult: (CartProducts?, Exception?)-> Unit){
        cartCollection.add(cartProducts)
            .addOnSuccessListener {
                onResult(cartProducts, null)
            }.addOnFailureListener {
                onResult(null, it)
            }
    }

    fun increaseQuantity(documentId: String, onResult: (String?, Exception?) -> Unit){
        firestore.runTransaction { transaction->
            val documentRef = cartCollection.document(documentId)
            val document = transaction.get(documentRef)
            val productObject = document.toObject(CartProducts:: class.java)
            productObject?.let{cartProduct->
                val newQuantity = cartProduct.quantity+1
                val newProductObject = cartProduct.copy(quantity= newQuantity)
                transaction.set(documentRef, newProductObject)
            }
        }.addOnSuccessListener {
            onResult(documentId, null)
        }.addOnFailureListener{
            onResult(null, it)
        }
    }

    fun decreaseQuantity(documentId: String, onResult: (String?, Exception?) -> Unit){
        firestore.runTransaction { transaction->
            val documentRef = cartCollection.document(documentId)
            val document = transaction.get(documentRef)
            val productObject = document.toObject(CartProducts:: class.java)
            productObject?.let{cartProduct->
                val newQuantity = cartProduct.quantity-1
                val newProductObject = cartProduct.copy(quantity= newQuantity)
                transaction.set(documentRef, newProductObject)
            }
        }.addOnSuccessListener {
            onResult(documentId, null)
        }.addOnFailureListener{
            onResult(null, it)
        }
    }

    enum class QuantityChanging{
        INCREASE, DECREASE
    }

    // Check available stock before increasing quantity
    fun checkStockAvailability(cartProduct: CartProducts, onResult: (Boolean, Exception?) -> Unit) {
        Log.d("QuantityCount","Check stock method called")

        val productRef = FirebaseFirestore.getInstance()
            .collection("products")
            .whereEqualTo("productId",cartProduct.product.productId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("QuantityCount","Fetched items")

                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val availableStock = document.getLong("quantity") ?: 0
                    Log.d("QuantityCount","Available stock: $availableStock")

                    if (cartProduct.quantity < availableStock) {
                        onResult(true, null) // Stock is available
                    } else {
                        onResult(false, null) // No more stock available
                    }
                } else {
                    Log.d("QuantityCount","Product with productId ${cartProduct.product.productId} not found")
                    onResult(false, null) // Product not found
                }
            }
            .addOnFailureListener { exception ->
                Log.d("QuantityCount","Error fetching product: ${exception.message}")
                onResult(false, exception) // Return error in result
            }
    }
}