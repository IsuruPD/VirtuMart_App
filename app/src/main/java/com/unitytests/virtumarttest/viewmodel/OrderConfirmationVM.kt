package com.unitytests.virtumarttest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unitytests.virtumarttest.data.ShippingDetails
import com.unitytests.virtumarttest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderConfirmationVM @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _shippingDetails = MutableStateFlow<Resource<List<ShippingDetails>>>(Resource.Unspecified())
    val shippingDetails = _shippingDetails.asStateFlow()

    init{
        getUserShippingAddresses()
    }

    fun getUserShippingAddresses(){
        viewModelScope.launch {
            _shippingDetails.emit(Resource.Loading())
        }
        firestore.collection("user").document(auth.uid!!).collection("shippingDetails")
            .addSnapshotListener{value, error ->
                if(error !=null){
                    viewModelScope.launch {
                        _shippingDetails.emit(Resource.Error(error.message.toString()))
                    }
                    return@addSnapshotListener
                }
                val shippingDetails = value?.toObjects(ShippingDetails:: class.java)
                viewModelScope.launch { _shippingDetails.emit(Resource.Success(shippingDetails!!)) }

            }
    }

}