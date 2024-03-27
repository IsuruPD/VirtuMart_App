package com.unitytests.virtumarttest.adapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.unitytests.virtumarttest.databinding.SizeRecyclerviewItemsBinding

class SizesAdapter: RecyclerView.Adapter<SizesAdapter.SizesViewHolder>() {

    private var selectedPosition =-1

    inner class SizesViewHolder(private val binding: SizeRecyclerviewItemsBinding) : ViewHolder(binding.root){
        fun bindColor(size: String, position: Int){
            binding.txtSelectedSize.text=size
            if (position== selectedPosition){ //Size Selected
                binding.apply {
                    SizeShadow.visibility= View.VISIBLE
                }
            }else{ //Colorr Not Selected
                binding.apply {
                    SizeShadow.visibility= View.INVISIBLE
                }
            }
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

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SizesViewHolder {
        return SizesViewHolder(
            SizeRecyclerviewItemsBinding.inflate(
                LayoutInflater.from(parent.context),parent, false)
        )
    }

    override fun onBindViewHolder(holder: SizesViewHolder, position: Int) {
        val size = differ.currentList[position]
        holder.bindColor(size, position)

        holder.itemView.setOnClickListener{
            if(selectedPosition<=0){
                notifyItemChanged(selectedPosition)
            }
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)
            onItemClick?.invoke(size)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    val onItemClick: ((String) -> Unit)? =null
}