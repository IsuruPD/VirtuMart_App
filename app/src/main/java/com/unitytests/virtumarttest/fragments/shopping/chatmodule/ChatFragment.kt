package com.unitytests.virtumarttest.fragments.shopping.chatmodule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.unitytests.virtumarttest.adapters.ChatHeadsAdapter
import com.unitytests.virtumarttest.databinding.FragmentChatBinding
import com.unitytests.virtumarttest.viewmodel.ChatVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatHeadsAdapter
    val viewModel by viewModels<ChatVM>()

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

        // Set click listener for send button
        binding.sendBtn.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        // Retrieve message text from EditText
        val messageText = binding.messageBodyChatViewEdt.text.toString().trim()

        if (messageText.isNotEmpty()) {
            val senderId = viewModel.getUserId()
            val imgFile: java.io.File? = null // Set to actual image file if needed

            lifecycleScope.launch {
                viewModel.sendMessage(senderId, messageText, imgFile)
            }
        }
    }
}
