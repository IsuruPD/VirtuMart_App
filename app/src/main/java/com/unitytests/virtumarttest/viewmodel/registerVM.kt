package com.unitytests.virtumarttest.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.unitytests.virtumarttest.data.User
import com.unitytests.virtumarttest.util.RegisterFieldsState
import com.unitytests.virtumarttest.util.RegisterValidation
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.util.validateEmail
import com.unitytests.virtumarttest.util.validatePassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class registerVM @Inject constructor(
    private val firebaseAuth: FirebaseAuth
    ): ViewModel(){

        private val _register= MutableStateFlow<Resource<FirebaseUser>>(Resource.Unspecified())
        val register: Flow<Resource<FirebaseUser>> =_register

        private val _validation= Channel<RegisterFieldsState>()
        val validation=_validation.receiveAsFlow()

        fun createAccountsWithEmailPass(user: User, password: String){
            if(checkValidation(user, password)) {
                runBlocking {
                    _register.emit(Resource.Loading())
                }
                firebaseAuth.createUserWithEmailAndPassword(user.email, password)
                    .addOnSuccessListener {
                        it.user?.let {
                            _register.value = Resource.Success(it)
                        }
                    }
                    .addOnFailureListener {
                        _register.value = Resource.Error(it.message.toString())
                    }
            }else{
                val registerFieldsState= RegisterFieldsState(validateEmail(user.email), validatePassword(password))
                runBlocking {
                    _validation.send(registerFieldsState)
                }
            }
        }

    private fun checkValidation(user: User, password: String): Boolean {
        val emailValidation = validateEmail(user.email)
        val passwordValidation = validatePassword(password)
        val validationStatus = emailValidation is RegisterValidation.Success &&
                passwordValidation is RegisterValidation.Success

        return validationStatus
    }
}