package com.unitytests.virtumarttest.viewmodel

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

    fun addOrUpdateShippingDetails(shippingDetails: ShippingDetails, isNew: Boolean) {
        if (!validateShippingDetails(shippingDetails)) {
            viewModelScope.launch {
                _error.emit("All fields are required!")
            }
            return
        }

        viewModelScope.launch {
            try {
                if (isNew) {
                    // Adding a new shipping detail
                    val isAliasUnique = checkUniqueness(shippingDetails.addressAlias)
                    if (!isAliasUnique) {
                        _error.emit("An address with name '${shippingDetails.addressAlias}' already exists!")
                        return@launch
                    }

                    _addOrUpdateShippingDetail.value = Resource.Loading()
                    val userShippingCollection = firestore.collection("user").document(auth.uid!!)
                        .collection("shippingDetails")

                    userShippingCollection.add(shippingDetails)
                        .addOnSuccessListener {
                            _addOrUpdateShippingDetail.value = Resource.Success(shippingDetails)
                        }
                        .addOnFailureListener { e ->
                            _addOrUpdateShippingDetail.value =
                                Resource.Error(e.message ?: "Failed to add shipping details")
                        }
                } else {
                    // Updating an existing shipping detail
                    val userShippingCollection = firestore.collection("user").document(auth.uid!!)
                        .collection("shippingDetails")

                    userShippingCollection.document(shippingDetails.id) ////////////////////////////
                        .set(shippingDetails)
                        .addOnSuccessListener {
                            _addOrUpdateShippingDetail.value = Resource.Success(shippingDetails)
                        }
                        .addOnFailureListener { e ->
                            _addOrUpdateShippingDetail.value =
                                Resource.Error(e.message ?: "Failed to update shipping details")
                        }
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
