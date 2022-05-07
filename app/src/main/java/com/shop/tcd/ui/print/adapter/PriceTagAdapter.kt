package com.shop.tcd.ui.print.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.databinding.ItemPricetagBinding

class PriceTagAdapter(private val list: List<String>) :
    RecyclerView.Adapter<PriceTagAdapter.PriceTagViewHolder>() {

    inner class PriceTagViewHolder(val binding: ItemPricetagBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PriceTagViewHolder {
        val binding = ItemPricetagBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PriceTagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PriceTagViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.txtPriceTag.text = this
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}