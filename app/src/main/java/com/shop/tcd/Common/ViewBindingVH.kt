package com.shop.tcd.Common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class ViewBindingVH constructor(val binding: ViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        inline fun create(
            parent: ViewGroup,
            crossinline block: (inflater: LayoutInflater, container: ViewGroup, attach: Boolean) -> ViewBinding
        ) = ViewBindingVH(block(LayoutInflater.from(parent.context), parent, false))
    }
}
/*

class CardAdapter : RecyclerView.Adapter<ViewBindingVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingVH {
        return ViewBindingVH.create(parent, CardBinding::inflate)
    }

    override fun onBindViewHolder(holder: ViewBindingVH, position: Int) {
        (holder.binding as CardBinding).apply {
            //bind model to view
            title.text = "some text"
            descripiton.text = "some text"
        }
    }

}*/
