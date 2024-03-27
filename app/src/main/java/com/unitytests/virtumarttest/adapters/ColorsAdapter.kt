package com.unitytests.virtumarttest.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.unitytests.virtumarttest.databinding.ColorRecyclerviewItemsBinding

class ColorsAdapter: RecyclerView.Adapter<ColorsAdapter.ColorsViewHolder>() {

    private var selectedPosition =-1

    inner class ColorsViewHolder(private val binding: ColorRecyclerviewItemsBinding) : ViewHolder(binding.root){
        fun bindColor(color: String, position: Int){
            val colorInt = Color.parseColor(color) // Convert hexadecimal string to integer color value
            val colorDrawable = ColorDrawable(colorInt)
            binding.ImageColor.setImageDrawable(colorDrawable)

            if (position== selectedPosition){ //Color Selected
                binding.apply {
                    ImageShadow.visibility= View.VISIBLE
                    imgSelectedColor.visibility=View.VISIBLE
                }
            }else{ //Colorr Not Selected
                binding.apply {
                    ImageShadow.visibility= View.INVISIBLE
                    imgSelectedColor.visibility=View.INVISIBLE
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorsViewHolder {
        return ColorsViewHolder(
            ColorRecyclerviewItemsBinding.inflate(
                LayoutInflater.from(parent.context),parent, false)
        )
    }

    override fun onBindViewHolder(holder: ColorsViewHolder, position: Int) {
        val color = differ.currentList[position]
        holder.bindColor(color, position)

        holder.itemView.setOnClickListener{
            if(selectedPosition<=0){
                notifyItemChanged(selectedPosition)
            }
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)
            onItemClick?.invoke(color)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    val onItemClick: ((String) -> Unit)? =null
}