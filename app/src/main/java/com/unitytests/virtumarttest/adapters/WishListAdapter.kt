package com.unitytests.virtumarttest.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unitytests.virtumarttest.data.WishListProducts
import com.unitytests.virtumarttest.databinding.WishlistRecyclerviewItemsBinding
import com.unitytests.virtumarttest.helper.getProductPrice

class WishListAdapter : RecyclerView.Adapter<WishListAdapter.WishListAdapterViewHolder>() {

    inner class WishListAdapterViewHolder(val binding: WishlistRecyclerviewItemsBinding):
        RecyclerView.ViewHolder(binding.root){
        fun bind(wishListProducts: WishListProducts){
            binding.apply {
                Glide.with(itemView).load(wishListProducts.product.imageURLs[0]).into(imgProductCartView)
                txtProductNameCartView.text=wishListProducts.product.productName

                val priceAfterDiscount = wishListProducts.product.offerPercentage.getProductPrice(wishListProducts.product.price)
                txtProductPriceCartView.text = "Rs. ${String.format("%,.2f", priceAfterDiscount)}"

                imgProductSelectedColorCartView.setImageDrawable(ColorDrawable(wishListProducts.selectedColor?: Color.TRANSPARENT))
                txtProductSelectedSizeCartView.text = wishListProducts.selectedSize?:"".also{imgProductSelectedSizeCartView.setImageDrawable(ColorDrawable(Color.TRANSPARENT))}
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<WishListProducts> (){
        override fun areItemsTheSame(oldItem: WishListProducts, newItem: WishListProducts): Boolean {
            return oldItem.product.productId==newItem.product.productId
        }
        override fun areContentsTheSame(oldItem: WishListProducts, newItem: WishListProducts): Boolean {
            return oldItem == newItem
        }
    }

    val differ= AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WishListAdapterViewHolder {
        return WishListAdapterViewHolder(
            WishlistRecyclerviewItemsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: WishListAdapterViewHolder, position: Int) {
        val cartProducts= differ.currentList[position]
        holder.bind(cartProducts)

        holder.itemView.setOnClickListener{
            onProductClick?.invoke(cartProducts)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onProductClick: ((WishListProducts) -> Unit)? = null
}