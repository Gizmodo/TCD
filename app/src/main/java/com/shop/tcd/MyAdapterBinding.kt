package com.shop.tcd

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.databinding.RecyclerviewItemRowBinding
import com.shop.tcd.model.Group

class MyAdapterBinding(private val groupsList: ArrayList<Group>) :
    RecyclerView.Adapter<MyAdapterBinding.GroupsViewHolder>() {
    val TAG = this::class.simpleName
    inner class GroupsViewHolder(val binding: RecyclerviewItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

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
                binding.itemCheckBox.setOnClickListener(View.OnClickListener {
                    val element= groupsList[position]
                    element.checked=binding.itemCheckBox.isChecked
                    groupsList[position] = element
                    Log.d(TAG, groupsList.toString())
                })
                holder.itemView.setOnClickListener {
                    Toast.makeText(holder.itemView.context,
                        code, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }
}