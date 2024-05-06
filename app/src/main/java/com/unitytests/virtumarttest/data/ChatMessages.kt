package com.unitytests.virtumarttest.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessages(
    val text: String,
    val type: MessageType,
    val timestamp: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
)

enum class MessageType {
    SENT,
    RECEIVED
}
