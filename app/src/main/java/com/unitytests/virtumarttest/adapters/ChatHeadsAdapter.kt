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

class ChatHeadsAdapter : ListAdapter<ChatMessages, ChatHeadsAdapter.ChatViewHolder>(ChatMessageDiffCallback()) {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessageView: TextView = itemView.findViewById(R.id.sentMessageChatView)
        //val receivedMessageView: TextView = itemView.findViewById(R.id.receivedMessageChatView)
        val sentTimeStamp: TextView = itemView.findViewById(R.id.sentTimeStamp)
        //val receivedTimeStamp: TextView = itemView.findViewById(R.id.receivedTimeStampChatView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            MessageType.SENT.ordinal -> inflater.inflate(R.layout.sent_message_recyclerview_item, parent, false)
            //MessageType.RECEIVED.ordinal -> inflater.inflate(R.layout.received_message_recyclerview_item, parent, false)
            else -> throw IllegalArgumentException("Invalid view type")
        }
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = getItem(position)
        with(holder) {
            if (message.type == MessageType.SENT) {
                sentMessageView.text = message.text
                sentTimeStamp.text = message.timestamp
                //receivedMessageView.visibility = View.GONE
                //receivedTimeStamp.visibility = View.GONE
            } else {
                //receivedMessageView.text = message.text
                //receivedTimeStamp.text = message.timestamp
                sentMessageView.visibility = View.GONE
                sentTimeStamp.visibility = View.GONE
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }

    fun addMessage(message: ChatMessages) {
        val newList = currentList.toMutableList()
        newList.add(message)
        submitList(newList)
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
