package com.unitytests.virtumarttest.fragments.manage

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
import com.unitytests.virtumarttest.adapters.OrderManageProductsAdapter
import com.unitytests.virtumarttest.util.Resource
import com.unitytests.virtumarttest.data.orders.OrderStatuses
import com.unitytests.virtumarttest.data.orders.getOrderStatuses
import com.unitytests.virtumarttest.databinding.FragmentOrderDetailBinding
import com.unitytests.virtumarttest.util.VerticalRecyclerStylingClass
import com.unitytests.virtumarttest.util.hideNavBarVisibility
import com.unitytests.virtumarttest.viewmodel.OrderStatusManagementVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class OrderDetailsFragment: Fragment() {

    private lateinit var binding: FragmentOrderDetailBinding
    private val userAllOrdersAdapter by lazy { OrderManageProductsAdapter() }
    private val args by navArgs<OrderDetailsFragmentArgs>()
    val viewModel by viewModels<OrderStatusManagementVM>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val order = args.order

        hideNavBarVisibility()
        setupOrdersRV()

        binding.btnBackOrderView.setOnClickListener{
            findNavController().navigateUp()
        }

        binding.apply{
            txtOrderIDOrderOver.text = "#"+order.orderId.toString()
            txtReceiverName.text=order.shippingAddress.receiverName.toString()
            txtShippingAddress.text=order.shippingAddress.address.toString()+",\n"+
                                        order.shippingAddress.city+", "+
                                        order.shippingAddress.district
            txtPhoneNumber.text=order.shippingAddress.contact.toString()
            txtOrderDateOrderView.text="(${order.orderDate})"
            txtOrderTotalOrderOverview.text="Rs. ${String.format("%,.2f", order.orderTotal)}"

            stepViewOrderOverview.setSteps(

                mutableListOf(
                    OrderStatuses.Ordered.status,
                    OrderStatuses.Shipped.status,
                    OrderStatuses.Delivered.status,
                    OrderStatuses.Complete.status,
                )
            )
            val currentOrderstatus = when(getOrderStatuses(order.orderStatus)){
                is OrderStatuses.Ordered -> 0
                is OrderStatuses.Shipped -> 1
                is OrderStatuses.Delivered -> 2
                is OrderStatuses.Complete -> 3
                is OrderStatuses.InDispute -> 4
                is OrderStatuses.Cancelled -> 5
                else -> 0
            }
            stepViewOrderOverview.go(currentOrderstatus, false)
            if(currentOrderstatus==3){
                stepViewOrderOverview.done(true)
            }

            if(currentOrderstatus==0){
                binding.orderStatusMessageText.text = "Your order is being processed"
                binding.cancelOrderBtn.visibility = View.VISIBLE
            } else if(currentOrderstatus==1){
                binding.orderStatusMessageText.text = "Your order is on the way! Sit back and Relax."
            } else if(currentOrderstatus==2){
                binding.orderStatusMessageText.text = "Are you happy with the purchase?"
                binding.applyDisputesBtn.visibility = View.VISIBLE
            } else if(currentOrderstatus==3){
                binding.orderStatusMessageText.text = "Your order has been fulfilled. Thank you for ordering from VirtuMart!"
                binding.provideFeedbackBtn.visibility = View.VISIBLE
            } else if(currentOrderstatus==4){
                binding.orderStatusMessageText.text = "Our team is working on resolving this issue. If you have any concerns please reach us through the chat."
            } else if(currentOrderstatus==5){
                binding.orderStatusMessageText.text = "This order has been cancelled."
            }

        }
        userAllOrdersAdapter.differ.submitList(order.orderItems)


        binding.cancelOrderBtn.setOnClickListener {
            // Update the status to "Cancelled"
            displayConfirmationDialog(order.orderId, OrderStatuses.Cancelled.status)
        }
        binding.applyDisputesBtn.setOnClickListener {
            // Update the status to "In Dispute"
            displayConfirmationDialog(order.orderId, OrderStatuses.InDispute.status)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.modifyOrderStatus.collectLatest {
                when(it){
                    is Resource.Loading ->{
                        binding.cancelOrderBtn.startAnimation()
                        binding.applyDisputesBtn.startAnimation()
                    }
                    is Resource.Success ->{
                        binding.cancelOrderBtn.revertAnimation()
                        binding.applyDisputesBtn.revertAnimation()
                        findNavController().navigateUp()
                        Snackbar.make(requireView(), "Order status updated!", Snackbar.LENGTH_LONG).show()
                    }
                    is Resource.Error ->{
                        binding.cancelOrderBtn.revertAnimation()
                        binding.applyDisputesBtn.revertAnimation()
                        Toast.makeText(requireContext(), "Error occurred: ${it.message.toString()}", Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setupOrdersRV(){
        binding.rvOrderItemsOrderOVerview.apply{
            adapter=userAllOrdersAdapter
            layoutManager=LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalRecyclerStylingClass())
        }
    }

    private fun displayConfirmationDialog(orderId: Long, status: String) {
        val viewOrderConfirmationDialog = AlertDialog.Builder(requireContext()).apply {
            setMessage("Confirm your request?")
            setNegativeButton("No") {dialogBox,_ ->
                dialogBox.dismiss()
            }
            setPositiveButton("Yes"){dialogBox,_ ->
                viewModel.updateOrderStatus(orderId, status)
                dialogBox.dismiss()
            }
        }
        viewOrderConfirmationDialog.create()
        viewOrderConfirmationDialog.show()
    }

    override fun onResume() {
        super.onResume()
        hideNavBarVisibility()
    }
}