package com.unitytests.virtumarttest.fragments.shopping

import android.app.AlertDialog
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.adapters.OrderProductsAdapter
import com.unitytests.virtumarttest.adapters.ShippingDetailsAdapter
import com.unitytests.virtumarttest.data.CartProducts
import com.unitytests.virtumarttest.data.ShippingDetails
import com.unitytests.virtumarttest.data.orders.Order
import com.unitytests.virtumarttest.data.orders.OrderStatuses
import com.unitytests.virtumarttest.databinding.FragmentOrderConfirmationBinding
import com.unitytests.virtumarttest.util.HorizontalRecyclerStylingClass
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.util.showNavBarVisibility
import com.unitytests.virtumarttest.viewmodel.OrderConfirmationVM
import com.unitytests.virtumarttest.viewmodel.PlaceOrderVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class OrderConfirmationFragment: Fragment() {

    private lateinit var binding: FragmentOrderConfirmationBinding
    private val ShippingDetailsAdapter by lazy {ShippingDetailsAdapter()}
    private val OrderProductsAdapter by lazy {OrderProductsAdapter()}
    private val orderConfirmationVM by viewModels<OrderConfirmationVM>()
    private val args by navArgs<OrderConfirmationFragmentArgs>()
    private var orderItems = emptyList<CartProducts>()
    private var totalCost = 0f
    private var selectedShippingAddress : ShippingDetails?=null
    private val placeOrderVM by viewModels<PlaceOrderVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        orderItems = args.orderProducts.toList()
        totalCost = args.totalOrderCost
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderConfirmationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showNavBarVisibility()
        setupShippingAddressesRV()
        setupOrderProductsRV()

        binding.imgAddShippingAddressesOrderView.setOnClickListener{
            findNavController().navigate(R.id.action_orderConfirmationFragment_to_shippingDetailsFragment)
        }

        lifecycleScope.launchWhenStarted {
            orderConfirmationVM.shippingDetails.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        binding.prgbrShippingAddressesOrderView.visibility = View.VISIBLE
                    }
                    is Resource.Success ->{
                        ShippingDetailsAdapter.differ.submitList(it.data)
                        binding.prgbrShippingAddressesOrderView.visibility = View.GONE
                    }
                    is Resource.Error ->{
                        binding.prgbrShippingAddressesOrderView.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error occurred: ${it.message.toString()}", Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            placeOrderVM.order.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        binding.btnPlaceOrder.startAnimation()
                    }
                    is Resource.Success ->{
                        binding.btnPlaceOrder.revertAnimation()
                        findNavController().navigateUp()
                        Snackbar.make(requireView(), "Order successful!", Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Error ->{
                        binding.btnPlaceOrder.revertAnimation()
                        Toast.makeText(requireContext(), "${it.message.toString()}", Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

        OrderProductsAdapter.differ.submitList(orderItems)
        binding.txtTotalCostOrderView.text = "Rs. ${String.format("%,.2f", totalCost)}"

        ShippingDetailsAdapter.onClick = {
            selectedShippingAddress = it
        }

        binding.btnPlaceOrder.setOnClickListener{
            if(selectedShippingAddress == null){
                Toast.makeText(requireContext(),"Select an address to ship the order!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            displayConfirmationDialog()
        }

        binding.btnBackOrderView.setOnClickListener{
            findNavController().navigateUp()
        }
    }

    private fun displayConfirmationDialog() {
        val viewOrderConfirmationDialog = AlertDialog.Builder(requireContext()).apply {
            setMessage("Confirm Your Order?")
            setNegativeButton("No") {dialogBox,_ ->
                dialogBox.dismiss()
            }
            setPositiveButton("Yes"){dialogBox,_ ->
                val order = Order(
                    OrderStatuses.Ordered.status,
                    totalCost,
                    orderItems,
                    selectedShippingAddress!!
                )
                placeOrderVM.placeOrder(order)
                dialogBox.dismiss()
            }
        }
        viewOrderConfirmationDialog.create()
        viewOrderConfirmationDialog.show()
    }

    private fun setupShippingAddressesRV() {
        binding.rvShippingAddressesOrderView.apply{
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = ShippingDetailsAdapter
            addItemDecoration(HorizontalRecyclerStylingClass())
        }
    }

    private fun setupOrderProductsRV() {
        binding.rvOrderProductsOrderView.apply{
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = OrderProductsAdapter
            addItemDecoration(HorizontalRecyclerStylingClass())
        }
    }

    override fun onResume() {
        super.onResume()
        showNavBarVisibility()
    }
}