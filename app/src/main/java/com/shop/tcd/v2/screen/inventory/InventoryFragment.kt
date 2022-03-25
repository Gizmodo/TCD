package com.shop.tcd.v2.screen.inventory

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.shop.tcd.R
import com.shop.tcd.databinding.FragmentInventoryBinding
import com.shop.tcd.model.InvItem
import com.shop.tcd.v2.core.extension.*
import com.shop.tcd.v2.core.utils.Common
import com.shop.tcd.v2.core.utils.Common.setReadOnly
import com.shop.tcd.v2.core.utils.Common.textChanges
import com.shop.tcd.v2.core.utils.Constants
import com.shop.tcd.v2.data.nomenclature.NomenclatureItem
import com.shop.tcd.v2.ui.adapters.InventoryAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
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
    private lateinit var rvInventory: RecyclerView

    private lateinit var jobAuto: Job
    private lateinit var jobManual: Job

    private val binding by viewBindingWithBinder(FragmentInventoryBinding::bind)
    private val viewModel: InventoryViewModel by lazy { getViewModel { InventoryViewModel() } }

    private var data: List<InvItem> = mutableListOf()
    private var adapterInventory = InventoryAdapter(mutableListOf()) { inventoryItem, position ->
        onItemClick(inventoryItem, position)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rvInventory.adapter = null
        jobAuto.cancel()
        jobManual.cancel()
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
        initBarcodeFieldListener()
    }

    private fun initBarcodeFieldListener() {
        edtBarcode.setOnFocusChangeListener { _, _ -> hideKeyboard() }
        edtBarcode.setOnClickListener { hideKeyboard() }
        edtBarcode.setReadOnly(value = true)
        jobAuto = createJobAuto()
    }

    private fun createJobAuto() = edtBarcode
        .textChanges()
        .distinctUntilChanged()
        // TODO: Remove as barcode can be empty
        .filterNot { it.isNullOrBlank() }
        .debounce(Constants.Inventory.DEBOUNCE_TIME)
        .flatMapLatest { viewModel.getProduct(it.toString()) }
        .onEach { displayFoundedItem(it) }
        .launchIn(lifecycleScope)

    private fun createJobManual() = edtBarcode
        .textChanges()
        .distinctUntilChanged()
        .filter {
            it?.isNotBlank() == true
        }
        .debounce(Constants.Inventory.DEBOUNCE_TIME)
        .flatMapLatest {
            viewModel.getProduct(it.toString())
        }
        .onEach {
            displayFoundedItem(it)
        }
        .launchIn(lifecycleScope)

    private fun displayFoundedItem(item: NomenclatureItem?) = when {
        item != null -> {

        }
        else -> {
            clearFields()
        }
    }

    private fun initUIListeners() {
        binding.navView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_clean -> {
                    true
                }
                R.id.menu_send -> {
//            sendTo1C()
                    true
                }
                R.id.menu_chronology -> {
                    navigateExt(InventoryFragmentDirections.actionInventoryFragmentToInventoryChronologyFragment())
                    true
                }
                R.id.menu_insert -> {
//            addNomenclatureItem()
                    true
                }
                R.id.menu_setting -> {
                    showMenu(binding.navView, R.menu.recalc_menu)
                    true
                }
                else -> false
            }
        }
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(context!!, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_mode_auto -> {
                    longFancy { "menu_mode_auto" }
                    true
                }
                else -> false
            }
        }
        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.
        popup.show()
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
        viewModel.urovoKeyboard.observe(viewLifecycleOwner) {
            if (edtCount.isFocused) {
                edtCount.setSelection(0)
                doInsert()
            } else if (edtBarcode.isFocused) {
                moveFocus(edtCount)
            }
        }
        viewModel.urovoScanner.observe(viewLifecycleOwner) {
            onReceiveScannerData(it)
        }
        viewModel.idataScanner.observe(viewLifecycleOwner) {
            onReceiveScannerData(it)
        }
    }

    private fun onReceiveScannerData(data: String) {
        clearFields()
        edtBarcode.apply {
            setText(data)
            requestFocus()
        }
    }

    private fun clearFields() {
        when (Common.currentScanMode) {
            Common.MODESCAN.AUTO -> {
                "".also {
                    edtBarcode.setText(it)
                    edtCount.setText(it)
                    txtGood.text = it
                    txtCode.text = it
                    txtBarcode.text = it
                    txtTotal.text = it
                    txtPrice.text = it
                }
            }
            Common.MODESCAN.MANUAL -> {
                "".also {
                    edtCount.setText(it)
                    txtGood.text = it
                    txtCode.text = it
                    txtBarcode.text = it
                    txtTotal.text = it
                    txtPrice.text = it
                }
            }
        }
    }

    private fun moveFocus(view: EditText) {
        view.requestFocus()
    }

    private fun doInsert() {
        longFancy { "Товар добавлен" }
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
//        btnInsert = binding.btnInsertItem
//        btnSend = binding.btnSend1C
    }
}