package com.unitytests.virtumarttest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.data.ChatMessages
import com.unitytests.virtumarttest.data.MessageType

class ChatHeadsAdapter : ListAdapter<ChatMessages, RecyclerView.ViewHolder>(ChatMessageDiffCallback()) {

    // ViewType for sent message and received message
    private val SENT_MESSAGE_VIEW_TYPE = 1
    private val RECEIVED_MESSAGE_VIEW_TYPE = 2

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessageView: TextView = itemView.findViewById(R.id.sentMessageChatView)
        val sentTimeStamp: TextView = itemView.findViewById(R.id.sentTimeStamp)
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedMessageView: TextView = itemView.findViewById(R.id.receivedMessageChatView)
        val receivedTimeStamp: TextView = itemView.findViewById(R.id.receivedTimeStampChatView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            SENT_MESSAGE_VIEW_TYPE -> {
                val sentView = inflater.inflate(R.layout.sent_message_recyclerview_item, parent, false)
                SentMessageViewHolder(sentView)
            }
            RECEIVED_MESSAGE_VIEW_TYPE -> {
                val receivedView = inflater.inflate(R.layout.received_message_recyclerview_item, parent, false)
                ReceivedMessageViewHolder(receivedView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)

        when (holder.itemViewType) {
            SENT_MESSAGE_VIEW_TYPE -> {
                val sentHolder = holder as SentMessageViewHolder
                sentHolder.sentMessageView.text = message.text
                sentHolder.sentTimeStamp.text = message.timestamp
            }
            RECEIVED_MESSAGE_VIEW_TYPE -> {
                val receivedHolder = holder as ReceivedMessageViewHolder
                receivedHolder.receivedMessageView.text = message.text
                receivedHolder.receivedTimeStamp.text = message.timestamp
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.type == MessageType.SENT) {
            SENT_MESSAGE_VIEW_TYPE
        } else {
            RECEIVED_MESSAGE_VIEW_TYPE
        }
    }

    class ChatMessageDiffCallback : DiffUtil.ItemCallback<ChatMessages>() {
        override fun areItemsTheSame(oldItem: ChatMessages, newItem: ChatMessages): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatMessages, newItem: ChatMessages): Boolean {
            return oldItem.text == newItem.text && oldItem.type == newItem.type
        }
    }

    fun addMessage(message: ChatMessages) {
        val newList = currentList.toMutableList()
        newList.add(message)
        submitList(newList)
    }
}
