package com.unitytests.virtumarttest.fragments.manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unitytests.virtumarttest.adapters.OrderManageProductsAdapter
import com.unitytests.virtumarttest.data.orders.OrderStatuses
import com.unitytests.virtumarttest.data.orders.getOrderStatuses
import com.unitytests.virtumarttest.databinding.FragmentOrderDetailBinding
import com.unitytests.virtumarttest.util.VerticalRecyclerStylingClass

class OrderDetailsFragment: Fragment() {

    private lateinit var binding: FragmentOrderDetailBinding
    private val userAllOrdersAdapter by lazy { OrderManageProductsAdapter() }
    private val args by navArgs<OrderDetailsFragmentArgs>()

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

        setupOrdersRV()
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
                else -> 0
            }
            stepViewOrderOverview.go(currentOrderstatus, false)
            if(currentOrderstatus==3){
                stepViewOrderOverview.done(true)
            }
        }
        userAllOrdersAdapter.differ.submitList(order.orderItems)
    }

    private fun setupOrdersRV(){
        binding.rvOrderItemsOrderOVerview.apply{
            adapter=userAllOrdersAdapter
            layoutManager=LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalRecyclerStylingClass())
        }
    }
}