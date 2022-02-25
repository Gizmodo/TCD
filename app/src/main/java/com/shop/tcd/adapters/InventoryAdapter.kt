package com.shop.tcd.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.databinding.InvItemV2Binding
import com.shop.tcd.model.InvItem

class InventoryAdapter(private val InvList: List<InvItem>) :
    RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    inner class InventoryViewHolder(val binding: InvItemV2Binding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val binding =
            InvItemV2Binding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return InventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        with(holder) {
            with(InvList[position]) {
                binding.txtInvCode.text = code.trim()
                binding.txtInvName.text = name.trim()
                binding.txtInvQuantity.text = quantity.trim()
            }
        }
    }

    override fun getItemCount(): Int {
        return InvList.size
    }
}