package com.shop.tcd.v2.screen.inventory.chronology

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.shop.tcd.R
import com.shop.tcd.databinding.FragmentInventoryChronologyBinding
import com.shop.tcd.model.InvItem
import com.shop.tcd.v2.core.extension.getViewModel
import com.shop.tcd.v2.core.extension.longFancy
import com.shop.tcd.v2.core.extension.viewBindingWithBinder
import com.shop.tcd.v2.ui.adapters.InventoryAdapter
import timber.log.Timber

class InventoryChronologyFragment : Fragment(R.layout.fragment_inventory_chronology) {
    private var data: List<InvItem> = mutableListOf()
    private val binding by viewBindingWithBinder(FragmentInventoryChronologyBinding::bind)
    private lateinit var rvInventoryChronology: RecyclerView
    private lateinit var shimmer: ConstraintLayout
    private var adapterInventory = InventoryAdapter(mutableListOf()) { inventoryItem, position ->
        onItemClick(inventoryItem, position)
    }

    private fun onItemClick(inventoryItem: InvItem, position: Int) {
        Timber.d("Был клик по элемену $inventoryItem в позиции $position")
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

    private val viewModel: InventoryChronologyViewModel by lazy { getViewModel { InventoryChronologyViewModel() } }

    override fun onDestroyView() {
        super.onDestroyView()
        rvInventoryChronology.adapter = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initRecyclerView()
        initViewModelObservers()
    }

    private fun initViewModelObservers() {
        viewModel.inventoryLiveData.observe(viewLifecycleOwner) { items ->
            data = items
            adapterInventory.updateList(items)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            longFancy { it }
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            when {
                it -> showShimmer()
                else -> hideShimmer()
            }
        }
    }

    private fun initRecyclerView() {
        with(rvInventoryChronology) {
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

    private fun initUI() {
        rvInventoryChronology = binding.rvInventoryChronology
        shimmer = binding.shimmer
    }

    private fun showShimmer() {
        shimmer.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        shimmer.visibility = View.GONE
    }
}