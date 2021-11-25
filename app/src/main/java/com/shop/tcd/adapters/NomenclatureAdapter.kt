package com.shop.tcd.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.databinding.RvNomenclatureItemBinding
import com.shop.tcd.model.NomenclatureItem

class NomenclatureAdapter(private val nomenclatureList: List<NomenclatureItem>) :
    RecyclerView.Adapter<NomenclatureAdapter.NomenclatureViewHolder>() {

    inner class NomenclatureViewHolder(val binding: RvNomenclatureItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NomenclatureViewHolder {
        val binding =
            RvNomenclatureItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return NomenclatureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NomenclatureViewHolder, position: Int) {
        with(holder) {
            with(nomenclatureList[position]) {
                binding.rvNomenclatureItemCode.text = code
                binding.rvNomenclatureItemName.text = name
            }
        }
    }

    override fun getItemCount(): Int {
        return nomenclatureList.size
    }

}