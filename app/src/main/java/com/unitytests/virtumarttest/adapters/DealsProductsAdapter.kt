package com.unitytests.virtumarttest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.unitytests.virtumarttest.data.Product
import com.unitytests.virtumarttest.databinding.DealsProductsRecyclerviewItemsBinding

class DealsProductsAdapter: RecyclerView.Adapter<DealsProductsAdapter.DealsProductViewHolder>(){

    inner class DealsProductViewHolder(private val binding: DealsProductsRecyclerviewItemsBinding): ViewHolder(binding.root){
        fun bind(product: Product){
            binding.apply {
                Glide.with(itemView).load(product.imageURLs[0]).into(imgProductDealsView)
                txtTitleProductDealsView.text=product.productName
                product.offerPercentage?.let{
                    // Discount calculation
                    val priceAfterDiscount = (product.price*(1f-it))
                    // The new price display with only two decimal values
                    txtNewPriceProductDealsView.text="${String.format("%,.2f",priceAfterDiscount)}"
                    // Offer percentage display
                    val discountPercentage= product.offerPercentage*100
                    txtProductOfferPercentageDealsView.text="${String.format("%.1f",discountPercentage)}"
                }
                txtOldPriceProductDealsView.text=product.price.toString()

            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>(){
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ= AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealsProductViewHolder {
        return DealsProductViewHolder(
            DealsProductsRecyclerviewItemsBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }
    override fun onBindViewHolder(holder: DealsProductViewHolder, position: Int) {
        val product= differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener{
            onClick?.invoke(product)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((Product) -> Unit)? = null
}