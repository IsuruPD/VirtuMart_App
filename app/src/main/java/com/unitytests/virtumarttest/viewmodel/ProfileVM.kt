package com.unitytests.virtumarttest.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.unitytests.virtumarttest.VirtuMartApplication
import com.unitytests.virtumarttest.data.User
import com.unitytests.virtumarttest.util.RegisterValidation
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.util.validateEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProfileVM @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: StorageReference,
    app: Application
) :AndroidViewModel(app) {

    fun getUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()

    private val _updateDetails = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val updateDetails = _updateDetails.asStateFlow()

    private val _changePassword= MutableSharedFlow<Resource<String>>()
    val changePassword= _changePassword.asSharedFlow()

    init{
        getUser()
    }

    fun getUser(){
        viewModelScope.launch{
            _user.emit(Resource.Loading())
        }
//
//        firestore.collection("user").document(auth.uid!!).get()
//            .addOnSuccessListener {
//                val user = it.toObject(User::class.java)
//                user?.let{
//                    viewModelScope.launch{
//                        _user.emit(Resource.Success(it))
//                    }
//                }
//            }.addOnFailureListener{
//                viewModelScope.launch{
//                    _user.emit(Resource.Error(it.message.toString()))
//                }
//            }
        firestore.collection("user").document(auth.uid!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    user?.let {
                        viewModelScope.launch {
                            _user.emit(Resource.Success(it))
                        }
                    }
                } else {
                    viewModelScope.launch {
                        _user.emit(Resource.Error("User document does not exist"))
                    }
                }
            }
            .addOnFailureListener { e ->
                viewModelScope.launch {
                    _user.emit(Resource.Error("Failed to get user document: ${e.message}"))
                }
            }

    }

    fun updateUserDetails(updatedUser: User, imageUri: Uri?) {
        val inputValidity = validateEmail(updatedUser.email) is RegisterValidation.Success
                && updatedUser.firstname.trim().isNotEmpty() && updatedUser.lastname.trim().isNotEmpty()

        if (!inputValidity) {
            viewModelScope.launch {
                _user.emit(Resource.Error("Fields cannot be empty!"))
            }
            return
        }

        viewModelScope.launch {
            _updateDetails.emit(Resource.Loading())

            val userRef = firestore.collection("user").document(auth.uid!!)
            val currentUserSnapshot = userRef.get().await()
            val currentUser = currentUserSnapshot.toObject(User::class.java)

            currentUser?.let { currentUserData ->
                val updates = mutableMapOf<String, Any>()

                // Compare current and new values, only add to updates map if changed
                if (updatedUser.firstname != currentUserData.firstname) {
                    updates["firstname"] = updatedUser.firstname
                }
                if (updatedUser.lastname != currentUserData.lastname) {
                    updates["lastname"] = updatedUser.lastname
                }

                // Check if the profile image needs updating
                if (imageUri != null) {
                    // Update profile image if necessary
                    uploadProfileImage(updatedUser, imageUri) { newImagePath ->
                        updates["imagePath"] = newImagePath
                        applyUpdates(userRef, updates, updatedUser)
                    }
                } else {
                    applyUpdates(userRef, updates, updatedUser)
                }
            } ?: run {
                _updateDetails.emit(Resource.Error("User not found"))
            }
        }
    }

    private fun applyUpdates(
        userRef: DocumentReference,
        updates: Map<String, Any>,
        updatedUser: User
    ) {
        if (updates.isNotEmpty()) {
            userRef.update(updates)
                .addOnSuccessListener {
                    viewModelScope.launch {
                        _updateDetails.emit(Resource.Success(updatedUser))
                    }
                }.addOnFailureListener { error ->
                    viewModelScope.launch {
                        _updateDetails.emit(Resource.Error(error.message.toString()))
                    }
                }
        } else {
            // No changes to update
            viewModelScope.launch {
                _updateDetails.emit(Resource.Error("No changes made to the profile"))
            }
        }
    }

    private fun uploadProfileImage(user: User, imageUri: Uri, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val imageBitmap = MediaStore.Images.Media.getBitmap(
                    getApplication<VirtuMartApplication>().contentResolver, imageUri
                )

                val byteArrayOutputStream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 96, byteArrayOutputStream)
                val imageBytearray = byteArrayOutputStream.toByteArray()

                val profileImageDirectory =
                    storage.child("userProfileImages/${auth.uid}/${UUID.randomUUID()}")
                val result = profileImageDirectory.putBytes(imageBytearray).await()
                val imageUri = result.storage.downloadUrl.await().toString()

                onComplete(imageUri)

            } catch (error: Exception) {
                _updateDetails.emit(Resource.Error(error.message.toString()))
            }
        }
    }

    fun updatePhoneNumber(phoneNumber: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("user").document(userId)

        viewModelScope.launch {
            _updateDetails.value = Resource.Loading()

            auth.currentUser?.let {
                userRef.update("phoneNumber", phoneNumber)
                    .addOnSuccessListener {
                        // Fetch the updated user from Firestore and emit it
                        userRef.get().addOnSuccessListener { documentSnapshot ->
                            val updatedUser = documentSnapshot.toObject(User::class.java)
                            updatedUser?.let {
                                _updateDetails.value = Resource.Success(it)
                            } ?: run {
                                _updateDetails.value = Resource.Error("Failed to fetch updated user")
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        _updateDetails.value = Resource.Error(e.message ?: "An error occurred")
                    }
            } ?: run {
                _updateDetails.value = Resource.Error("User is not authenticated")
            }
        }
    }

    fun updateNIC(nic: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("user").document(userId)

        viewModelScope.launch {
            _updateDetails.value = Resource.Loading()

            auth.currentUser?.let {
                userRef.update("nic", nic)
                    .addOnSuccessListener {
                        // Fetch the updated user from Firestore and emit it
                        userRef.get().addOnSuccessListener { documentSnapshot ->
                            val updatedUser = documentSnapshot.toObject(User::class.java)
                            updatedUser?.let {
                                _updateDetails.value = Resource.Success(it)
                            } ?: run {
                                _updateDetails.value = Resource.Error("Failed to fetch updated user")
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        _updateDetails.value = Resource.Error(e.message ?: "An error occurred")
                    }
            } ?: run {
                _updateDetails.value = Resource.Error("User is not authenticated")
            }
        }
    }

    fun updateDOB(dob: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("user").document(userId)

        viewModelScope.launch {
            _updateDetails.value = Resource.Loading()

            auth.currentUser?.let {
                userRef.update("dob", dob)
                    .addOnSuccessListener {
                        // Fetch the updated user from Firestore and emit it
                        userRef.get().addOnSuccessListener { documentSnapshot ->
                            val updatedUser = documentSnapshot.toObject(User::class.java)
                            updatedUser?.let {
                                _updateDetails.value = Resource.Success(it)
                            } ?: run {
                                _updateDetails.value = Resource.Error("Failed to fetch updated user")
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        _updateDetails.value = Resource.Error(e.message ?: "An error occurred")
                    }
            } ?: run {
                _updateDetails.value = Resource.Error("User is not authenticated")
            }
        }
    }

    fun updateGender(gender: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("user").document(userId)

        viewModelScope.launch {
            _updateDetails.value = Resource.Loading()

            auth.currentUser?.let {
                userRef.update("gender", gender)
                    .addOnSuccessListener {
                        // Fetch the updated user from Firestore and emit it
                        userRef.get().addOnSuccessListener { documentSnapshot ->
                            val updatedUser = documentSnapshot.toObject(User::class.java)
                            updatedUser?.let {
                                _updateDetails.value = Resource.Success(it)
                            } ?: run {
                                _updateDetails.value = Resource.Error("Failed to fetch updated user")
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        _updateDetails.value = Resource.Error(e.message ?: "An error occurred")
                    }
            } ?: run {
                _updateDetails.value = Resource.Error("User is not authenticated")
            }
        }
    }

    fun resetPassword(email: String){
        viewModelScope.launch {
            _changePassword.emit(Resource.Loading())
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                viewModelScope.launch {
                    _changePassword.emit(Resource.Success(email))
                }
            }
            .addOnFailureListener{
                viewModelScope.launch {
                    _changePassword.emit(Resource.Error(it.message.toString()))
                }
            }
    }

//    fun updateUserDetails(user: User, imageUri: Uri?){
//
//        val inputValidity = validateEmail(user.email) is RegisterValidation.Success
//                && user.firstname.trim().isNotEmpty() && user.lastname.trim().isNotEmpty()
//
//        if(!inputValidity){
//            viewModelScope.launch{
//                _user.emit(Resource.Error("Fields cannot be empty!"))
//            }
//        }
//
//        viewModelScope.launch{
//            _updateDetails.emit(Resource.Loading())
//        }
//
//        if(imageUri==null){
//            editDetails(user, true)
//        }else{
//            editDetailswithImage(user, imageUri)
//        }
//    }
//
//    private fun editDetails(user: User, retrieveOldImage: Boolean) {
//        firestore.runTransaction { transaction ->
//            val documentRef = firestore.collection("user").document(auth.uid!!)
//            if(retrieveOldImage){
//                val currentUser = transaction.get(documentRef).toObject(User::class.java)
//                val newUser = user.copy(imagePath = currentUser?.imagePath?: "")
//                transaction.set(documentRef, newUser)
//            }else{
//                transaction.set(documentRef, user)
//            }
//        }.addOnSuccessListener {
//            viewModelScope.launch{
//                _updateDetails.emit(Resource.Success(user))
//            }
//        }.addOnFailureListener{
//            viewModelScope.launch {
//                _updateDetails.emit(Resource.Error(it.message.toString()))
//            }
//        }
//    }
//
//    private fun editDetailswithImage(user: User, imageUri: Uri) {
//        viewModelScope.launch{
//            try{
//                val imageBitmap= MediaStore.Images.Media.getBitmap(
//                    getApplication<VirtuMartApplication>().contentResolver, imageUri)
//
//                val byteArrayOutputStream = ByteArrayOutputStream()
//                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 96, byteArrayOutputStream)
//                val imageBytearray = byteArrayOutputStream.toByteArray()
//
//                val profileImageDirectory =
//                    storage.child("userProfileImages/${auth.uid}/${UUID.randomUUID()}")
//                val result= profileImageDirectory.putBytes(imageBytearray).await()
//                val imageUri = result.storage.downloadUrl.await().toString()
//
//                editDetails(user.copy(imagePath = imageUri), false)
//
//            }catch(error: Exception){
//                viewModelScope.launch {
//                    _updateDetails.emit(Resource.Error(error.message.toString()))
//                }
//            }
//        }
//    }
}