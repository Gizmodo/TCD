package com.shop.tcd.v2.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.databinding.ItemRowInventoryRvBinding
import com.shop.tcd.model.InvItem

class InventoryAdapter(
    private val inventoryList: MutableList<InvItem>,
    private val onItemClick: (InvItem) -> Unit,
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {
    override fun getItemCount() = inventoryList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRowInventoryRvBinding.inflate(inflater, parent, false)
        return InventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(inventoryList[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(itemsList: List<InvItem>) {
        inventoryList.clear()
        inventoryList.addAll(itemsList)
        notifyDataSetChanged()
    }

    inner class InventoryViewHolder(val binding: ItemRowInventoryRvBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(inventoryItem: InvItem) = binding.apply {
            txtInvCode.text = inventoryItem.code
            txtInvName.text = inventoryItem.name
            txtInvQuantity.text = inventoryItem.quantity
            root.setOnClickListener { onItemClick(inventoryItem) }
        }
    }
}