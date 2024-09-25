package com.unitytests.virtumarttest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.unitytests.virtumarttest.util.RegisterFieldsState
import com.unitytests.virtumarttest.util.RegisterValidation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.util.validateLoginEmail
import com.unitytests.virtumarttest.util.validateLoginPassword
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class LoginVM @Inject constructor(
    private val firebaseAuth: FirebaseAuth
): ViewModel() {

    private val _login= MutableSharedFlow<Resource<FirebaseUser>>()
    val login= _login.asSharedFlow()

    private val _resetPassword= MutableSharedFlow<Resource<String>>()
    val resetPassword= _resetPassword.asSharedFlow()

    private val _validateLogin = Channel<RegisterFieldsState>()
    val validateLogin = _validateLogin.receiveAsFlow()

    fun login(email: String, password: String){
        val emailValidation = validateLoginEmail(email)
        val passwordValidation = validateLoginPassword(password)

        if (emailValidation is RegisterValidation.Success && passwordValidation is RegisterValidation.Success) {
            viewModelScope.launch {
                _login.emit(Resource.Loading())
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        authResult.user?.let {
                            viewModelScope.launch {
                                _login.emit(Resource.Success(it))
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        viewModelScope.launch {
                            _login.emit(Resource.Error(e.message.toString()))
                        }
                    }
            }
        } else {
            val loginFieldsState = RegisterFieldsState(emailValidation, passwordValidation)
            viewModelScope.launch {
                _validateLogin.send(loginFieldsState)
            }
        }
    }

    fun resetPassword(email: String){
        viewModelScope.launch {
            _resetPassword.emit(Resource.Loading())
        }

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                viewModelScope.launch {
                    _resetPassword.emit(Resource.Success(email))
                }
            }
            .addOnFailureListener{
                viewModelScope.launch {
                    _resetPassword.emit(Resource.Error(it.message.toString()))
                }
            }
    }
}