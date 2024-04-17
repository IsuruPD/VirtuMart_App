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
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.adapters.ShippingDetailsManageAdapter
import com.unitytests.virtumarttest.data.ShippingDetails
import com.unitytests.virtumarttest.databinding.FragmentShippingDetailsManagementBinding
import com.unitytests.virtumarttest.util.HorizontalRecyclerStylingClass
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.viewmodel.OrderConfirmationVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ShippingAddressManagementFragment: Fragment() {

    private lateinit var binding: FragmentShippingDetailsManagementBinding
    private val shippingDetailsManageAdapter by lazy { ShippingDetailsManageAdapter() }
    private val orderConfirmationVM by viewModels<OrderConfirmationVM>()
    private var selectedShippingAddress : ShippingDetails?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShippingDetailsManagementBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupManageAddressesRV()

        binding.btnBackManageShippingView.setOnClickListener{
            findNavController().navigateUp()
        }

        binding.imgAddShippingAddressesManageShippingView.setOnClickListener{
            findNavController().navigate(R.id.action_shippingAddressManagementFragment_to_shippingDetailsFragment)
        }

        lifecycleScope.launchWhenStarted {
            orderConfirmationVM.shippingDetails.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        binding.prgbrShippingAddressesManageShippingView.visibility = View.VISIBLE
                    }
                    is Resource.Success ->{
                        shippingDetailsManageAdapter.differ.submitList(it.data)
                        binding.prgbrShippingAddressesManageShippingView.visibility = View.GONE
                    }
                    is Resource.Error ->{
                        binding.prgbrShippingAddressesManageShippingView.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error occurred: ${it.message.toString()}", Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

        shippingDetailsManageAdapter.onClick = {
            selectedShippingAddress = it
            val bundle = Bundle().apply{putParcelable("shippingDetails", selectedShippingAddress)}
            findNavController().navigate(R.id.action_shippingAddressManagementFragment_to_shippingDetailsFragment, bundle)
        }
    }

    private fun setupManageAddressesRV() {
        binding.rvShippingAddressesManageShippingView.apply{
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = shippingDetailsManageAdapter
            addItemDecoration(HorizontalRecyclerStylingClass())
        }
    }
}