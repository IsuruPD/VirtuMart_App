package com.unitytests.virtumarttest.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.StorageReference
import com.unitytests.virtumarttest.data.ChatMessages
import com.unitytests.virtumarttest.data.ChatMetadata
import com.unitytests.virtumarttest.data.Product
import com.unitytests.virtumarttest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject


@HiltViewModel
class ChatVM @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storageRef: StorageReference): ViewModel() {

    private val _chatMessages = MutableStateFlow<Resource<List<ChatMessages>>>(Resource.Unspecified())
    val chatMessages = _chatMessages.asStateFlow()

    private val pagingInfoChats  = ChatsPagingInfo()
    init{
        fetchChatMessages()
    }

    fun getUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    suspend fun sendMessage(senderId: String, text: String, imgFile: java.io.File?) {

        if (text.isEmpty()) return

        val customerCareId = "DaMi3g8f9bSEUK6hWn5UWcbi8nv2";
        val chatId = generateChatId(senderId)

        var imgUrl: String? = null

        try {
                // Upload image to Firebase Storage if available
                imgFile?.let { file ->
                    imgUrl = uploadImage(file, senderId)
                }

                // Construct message data
                val messageData = mapOf(
                    "senderId" to senderId,
                    "text" to text,
                    "createdAt" to Date(),
                    "imgUrl" to imgUrl
                )
            val chatRef = firestore.collection("chats").document(chatId)

            firestore.runTransaction{transaction->
                // Update chat document in Firestore with new message
                chatRef.set(mapOf<String, Any>(), SetOptions.merge())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Document exists or has been created/updated successfully
                            // Now update the messages array
                            chatRef.update("messages", FieldValue.arrayUnion(messageData))
                                .addOnSuccessListener {
                                    Log.d("chats Success", "Data has been saved in chats successfully!")
                                }
                                .addOnFailureListener { error ->
                                    viewModelScope.launch {
                                        _chatMessages.emit(Resource.Error(error?.message.toString()))
                                    }
                                    Log.d("Chats Fail", "Failed to update messages in chats: ${error.message}")
                                }
                        } else {
                            Log.d("Chats Fail", "Failed to create/update chat document: ${task.exception?.message}")
                            viewModelScope.launch {
                                _chatMessages.emit(Resource.Error("Failed to create/update chat document"))
                            }
                        }
                    }

                // Update userchats document for each participant
                val participantIds = listOf(senderId, customerCareId) // Add receiverId if needed

                participantIds.forEach { id ->
                    val userChatsRef = firestore.collection("userchats").document(id)

                    userChatsRef.get().addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val userChatsData = snapshot.toObject(ChatMessages::class.java)

                            // Determine senderId and receiverId based on current participant
                            val currentSenderId = if (id == senderId) senderId else customerCareId
                            val currentReceiverId = if (id == senderId) customerCareId else senderId

                            // Create or update the 'chats' array
                            val updatedChats = userChatsData?.chats?.toMutableList() ?: mutableListOf()
                            val existingChat = updatedChats.find { it.chatId == chatId }

                            if (existingChat == null) {
                                // Create a new ChatMetadata object if it doesn't exist
                                val newChat = ChatMetadata(
                                    chatId = chatId,
                                    isSeen = currentSenderId == senderId,
                                    lastMessage = text,
                                    receiverId = currentReceiverId,
                                    updatedAt = Date().time
                                )
                                updatedChats.add(newChat)
                            } else {
                                // Update existing ChatMetadata if it already exists
                                existingChat.lastMessage = text
                                existingChat.isSeen = currentSenderId == senderId
                                existingChat.updatedAt = Date().time
                            }

                            // Update 'chats' array in userchats document
                            userChatsRef.set(mapOf("chats" to updatedChats), SetOptions.merge())
                                .addOnSuccessListener {
                                    Log.d("userChats Success", "Data has been saved in userChats successfully!")
                                }
                                .addOnFailureListener { error ->
                                    viewModelScope.launch {
                                        _chatMessages.emit(Resource.Error(error?.message.toString()))
                                    }
                                    Log.d("userChats Fail", "Data has not been saved in userChats successfully!")
                                }
                        }
                    }
                }

            }.addOnSuccessListener {
                Log.d(
                    "All Success",
                    "Data has been saved in userChats and chats successfully!"
                )
            }
                .addOnFailureListener { error ->
                    viewModelScope.launch {
                        _chatMessages.emit(Resource.Error(error?.message.toString()))
                    }
                    Log.d("All Fail","Data has not been saved in userChats and chats successfully! : ${error.message.toString()}")

                }
            } catch (e: Exception){

                Log.d("Message Send Fail", "Error: " + e.message.toString())
            }
    }

    private suspend fun uploadImage(file: java.io.File, senderId: String): String? {
        return try {
            val chatImagesRef = storageRef.child("chat_images/$senderId/${file.name}")

            val uploadTask = chatImagesRef.putFile(Uri.fromFile(file))

            uploadTask.await() // Wait for the upload to complete

            // Get the download URL of the uploaded file
            val downloadUrl = chatImagesRef.downloadUrl.await()

            downloadUrl.toString() // Return the download URL as a String
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Error uploading image: ${e.message}", e)
            null
        }
    }

    private fun generateChatId(senderId : String): String{

        return senderId+"chat";
    }


    fun fetchChatMessages(){
        if(!pagingInfoChats.isPagingEnd){
            viewModelScope.launch{
                _chatMessages.emit(Resource.Loading())
            }
            //Make the selection here using whereEqualTo("field","value") after collection
            firestore.collection("chats").limit(pagingInfoChats.page * 10).get().addOnSuccessListener { result->
                val chatMessageList=result.toObjects((ChatMessages::class.java))
                //
                pagingInfoChats.isPagingEnd = chatMessageList == pagingInfoChats.prevChatMessages
                pagingInfoChats.prevChatMessages = chatMessageList
                //
                viewModelScope.launch {
                    _chatMessages.emit(Resource.Success(chatMessageList))
                }
                pagingInfoChats.page++
            }.addOnFailureListener{
                viewModelScope.launch {
                    _chatMessages.emit(Resource.Error(it.message.toString()))
                }
            }
        }
    }
}

internal data class ChatsPagingInfo(
    var page: Long = 1,
    //To avoid making more requests from firebase if the items are over to save bandwidth
    var prevChatMessages: List<ChatMessages> = emptyList(),
    var isPagingEnd: Boolean = false

)