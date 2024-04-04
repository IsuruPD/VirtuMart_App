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
import javax.inject.Inject

@HiltViewModel
class ShippingDetailsVM @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _addNewShippingDetail = MutableStateFlow<Resource<ShippingDetails>>(Resource.Unspecified())
    val addNewShippingDetail = _addNewShippingDetail.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    fun addShippingDetails(shippingDetails: ShippingDetails){

        val validateShippingDetails = validateShippingDetails(shippingDetails)
        if(validateShippingDetails) {
            viewModelScope.launch { _addNewShippingDetail.emit(Resource.Loading()) }
            firestore.collection("user").document(auth.uid!!).collection("shipping").document()
                .set(shippingDetails)
                .addOnSuccessListener {
                    viewModelScope.launch{_addNewShippingDetail.emit(Resource.Success(shippingDetails))}
                }.addOnFailureListener {
                    viewModelScope.launch{_addNewShippingDetail.emit(Resource.Error(it.message.toString()))}
                }
        }else{
            viewModelScope.launch {
                _error.emit("All fields are required!")
            }
        }
    }

    private fun validateShippingDetails(shippingDetails: ShippingDetails): Boolean {
        if(shippingDetails.addressAlias.isNotEmpty() && shippingDetails.receiverName.isNotEmpty() &&
                shippingDetails.address.isNotEmpty() && shippingDetails.city.isNotEmpty() &&
                shippingDetails.district.isNotEmpty() && shippingDetails.contact.isNotEmpty()){
            return true
        }
        return false
    }
}