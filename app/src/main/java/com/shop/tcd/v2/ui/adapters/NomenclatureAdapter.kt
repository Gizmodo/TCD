package com.shop.tcd.v2.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.databinding.ItemNomenclatureBinding
import com.shop.tcd.v2.data.nomenclature.NomenclatureItem

class NomenclatureAdapter(
    private val list: MutableList<NomenclatureItem>,
) : RecyclerView.Adapter<NomenclatureAdapter.NomenclatureViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NomenclatureViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNomenclatureBinding.inflate(inflater, parent, false)
        return NomenclatureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NomenclatureViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(itemsList: List<NomenclatureItem>) {
        list.clear()
        list.addAll(itemsList)
        notifyDataSetChanged()
    }

    inner class NomenclatureViewHolder(val binding: ItemNomenclatureBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(nomenclatureItem: NomenclatureItem) {
            binding.txtTitle.text = nomenclatureItem.name
            binding.txtBarcode.text = nomenclatureItem.barcode
            binding.txtCode.text = nomenclatureItem.code
        }
    }
}