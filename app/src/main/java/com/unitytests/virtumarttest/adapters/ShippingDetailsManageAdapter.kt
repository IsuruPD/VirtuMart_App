package com.unitytests.virtumarttest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.unitytests.virtumarttest.data.ShippingDetails
import com.unitytests.virtumarttest.databinding.ManageShippingAddressesRecyclerviewItemsBinding

class ShippingDetailsManageAdapter: RecyclerView.Adapter<ShippingDetailsManageAdapter.ShippingDetailsManageViewHolder>() {

    inner class ShippingDetailsManageViewHolder(val binding: ManageShippingAddressesRecyclerviewItemsBinding): RecyclerView.ViewHolder(binding.root){
        fun bindShippingDetails(shippingDetails: ShippingDetails) {
            binding.apply{
                txtAliasManageShipping.text = shippingDetails.addressAlias
                txtReceiverManageShipping.text = shippingDetails.receiverName
                txtAddressManageShipping.text = shippingDetails.address
                txtCityManageShipping.text = shippingDetails.city
                txtDistrictManageShipping.text = shippingDetails.district
                txtContactManageShipping.text = shippingDetails.contact
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShippingDetailsManageViewHolder {
        return ShippingDetailsManageViewHolder(
            ManageShippingAddressesRecyclerviewItemsBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: ShippingDetailsManageViewHolder, position: Int) {
        val shippingDetails = differ.currentList[position]
        holder.bindShippingDetails(shippingDetails)

        holder.binding.cardManageShipping.setOnClickListener{
              onClick?.invoke(shippingDetails)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((ShippingDetails) ->Unit)? = null
}