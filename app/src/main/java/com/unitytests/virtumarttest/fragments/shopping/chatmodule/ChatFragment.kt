package com.unitytests.virtumarttest.fragments.shopping.chatmodule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.unitytests.virtumarttest.adapters.ChatHeadsAdapter
import com.unitytests.virtumarttest.data.ChatMessages
import com.unitytests.virtumarttest.data.MessageType
import com.unitytests.virtumarttest.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatHeadsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the ChatHeadsAdapter
        chatAdapter = ChatHeadsAdapter()

        // Set up RecyclerView with LinearLayoutManager
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }

        // Set click listener for send button
        binding.sendBtn.setOnClickListener {
            sendMessage()
        }

        // Simulated received messages (replace with actual message retrieval logic)
        val messages = listOf(
            ChatMessages("Hello", MessageType.SENT),
            ChatMessages("Hi there!", MessageType.RECEIVED),
            ChatMessages("How are you?", MessageType.SENT)
        )

        // Submit the list of messages to the adapter
        chatAdapter.submitList(messages)
    }

    private fun sendMessage() {
        // Retrieve message text from EditText
        val messageText = binding.messageBodyChatViewEdt.text.toString().trim()

        // Check if message text is not empty
        if (messageText.isNotEmpty()) {
            // Create a ChatMessages object for the sent message
            val sentMessage = ChatMessages(messageText, MessageType.SENT)

            // Add the sent message to the adapter
            chatAdapter.addMessage(sentMessage)

            // Clear the EditText after sending the message
            binding.messageBodyChatViewEdt.text.clear()
        }
    }
}
