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
import com.unitytests.virtumarttest.databinding.OrderProductsRecyclerviewItemsBinding
import com.unitytests.virtumarttest.helper.getProductPrice

class OrderProductsAdapter : RecyclerView.Adapter<OrderProductsAdapter.OrderProductsViewHolder>() {

    inner class OrderProductsViewHolder(val binding: OrderProductsRecyclerviewItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindOrderProductDetails(orderProducts: CartProducts) {
            binding.apply {
                Glide.with(itemView).load(orderProducts.product.imageURLs[0])
                    .into(imgProductOrderView)
                txtProductNameOrderView.text = orderProducts.product.productName
                txtProductQuantityOrderView.text = orderProducts.quantity.toString()
                val priceAfterDiscount =
                    orderProducts.product.offerPercentage.getProductPrice(orderProducts.product.price)
                txtProductPriceOrderView.text = "Rs. ${String.format("%,.2f", priceAfterDiscount)}"

                imgProductSelectedColorOrderView.setImageDrawable(
                    ColorDrawable(
                        orderProducts.selectedColor ?: Color.TRANSPARENT
                    )
                )
                txtProductSelectedSizeOrderView.text = orderProducts.selectedSize ?: "".also {
                    imgProductSelectedSizeOrderView.setImageDrawable(
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
            OrderProductsRecyclerviewItemsBinding.inflate(
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