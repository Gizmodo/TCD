package com.shop.tcd.ui.inventory.chronology

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.shop.tcd.R
import com.shop.tcd.core.extension.fancyError
import com.shop.tcd.core.extension.fancyException
import com.shop.tcd.core.extension.getViewModel
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.data.dto.inventory.InvItem
import com.shop.tcd.databinding.FragmentInventoryChronologyBinding
import com.shop.tcd.ui.inventory.adapter.InventoryAdapter
import timber.log.Timber

class InventoryChronologyFragment : Fragment(R.layout.fragment_inventory_chronology) {
    private var data: List<InvItem> = mutableListOf()
    private val binding by viewBindingWithBinder(FragmentInventoryChronologyBinding::bind)
    private val viewModel: InventoryChronologyViewModel by lazy { getViewModel { InventoryChronologyViewModel() } }
    private var adapterInventory = InventoryAdapter(mutableListOf()) { onItemClick(it) }

    private fun onItemClick(inventoryItem: InvItem) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_inventory_quantity)

        val edtQuantity = dialog.findViewById<EditText>(R.id.edtQuantity)
        val btnUpdateQuantity = dialog.findViewById<Button>(R.id.btnUpdateQuantity)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val txtName = dialog.findViewById<TextView>(R.id.txtName)

        txtName.text = inventoryItem.name
        edtQuantity.setText(inventoryItem.quantity)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnUpdateQuantity.setOnClickListener {
            val quantity = edtQuantity.text.toString().replace(',', '.')
            Timber.d(quantity)
            quantity.toFloatOrNull()?.let {
                viewModel.updateInventoryQuantity(inventoryItem.uid!!, quantity)
                adapterInventory.notifyDataSetChanged()
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvInventoryChronology.adapter = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initViewModelObservers()
    }

    private fun initViewModelObservers() {
        viewModel.inventoryLiveData.observe(viewLifecycleOwner) { items ->
            data = items
            adapterInventory.updateList(items)
        }
        viewModel.exceptionMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            fancyException { it }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            fancyError { it }
        }
    }

    private fun initRecyclerView() {
        with(binding.rvInventoryChronology) {
            val animator = itemAnimator
            if (animator is SimpleItemAnimator) {
                animator.supportsChangeAnimations = false
            }
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    LinearLayout.VERTICAL
                )
            )
            adapter = adapterInventory
        }
    }
}
