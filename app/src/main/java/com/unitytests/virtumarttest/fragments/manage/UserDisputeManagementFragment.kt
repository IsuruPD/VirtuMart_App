package com.unitytests.virtumarttest.fragments.manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unitytests.virtumarttest.adapters.UserAllOrdersAdapter
import com.unitytests.virtumarttest.databinding.FragmentUserDisputeManagementBinding
import com.unitytests.virtumarttest.databinding.FragmentUserOrdersManagementBinding
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.util.hideNavBarVisibility
import com.unitytests.virtumarttest.viewmodel.DisputeOrdersVM
import com.unitytests.virtumarttest.viewmodel.OrdersVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class UserDisputeManagementFragment: Fragment() {

    private lateinit var binding : FragmentUserDisputeManagementBinding
    val viewModel by viewModels<DisputeOrdersVM>()
    val userAllOrdersAdapter by lazy {UserAllOrdersAdapter()}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentUserDisputeManagementBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideNavBarVisibility()
        setupOrdersRv()

        binding.btnBackAllDisputesView.setOnClickListener{
            findNavController().navigateUp()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.disputeOrders.collectLatest{
                when(it){
                    is Resource.Loading ->{
                        binding.prgbrAllDisputesView.visibility=View.VISIBLE
                    }
                    is Resource.Success ->{
                        binding.prgbrAllDisputesView.visibility=View.GONE
                        userAllOrdersAdapter.differ.submitList(it.data)

                        if(it.data.isNullOrEmpty()){
                            binding.txtNoItemsAllDisputesView.visibility=View.VISIBLE
                        }
                    }
                    is Resource.Error ->{
                        binding.prgbrAllDisputesView.visibility=View.GONE
                        Toast.makeText(requireContext(), "Error occurred: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

        userAllOrdersAdapter.onClick={
            val action= UserDisputeManagementFragmentDirections.actionUserDisputeManagementFragmentToOrderDetailsFragment(it)
            findNavController().navigate(action)
        }
    }

    private fun setupOrdersRv(){
        binding.rvAllDisputesView.apply{
            adapter = userAllOrdersAdapter
            layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL, false)
        }
    }

    override fun onResume() {
        super.onResume()
        hideNavBarVisibility()
    }
}