package com.unitytests.virtumarttest.adapters

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.unitytests.virtumarttest.R
import com.unitytests.virtumarttest.data.ShippingDetails
import com.unitytests.virtumarttest.databinding.ShippingAliasRecyclerviewItemsBinding

class ShippingDetailsAdapter: RecyclerView.Adapter<ShippingDetailsAdapter.ShippingDetailsViewHolder>() {

    inner class ShippingDetailsViewHolder(val binding: ShippingAliasRecyclerviewItemsBinding): RecyclerView.ViewHolder(binding.root){
        fun bindShippingDetails(shippingDetails: ShippingDetails, isSelected: Boolean) {
            binding.apply{
                btnShippingAliasRV.text = shippingDetails.addressAlias
                if(isSelected){
                    btnShippingAliasRV.background = ColorDrawable(itemView.context.resources.getColor(R.color.g_blue))
                }else{
                    btnShippingAliasRV.background = ColorDrawable(itemView.context.resources.getColor(R.color.g_white))
                }

            }
        }

    }

    private val diffUtil = object : DiffUtil.ItemCallback<ShippingDetails>(){
        override fun areItemsTheSame(oldItem: ShippingDetails, newItem: ShippingDetails): Boolean {
            return oldItem.addressAlias == newItem.addressAlias && oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: ShippingDetails, newItem: ShippingDetails): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShippingDetailsViewHolder {
        return ShippingDetailsViewHolder(
            ShippingAliasRecyclerviewItemsBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    var selectedShippingAddress = -1

    override fun onBindViewHolder(holder: ShippingDetailsViewHolder, position: Int) {
        val shippingDetails = differ.currentList[position]
        holder.bindShippingDetails(shippingDetails, selectedShippingAddress == position)

        holder.binding.btnShippingAliasRV.setOnClickListener{
            if (selectedShippingAddress >= 0){
                notifyItemChanged(selectedShippingAddress)
            }
            selectedShippingAddress = holder.adapterPosition
            notifyItemChanged(selectedShippingAddress)

            onClick?.invoke(shippingDetails)
        }
    }

    // Correct the double address selection issue when adding new ones
    init{
        differ.addListListener{_,_ ->
            notifyItemChanged(selectedShippingAddress)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((ShippingDetails) ->Unit)? = null
}