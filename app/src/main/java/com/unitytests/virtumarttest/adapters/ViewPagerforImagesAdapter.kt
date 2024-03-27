package com.unitytests.virtumarttest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.unitytests.virtumarttest.databinding.ViewpagerImageItemsBinding

class ViewPagerforImagesAdapter: RecyclerView.Adapter<ViewPagerforImagesAdapter.ViewPagerforImagesAdapterViewHolder>() {

    inner class ViewPagerforImagesAdapterViewHolder(val binding: ViewpagerImageItemsBinding): ViewHolder(binding.root){

        fun bindImage(imagePath: String){
            Glide.with(itemView).load(imagePath).into(binding.imageViewProductsView)
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<String>(){
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem==newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem== newItem
        }
    }

    val differ =AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerforImagesAdapterViewHolder {
        return ViewPagerforImagesAdapterViewHolder(
            ViewpagerImageItemsBinding.inflate(
            LayoutInflater.from(parent.context),parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewPagerforImagesAdapterViewHolder, position: Int) {
        val image = differ.currentList[position]
        holder.bindImage(image)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}