package com.unitytests.virtumarttest.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unitytests.virtumarttest.data.CartProducts
import com.unitytests.virtumarttest.databinding.CartProductRecyclerviewItemsBinding
import com.unitytests.virtumarttest.helper.getProductPrice

class CartProductsAdapter : RecyclerView.Adapter<CartProductsAdapter.CartProductsAdapterViewHolder>() {

    inner class CartProductsAdapterViewHolder(val binding: CartProductRecyclerviewItemsBinding):
        RecyclerView.ViewHolder(binding.root){
        fun bind(cartProducts: CartProducts){
            binding.apply {
                Glide.with(itemView).load(cartProducts.product.imageURLs[0]).into(imgProductCartView)
                txtProductNameCartView.text=cartProducts.product.productName
                txtProductQuantityCartView.text=cartProducts.quantity.toString()

                val priceAfterDiscount = cartProducts.product.offerPercentage.getProductPrice(cartProducts.product.price)
                txtProductPriceCartView.text = "Rs. ${String.format("%,.2f", priceAfterDiscount)}"

                imgProductSelectedColorCartView.setImageDrawable(ColorDrawable(cartProducts.selectedColor?: Color.TRANSPARENT))
                txtProductSelectedSizeCartView.text = cartProducts.selectedSize?:"".also{imgProductSelectedSizeCartView.setImageDrawable(ColorDrawable(Color.TRANSPARENT))}
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<CartProducts> (){
        override fun areItemsTheSame(oldItem: CartProducts, newItem: CartProducts): Boolean {
            return oldItem.product.id==newItem.product.id
        }
        override fun areContentsTheSame(oldItem: CartProducts, newItem: CartProducts): Boolean {
            return oldItem == newItem
        }
    }

    val differ= AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartProductsAdapterViewHolder {
        return CartProductsAdapterViewHolder(
            CartProductRecyclerviewItemsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CartProductsAdapterViewHolder, position: Int) {
        val cartProducts= differ.currentList[position]
        holder.bind(cartProducts)

        holder.itemView.setOnClickListener{
            onProductClick?.invoke(cartProducts)
        }
        holder.binding.btnProductQuantityIncreaseCartView.setOnClickListener{
            onProductAddClick?.invoke(cartProducts)
        }
        holder.binding.btnProductQuantityDecreaseCartView.setOnClickListener{
            onProductRemoveClick?.invoke(cartProducts)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onProductClick: ((CartProducts) -> Unit)? = null
    var onProductAddClick: ((CartProducts) -> Unit)? = null
    var onProductRemoveClick: ((CartProducts) -> Unit)? = null
}