package com.unitytests.virtumarttest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unitytests.virtumarttest.data.Product
import com.unitytests.virtumarttest.databinding.CatalogueGalleryRecyclerviewItemsBinding

class GalleryProductsAdapter: RecyclerView.Adapter<GalleryProductsAdapter.GalleryProductViewHolder>() {
    inner class GalleryProductViewHolder(private val binding: CatalogueGalleryRecyclerviewItemsBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(product: Product){
            binding.apply {
                Glide.with(itemView).load(product.imageURLs[0]).into(imgProductGalleryView)
                txtTitleProductGalleryView.text=product.productName

                product.offerPercentage?.let{
                    // Discount calculation
                    val priceAfterDiscount = (product.price*(1f-it))
                    // The new price display with only two decimal values
                    txtPriceProductGalleryView.text="${String.format("%,2f",priceAfterDiscount)}"
                    // Offer percentage display
                    val discountPercentage= product.offerPercentage*100
                    txtOfferPercentageProductGalleryView.text=discountPercentage.toString()
                }
                if(product.offerPercentage==null){
                    txtOfferPercentageProductGalleryView.visibility= View.INVISIBLE
                }
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>(){
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ= AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryProductViewHolder {
        return GalleryProductViewHolder(
            CatalogueGalleryRecyclerviewItemsBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }
    override fun onBindViewHolder(holder: GalleryProductViewHolder, position: Int) {
        val product= differ.currentList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}