package com.unitytests.virtumarttest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unitytests.virtumarttest.data.Product
import com.unitytests.virtumarttest.databinding.CatalogueGalleryRecyclerviewItemsBinding

class SearchProductsAdapter : ListAdapter<Product, SearchProductsAdapter.SearchProductViewHolder>(diffCallback) {

    inner class SearchProductViewHolder(private val binding: CatalogueGalleryRecyclerviewItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                Glide.with(itemView).load(product.imageURLs[0]).into(imgProductGalleryView)
                txtTitleProductGalleryView.text = product.productName

                product.offerPercentage?.let {
                    // Discount calculation
                    val priceAfterDiscount = (product.price * (1f - it))
                    // The new price display with only two decimal values
                    txtPriceProductGalleryView.text = "${String.format("%,.2f", priceAfterDiscount)}"
                    // Offer percentage display
                    val discountPercentage = product.offerPercentage * 100
                    txtOfferPercentageProductGalleryView.text = "${String.format("%.2f", discountPercentage)}% off"

                    txtOldPriceProductGalleryView.text = "${String.format("%,.2f", product.price)}"
                    if (product.offerPercentage == null || discountPercentage == 0f) {
                        txtOfferPercentageProductGalleryView.visibility = View.INVISIBLE
                        oldPriceDisplayGallery.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
                return oldItem.productId == newItem.productId
            }

            override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchProductViewHolder {
        return SearchProductViewHolder(
            CatalogueGalleryRecyclerviewItemsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SearchProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
    }

    var onClick: ((Product) -> Unit)? = null
}