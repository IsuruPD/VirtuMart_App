package com.unitytests.virtumarttest.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.unitytests.virtumarttest.data.Messages
import com.unitytests.virtumarttest.databinding.SentMessageRecyclerviewItemBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ChatHeadsAdapter: RecyclerView.Adapter<ChatHeadsAdapter.ChatHeadsAdapterViewHolder>() {


    inner class ChatHeadsAdapterViewHolder(private val binding: SentMessageRecyclerviewItemBinding):
        RecyclerView.ViewHolder(binding.root){
        fun bind(message: Messages){
            binding.apply {
                Log.d("Messages: ", message.text)
                Log.d("Messages: ", message.createdAt.toString())
                sentMessageChatView.text=message.text
                sentTimeStamp.text= message.createdAt?.let {
                    formatTimestamp(it)
                }
            }
        }

        // Function to format Timestamp to a readable string
        private fun formatTimestamp(timestamp: Timestamp): String {
            // Convert Timestamp to Date
            val date = timestamp.toDate()
            // Define the desired format
            val dateFormat = SimpleDateFormat("HH:mm, MMM d, yyyy", Locale.getDefault())
            // Return formatted date string
            return dateFormat.format(date)
        }
    }


    private val diffCallback = object : DiffUtil.ItemCallback<Messages> (){
        override fun areItemsTheSame(oldItem: Messages, newItem: Messages): Boolean {
            return oldItem.text==newItem.text
        }
        override fun areContentsTheSame(oldItem: Messages, newItem: Messages): Boolean {
            return oldItem == newItem
        }
    }

    val differ= AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatHeadsAdapter.ChatHeadsAdapterViewHolder {
        return ChatHeadsAdapterViewHolder(
            SentMessageRecyclerviewItemBinding
            .inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ChatHeadsAdapter.ChatHeadsAdapterViewHolder, position: Int) {
        val message= differ.currentList[position]
        holder.bind(message)
        Log.d("Bind Message: ", message.toString())
    }

    override fun getItemCount(): Int {
        Log.d("List size: ", differ.currentList.size.toString())
        return differ.currentList.size
    }

}
