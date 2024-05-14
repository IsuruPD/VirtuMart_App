package com.unitytests.virtumarttest.fragments.shopping.chatmodule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.unitytests.virtumarttest.adapters.ChatHeadsAdapter
import com.unitytests.virtumarttest.adapters.TopProductsAdapter
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

        setupChatsView()

        // Set up RecyclerView with LinearLayoutManager
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }


        // Set click listener for send button
        binding.sendBtn.setOnClickListener {
            sendMessage()
        }

        // Submit the list of messages to the adapter

        // Chats bottom reach
//        binding.nestedScrollMainCategory.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener{ v, _, scrollY, _, _ ->
//            //Check if the end of the page is reached
//            if(v.getChildAt(0).bottom <= v.height+scrollY){
//                //if yes fetch the next batch of products from firebase
                viewModel.fetchChatMessages()
//            }
//        })
    }

    private fun setupChatsView(){
        chatAdapter = ChatHeadsAdapter()
        binding.messagesRecyclerView.apply{
            layoutManager= LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter=chatAdapter
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
                binding.messageBodyChatViewEdt.text.clear()
            }
        }
    }
}