package com.unitytests.virtumarttest.viewmodel

import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.ShippingDetails
import com.unitytests.virtumarttest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ShippingDetailsVM @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _addOrUpdateShippingDetail = MutableStateFlow<Resource<ShippingDetails>>(Resource.Unspecified())
    val addOrUpdateShippingDetail = _addOrUpdateShippingDetail.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    fun addShippingDetails(shippingDetails: ShippingDetails) {
        if (!validateShippingDetails(shippingDetails)) {
            viewModelScope.launch {
                _error.emit("All fields are required!")
            }
            return
        }

        viewModelScope.launch {
            try {
                // Generate a UUID for the shipping detail ID
                val shippingId = UUID.randomUUID().toString()

                // Set the generated ID to the shippingDetails object
                val shippingDetailsWithId = shippingDetails.copy(id = shippingId)

                // Check uniqueness by addressAlias
                val isAliasUnique = checkUniqueness(shippingDetailsWithId.addressAlias)
                if (!isAliasUnique) {
                    _error.emit("An address with name '${shippingDetailsWithId.addressAlias}' already exists!")
                    return@launch
                }

                _addOrUpdateShippingDetail.value = Resource.Loading()
                val userShippingCollection = firestore.collection("user").document(auth.uid!!)
                    .collection("shippingDetails")

                userShippingCollection
                    .document(shippingId) // Use the generated ID as the document ID
                    .set(shippingDetailsWithId)
                    .addOnSuccessListener {
                        _addOrUpdateShippingDetail.value = Resource.Success(shippingDetailsWithId)
                    }
                    .addOnFailureListener { e ->
                        _addOrUpdateShippingDetail.value =
                            Resource.Error(e.message ?: "Failed to add shipping details")
                    }
            } catch (e: Exception) {
                _addOrUpdateShippingDetail.value =
                    Resource.Error("Error occurred: " + e.message.toString())
            }
        }
    }


    fun updateShippingDetails(shippingDetails: ShippingDetails) {
        if (!validateShippingDetails(shippingDetails)) {
            viewModelScope.launch {
                _error.emit("All fields are required!")
            }
            return
        }

        viewModelScope.launch {
            try {
                _addOrUpdateShippingDetail.value = Resource.Loading()

                val userShippingCollection = firestore.collection("user").document(auth.uid!!)
                    .collection("shippingDetails")

                val shippingId = shippingDetails.id ?: ""
                if (shippingId.isNotEmpty()) {
                    userShippingCollection.document(shippingId)
                        .set(shippingDetails)
                        .addOnSuccessListener {
                            _addOrUpdateShippingDetail.value = Resource.Success(shippingDetails)
                        }
                        .addOnFailureListener { e ->
                            _addOrUpdateShippingDetail.value =
                                Resource.Error(e.message ?: "Failed to update shipping details")
                        }
                } else {
                    _addOrUpdateShippingDetail.value =
                        Resource.Error("Shipping ID is missing for update operation")
                }
            } catch (e: Exception) {
                _addOrUpdateShippingDetail.value =
                    Resource.Error("Error occurred: " + e.message.toString())
            }
        }
    }

    private suspend fun checkUniqueness(alias: String): Boolean {
        val querySnapshot = firestore.collection("user").document(auth.uid!!)
            .collection("shippingDetails").whereEqualTo("addressAlias", alias).get().await()
        return querySnapshot.isEmpty
    }

    private fun validateShippingDetails(shippingDetails: ShippingDetails): Boolean {
        return shippingDetails.addressAlias.isNotEmpty() &&
                shippingDetails.receiverName.isNotEmpty() &&
                shippingDetails.address.isNotEmpty() &&
                shippingDetails.city.isNotEmpty() &&
                shippingDetails.district.isNotEmpty() &&
                shippingDetails.contact.isNotEmpty()
    }
}
