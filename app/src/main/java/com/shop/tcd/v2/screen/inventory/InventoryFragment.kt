package com.shop.tcd.v2.screen.inventory

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.shop.tcd.R
import com.shop.tcd.databinding.FragmentInventoryBinding
import com.shop.tcd.model.InvItem
import com.shop.tcd.v2.core.extension.getViewModel
import com.shop.tcd.v2.core.extension.hideSoftKeyboardExt
import com.shop.tcd.v2.core.extension.viewBindingWithBinder
import com.shop.tcd.v2.ui.adapters.InventoryAdapter
import timber.log.Timber

class InventoryFragment : Fragment(R.layout.fragment_inventory) {
    //    UI
    private lateinit var tilBarcode: TextInputLayout
    private lateinit var edtBarcode: TextInputEditText
    private lateinit var tilCount: TextInputLayout
    private lateinit var edtCount: TextInputEditText
    private lateinit var txtGood: TextView
    private lateinit var txtCode: TextView
    private lateinit var txtBarcode: TextView
    private lateinit var txtTotal: TextView
    private lateinit var txtPrice: TextView
    private lateinit var btnSend: Button
    private lateinit var btnInsert: Button

    private var data: List<InvItem> = mutableListOf()
    private val binding by viewBindingWithBinder(FragmentInventoryBinding::bind)
    private lateinit var rvInventory: RecyclerView
    private var adapterInventory = InventoryAdapter(mutableListOf()) { inventoryItem, position ->
        onItemClick(inventoryItem, position)
    }
    private val viewModel: InventoryViewModel by lazy { getViewModel { InventoryViewModel() } }

    override fun onDestroyView() {
        super.onDestroyView()
        rvInventory.adapter = null
    }

    private fun onItemClick(inventoryItem: InvItem, position: Int) {
        Timber.d("Был клик по элемену $inventoryItem в позиции $position")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initUIListeners()
        initRecyclerView()
        initViewModelObservers()
    }

    private fun initUIListeners() {
        btnSend.setOnClickListener {
//            sendTo1C()
        }
        btnInsert.setOnClickListener {
//            addNomenclatureItem()
        }
    }

    private fun attachHideKeyboardListeners() {
        binding.tilCount.apply {
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
        }
        binding.tilBarcode.apply {
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
        }

        edtCount.apply {
            showSoftInputOnFocus = false
            setSelectAllOnFocus(true)
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
            setOnFocusChangeListener { view, hasFocus -> onFocus(view, hasFocus) }
        }
        edtBarcode.apply {
            showSoftInputOnFocus = false
            setSelectAllOnFocus(true)
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
            setOnFocusChangeListener { view, hasFocus -> onFocus(view, hasFocus) }
        }
    }

    private fun onFocus(view: View?, hasFocus: Boolean) {
        if (hasFocus) {
            (view as TextInputEditText).selectAll()
        }
    }

    private fun hideKeyboard() {
        this.hideSoftKeyboardExt()
       /* val view = this.currentFocus
        if (view != null) {
            val hideMe = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)*/
    }

    private fun initViewModelObservers() {
        viewModel.inventoryList.observe(viewLifecycleOwner) { items ->
            data = items
            adapterInventory.updateList(data)
            adapterInventory.notifyDataSetChanged()
        }
    }

    private fun initRecyclerView() {
        with(rvInventory) {
            val animator = itemAnimator
            if (animator is SimpleItemAnimator) {
                animator.supportsChangeAnimations = false
            }
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    android.widget.LinearLayout.VERTICAL
                )
            )
            adapter = adapterInventory
        }
    }

    private fun initUI() {
        rvInventory = binding.rvInventory
        tilBarcode = binding.tilBarcode
        edtBarcode = binding.edtBarcode
        tilCount = binding.tilCount
        edtCount = binding.edtCount
        txtGood = binding.txtZGood
        txtCode = binding.txtZCode
        txtBarcode = binding.txtZBarcode
        txtTotal = binding.txtZTotal
        txtPrice = binding.txtZPrice
        btnInsert = binding.btnInsertItem
        btnSend = binding.btnSend1C
    }
}