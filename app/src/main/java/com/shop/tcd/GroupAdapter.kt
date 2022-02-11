package com.shop.tcd

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.databinding.RecyclerviewItemRowBinding
import com.shop.tcd.model.Group
import timber.log.Timber

class GroupAdapter(private val groupsList: ArrayList<Group>) :
    RecyclerView.Adapter<GroupAdapter.GroupsViewHolder>() {
    inner class GroupsViewHolder(val binding: RecyclerviewItemRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val binding =
            RecyclerviewItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        with(holder) {
            with(groupsList[position]) {
                binding.itemCode.text = code
                binding.itemName.text = name
                binding.itemCheckBox.setOnClickListener {
                    val element = groupsList[position]
                    element.checked = binding.itemCheckBox.isChecked
                    groupsList[position] = element
                    Timber.d(groupsList[position].toString())
                }
                holder.itemView.setOnClickListener {
                    Toast.makeText(
                        holder.itemView.context,
                        code, Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }
}