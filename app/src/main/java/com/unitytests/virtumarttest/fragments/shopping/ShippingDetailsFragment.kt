package com.unitytests.virtumarttest.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.unitytests.virtumarttest.data.ShippingDetails
import com.unitytests.virtumarttest.databinding.FragmentShippingDetailsBinding
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.util.hideNavBarVisibility
import com.unitytests.virtumarttest.viewmodel.ShippingDetailsVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ShippingDetailsFragment: Fragment() {
    private lateinit var binding : FragmentShippingDetailsBinding
    val viewModel by viewModels<ShippingDetailsVM>()
    val args by navArgs<ShippingDetailsFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenStarted{
            viewModel.addOrUpdateShippingDetail.collectLatest{
                when(it){
                    is Resource.Loading ->{
                        binding.prgbrShippingView.visibility = View.VISIBLE
                    }
                    is Resource.Success ->{
                        binding.prgbrShippingView.visibility = View.INVISIBLE
                        findNavController().navigateUp()
                    }
                    is Resource.Error ->{
                        Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.error.collectLatest {
                Toast.makeText(requireContext(),it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShippingDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Getting the values from argument
        val shippingDetails = args.shippingDetails


        hideNavBarVisibility()

        binding.btnBackShippingView.setOnClickListener{
            findNavController().navigateUp()
        }

        if(shippingDetails == null){
            binding.btnDeleteShippingView.visibility = View.GONE
            binding.btnUpdateShippingView.visibility = View.GONE
        }else{
            binding.apply{
                shippingAliasEdt.isEnabled = false
                binding.btnSaveShippingView.visibility = View.GONE

                shippingAliasEdt.setText(shippingDetails.addressAlias)
                shippingReceiverNameEdt.setText(shippingDetails.receiverName)
                shippingAddressEdt.setText(shippingDetails.address)
                shippingCityEdt.setText(shippingDetails.city)
                shippingDistrictEdt.setText(shippingDetails.district)
                shippingContactEdt.setText(shippingDetails.contact)
            }
        }

        binding.apply {
            btnSaveShippingView.setOnClickListener {
                val addressAlias = shippingAliasEdt.text.toString()
                val receiverName = shippingReceiverNameEdt.text.toString()
                val address = shippingAddressEdt.text.toString()
                val city = shippingCityEdt.text.toString()
                val district = shippingDistrictEdt.text.toString()
                val contact = shippingContactEdt.text.toString()

                val shippingAddress = ShippingDetails(null, addressAlias, receiverName, address, city, district, contact)

                viewModel.addShippingDetails(shippingAddress)
            }

            btnUpdateShippingView.setOnClickListener {
                val shippingId = args.shippingDetails?.id ?: ""
                val addressAlias = shippingAliasEdt.text.toString()
                val receiverName = shippingReceiverNameEdt.text.toString()
                val address = shippingAddressEdt.text.toString()
                val city = shippingCityEdt.text.toString()
                val district = shippingDistrictEdt.text.toString()
                val contact = shippingContactEdt.text.toString()

                val shippingAddress = ShippingDetails(shippingId, addressAlias, receiverName, address, city, district, contact)

                viewModel.updateShippingDetails(shippingAddress)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        hideNavBarVisibility()
    }
}