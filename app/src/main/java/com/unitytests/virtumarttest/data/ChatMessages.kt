package com.unitytests.virtumarttest.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class ChatMessages(
    val userId: String,
    val chats: List<ChatMetadata>
){
    constructor() : this("", emptyList())
}

@Serializable
data class ChatMetadata(
    val chatId: String,
    @SerialName("isSeen") var isSeen: Boolean,
    var lastMessage: String = "",
    val receiverId: String,
    var updatedAt: Long
){
    constructor() : this("", false,"", "XbkxdDReZVQOesb0a5ikpDu0lKY2", 0)
}
