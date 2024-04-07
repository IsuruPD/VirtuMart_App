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
import com.unitytests.virtumarttest.data.ShippingDetails
import com.unitytests.virtumarttest.databinding.FragmentShippingDetailsBinding
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.viewmodel.ShippingDetailsVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ShippingDetailsFragment: Fragment() {
    private lateinit var binding : FragmentShippingDetailsBinding
    val viewModel by viewModels<ShippingDetailsVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenStarted{
            viewModel.addNewShippingDetail.collectLatest{
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

        binding.apply{
            btnSaveShippingView.setOnClickListener{
                val addressAlias = shippingAliasEdt.text.toString()
                val receiverName = shippingReceiverNameEdt.text.toString()
                val address = shippingAddressEdt.text.toString()
                val city = shippingCityEdt.text.toString()
                val district = shippingDistrictEdt.text.toString()
                val contact = shippingContactEdt.text.toString()
                val shippingAddress = ShippingDetails(addressAlias, receiverName, address, city, district, contact)

                viewModel.addShippingDetails(shippingAddress)
            }
        }
    }
}