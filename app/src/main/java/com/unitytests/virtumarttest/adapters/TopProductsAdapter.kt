package com.unitytests.virtumarttest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unitytests.virtumarttest.data.Product
import com.unitytests.virtumarttest.databinding.TopProductsRecyclerviewItemsBinding


class TopProductsAdapter: RecyclerView.Adapter<TopProductsAdapter.TopProductsAdapterViewHolder>() {

    inner class TopProductsAdapterViewHolder(private val binding: TopProductsRecyclerviewItemsBinding):
        RecyclerView.ViewHolder(binding.root){
            fun bind(product: Product){
                binding.apply {
                    Glide.with(itemView).load(product.imageURLs[0]).into(imgProductTopView)
                    txtTitleProductTopView.text=product.productName
                    txtPriceProductTopView.text=product.price.toString()
                }
            }
        }

    private val diffCallback = object : DiffUtil.ItemCallback<Product> (){
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id==newItem.id
        }
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ= AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TopProductsAdapterViewHolder {
        return TopProductsAdapterViewHolder(
            TopProductsRecyclerviewItemsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: TopProductsAdapterViewHolder, position: Int) {
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