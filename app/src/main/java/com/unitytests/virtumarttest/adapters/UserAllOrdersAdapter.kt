package com.unitytests.virtumarttest.adapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.data.orders.Order
import com.unitytests.virtumarttest.data.orders.OrderStatuses
import com.unitytests.virtumarttest.data.orders.getOrderStatuses
import com.unitytests.virtumarttest.databinding.UserOrdersRecyclerviewItemsBinding

class UserAllOrdersAdapter : RecyclerView.Adapter<UserAllOrdersAdapter.UserAllOrdersViewHolder>() {

    inner class UserAllOrdersViewHolder(
        private val binding: UserOrdersRecyclerviewItemsBinding,parent: ViewGroup,attachToParent: Boolean
    ):RecyclerView.ViewHolder(binding.root) {

        fun bindUserAllOrdersDetails(order: Order) {
            binding.apply {
                txtIDAllOrdersView.text=order.orderId.toString()
                txtDateAllOrdersView.text=order.orderDate

                val resources = itemView.resources

                val colorDrawable = when(getOrderStatuses(order.orderStatus)){
                    is OrderStatuses.Ordered ->{
                        ColorDrawable(resources.getColor(R.color.g_purple))
                    }
                    is OrderStatuses.Processing ->{
                        ColorDrawable(resources.getColor(R.color.g_purple))
                    }
                    is OrderStatuses.Shipped ->{
                        ColorDrawable(resources.getColor(R.color.g_purple))
                    }
                    is OrderStatuses.Delivered ->{
                        ColorDrawable(resources.getColor(R.color.g_purple))
                    }
                    is OrderStatuses.Complete ->{
                        ColorDrawable(resources.getColor(R.color.g_green))
                    }
                    is OrderStatuses.Cancelled ->{
                        ColorDrawable(resources.getColor(R.color.g_red))
                    }
                    is OrderStatuses.InDispute ->{
                        ColorDrawable(resources.getColor(R.color.g_orange_yellow))
                    }
                    is OrderStatuses.Returned ->{
                        ColorDrawable(resources.getColor(R.color.g_black))
                    }
                    is OrderStatuses.Refunded ->{
                        ColorDrawable(resources.getColor(R.color.g_black))
                    }
                }
                imgOrderStatusAllOrdersView.setImageDrawable(colorDrawable)
            }
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.orderItems == newItem.orderItems
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAllOrdersViewHolder {
        return UserAllOrdersViewHolder(UserOrdersRecyclerviewItemsBinding
            .inflate(LayoutInflater.from(parent.context)), parent, false)
    }


    override fun onBindViewHolder(holder: UserAllOrdersViewHolder, position: Int) {
        val orderProduct = differ.currentList[position]
        holder.bindUserAllOrdersDetails(orderProduct)

        holder.itemView.setOnClickListener{
            onClick?.invoke(orderProduct)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((Order)->Unit)?=null
}