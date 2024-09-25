package com.unitytests.virtumarttest.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unitytests.virtumarttest.data.CartProducts
import com.unitytests.virtumarttest.databinding.OrderManagementProductsRecyclerviewItemsBinding
import com.unitytests.virtumarttest.helper.getProductPrice

class OrderManageProductsAdapter : RecyclerView.Adapter<OrderManageProductsAdapter.OrderProductsViewHolder>() {

    inner class OrderProductsViewHolder(val binding: OrderManagementProductsRecyclerviewItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindOrderProductDetails(orderProducts: CartProducts) {
            binding.apply {
                Glide.with(itemView).load(orderProducts.product.imageURLs[0])
                    .into(imgProductOrderMngView)
                txtProductNameOrderMngView.text = orderProducts.product.productName
                txtProductQuantityOrderMngView.text = orderProducts.quantity.toString()
                val priceAfterDiscount =
                    orderProducts.product.offerPercentage.getProductPrice(orderProducts.product.price)
                txtProductPriceOrderMngView.text = "Rs. ${String.format("%,.2f", priceAfterDiscount)}"

                imgProductSelectedColorOrderMngView.setImageDrawable(
                    ColorDrawable(
                        orderProducts.selectedColor ?: Color.TRANSPARENT
                    )
                )
                txtProductSelectedSizeOrderMngView.text = orderProducts.selectedSize ?: "".also {
                    imgProductSelectedSizeOrderMngView.setImageDrawable(
                        ColorDrawable(
                            Color.TRANSPARENT
                        )
                    )
                }
            }
        }

    }

    private val diffUtil = object : DiffUtil.ItemCallback<CartProducts>() {
        override fun areItemsTheSame(oldItem: CartProducts, newItem: CartProducts): Boolean {
            return oldItem.product == newItem.product
        }

        override fun areContentsTheSame(oldItem: CartProducts, newItem: CartProducts): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderProductsViewHolder {
        return OrderProductsViewHolder(
            OrderManagementProductsRecyclerviewItemsBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: OrderProductsViewHolder, position: Int) {
        val orderProduct = differ.currentList[position]
        holder.bindOrderProductDetails(orderProduct)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}