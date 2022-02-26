package com.shop.tcd.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.databinding.InvItemBinding
import com.shop.tcd.model.InvItem

class InvAdapterGeneric(
    private val list: List<InvItem>
) :
    RecyclerView.Adapter<InvAdapterGeneric.VH>() {
    inner class VH(val binding: InvItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding =
            InvItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.txtInvId.text = uid.toString()
                binding.txtInvBarcode.text = barcode
                binding.txtInvCode.text = code
                binding.txtInvPlu.text = plu
                binding.txtInvQuantity.text = quantity.toString()
            }
        }
     holder.itemView.setOnClickListener {  }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}
