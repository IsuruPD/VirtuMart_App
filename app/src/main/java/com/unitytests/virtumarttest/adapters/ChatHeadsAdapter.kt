package com.unitytests.virtumarttest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.unitytests.virtumarttest.data.ChatMetadata
import com.unitytests.virtumarttest.databinding.SentMessageRecyclerviewItemBinding

class ChatHeadsAdapter: RecyclerView.Adapter<ChatHeadsAdapter.ChatHeadsAdapterViewHolder>() {


    inner class ChatHeadsAdapterViewHolder(private val binding: SentMessageRecyclerviewItemBinding):
        RecyclerView.ViewHolder(binding.root){
        fun bind(chatMetaData: ChatMetadata){
            binding.apply {
                sentMessageChatView.text=chatMetaData.lastMessage
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<ChatMetadata> (){
        override fun areItemsTheSame(oldItem: ChatMetadata, newItem: ChatMetadata): Boolean {
            return oldItem.lastMessage==newItem.lastMessage
        }
        override fun areContentsTheSame(oldItem: ChatMetadata, newItem: ChatMetadata): Boolean {
            return oldItem == newItem
        }
    }

    val differ= AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatHeadsAdapter.ChatHeadsAdapterViewHolder {
        return ChatHeadsAdapterViewHolder(
            SentMessageRecyclerviewItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ChatHeadsAdapter.ChatHeadsAdapterViewHolder, position: Int) {
        val message= differ.currentList[position]
        holder.bind(message)

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}
