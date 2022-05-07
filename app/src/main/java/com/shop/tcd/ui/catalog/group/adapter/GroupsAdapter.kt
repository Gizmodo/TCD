package com.shop.tcd.ui.catalog.group.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.data.dto.group.Group
import com.shop.tcd.databinding.ItemRowGroupsRvBinding

class GroupsAdapter(
    private val groupsList: MutableList<Group>,
) :
    RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder>() {
    override fun getItemCount() = groupsList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRowGroupsRvBinding.inflate(inflater, parent, false)
        return GroupsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        holder.bind(groupsList[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(itemsList: List<Group>) {
        groupsList.clear()
        groupsList.addAll(itemsList)
        notifyDataSetChanged()
    }

    inner class GroupsViewHolder(val binding: ItemRowGroupsRvBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(group: Group) {
            binding.itemCode.text = group.code
            binding.itemName.text = group.name
            binding.itemCheckBox.isChecked = group.checked
            binding.root.setOnClickListener {
                binding.itemCheckBox.isChecked = !binding.itemCheckBox.isChecked
                group.checked = binding.itemCheckBox.isChecked
            }
        }
    }
}
