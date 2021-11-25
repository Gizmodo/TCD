package com.shop.tcd.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.databinding.InvItemV2Binding
import com.shop.tcd.model.InvItem

class InvAdapter(
    private val InvList: List<InvItem>,
    private val onItemClickListener: OnItemClickListener,
) :
    RecyclerView.Adapter<InvAdapter.InvViewHolder>() {
    inner class InvViewHolder(val binding: InvItemV2Binding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnItemClickListener {
        fun onClick(invItem: InvItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvViewHolder {
        val binding =
            InvItemV2Binding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return InvViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvViewHolder, position: Int) {
        with(holder) {
            with(InvList[position]) {
                /*  binding.txtInvId.text = uid.toString()
                  binding.txtInvBarcode.text = barcode
                  binding.txtInvCode.text = code
                  binding.txtInvPlu.text = plu*/
                binding.txtInvName.text = name.trim()
                binding.txtInvQuantity.text = quantity
            }

        }
        val item: InvItem = InvList[position]
        holder.itemView.setOnClickListener { onItemClickListener.onClick(item) }
    }

    override fun getItemCount(): Int {
        return InvList.size
    }
}