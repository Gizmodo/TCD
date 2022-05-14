package com.shop.tcd.ui.inventory

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.textfield.TextInputEditText
import com.shop.tcd.R
import com.shop.tcd.core.extension.*
import com.shop.tcd.core.utils.Constants
import com.shop.tcd.core.utils.StatefulData
import com.shop.tcd.core.utils.StatefulData.Error
import com.shop.tcd.data.dto.inventory.InvItem
import com.shop.tcd.data.dto.inventory.InventoryPair
import com.shop.tcd.data.dto.nomenclature.NomenclatureItem
import com.shop.tcd.databinding.FragmentInventoryBinding
import com.shop.tcd.ui.inventory.adapter.InventoryAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class InventoryFragment : Fragment(R.layout.fragment_inventory) {
    private lateinit var edtBarcode: TextInputEditText
    private lateinit var edtCount: TextInputEditText
    private lateinit var txtName: TextView
    private lateinit var txtCode: TextView
    private lateinit var txtBarcode: TextView
    private lateinit var txtPLU: TextView
    private lateinit var txtPrice: TextView
    private lateinit var txtTotalCount: TextView
    private lateinit var rvInventory: RecyclerView
    private lateinit var progressDialog: ProgressDialog
    private var jobAuto: Job? = null

    private val binding by viewBindingWithBinder(FragmentInventoryBinding::bind)
    private val viewModel: InventoryViewModel by lazy { getViewModel { InventoryViewModel() } }

    private var data: List<InvItem> = mutableListOf()
    private var adapterInventory = InventoryAdapter(mutableListOf()) { onItemClick(it) }

    override fun onDestroyView() {
        super.onDestroyView()
        rvInventory.adapter = null
        jobAuto?.cancel()
    }

    private fun onItemClick(inventoryItem: InvItem) {
        navigateExt(InventoryFragmentDirections.actionInventoryFragmentToInventoryItemDetailFragment(
            inventoryItem.code,
            inventoryItem.barcode
        ))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initNavigationMenuListeners()
        initViewModelObservers()
        initBarcodeFieldListener()
        attachHideKeyboardListeners()
    }

    private fun initBarcodeFieldListener() {
        edtBarcode.setOnFocusChangeListener { _, _ -> hideKeyboard() }
        edtBarcode.setOnClickListener { hideKeyboard() }
        jobAuto = barcodeListener()
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun barcodeListener() = edtBarcode
        .textChanges()
        .filterNot { it.isNullOrBlank() }
        .debounce(Constants.Inventory.DEBOUNCE_TIME)
        .flatMapLatest {
            viewModel.searchProduct(it.toString())
        }
        .onEach {
            handleProduct(it)
        }
        .launchIn(lifecycleScope)

    private fun handleProduct(state: StatefulData<InventoryPair>) {
        when (state) {
            is Error -> {
                Timber.e(state.msg)
                clearFields(allFields = false)
            }
            is StatefulData.Success -> {
                showPrevCount(state.result.previousItem)
                showProduct(state.result.currentItem)
                moveFocus(edtCount)
            }
            else -> {
                fancyError { "Not implemented" }
            }
        }
    }

    private fun showProduct(item: NomenclatureItem?) {
        item?.let {
            with(item) {
                txtName.text = name
                txtCode.text = code
                txtBarcode.text = barcode
                txtPrice.text = price
                txtPLU.text = plu
            }
            when (val response = viewModel.parseBarcode(item,edtBarcode.text.toString())) {
                is Error -> {
                    fancyErrorShort { response.msg }
                    edtCount.setText("")
                }
                is StatefulData.Success -> {
                    edtCount.setText(response.result)
                    moveFocus(edtCount)
                }
                is StatefulData.Notify -> {
                    fancyInfoShort { response.msg }
                    edtCount.setText("")
                }
                else -> {
                    fancyError { "Not implemented" }
                    edtCount.setText("")
                }
            }
        }
    }

    private fun showPrevCount(item: String?) {
        when {
            item != null -> {
                txtTotalCount.text = item
            }
            else -> {
                txtTotalCount.text = "0"
            }
        }
    }

    private fun initNavigationMenuListeners() {
        binding.navView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_clean -> {
                    clearInventoryData()
                    true
                }
                R.id.menu_send -> {
                    sendTo1C()
                    true
                }
                R.id.menu_chronology -> {
                    navigateExt(InventoryFragmentDirections.actionInventoryFragmentToInventoryChronologyFragment())
                    true
                }
                R.id.menu_insert -> {
                    addNomenclatureItem()
                    true
                }
                else -> false
            }
        }
    }

    private fun sendTo1C() {
        val builderAlert = AlertDialog.Builder(requireContext())
        with(builderAlert) {
            setTitle("Внимание")
            setMessage("Выгрузить документы в 1С?")
            setPositiveButton("Да") { _: DialogInterface, _: Int ->
                viewModel.sendResults()
            }
            setNegativeButton("Нет") { _, _ -> fancyInfo { "Выгрузка отменена" } }
            show()
        }
    }

    private fun addNomenclatureItem() {
        val count = edtCount.text.toString().replace(',', '.')
        when {
            count.toFloatOrNull() == null -> {
                longFancyConfusing { "Укажите количество!" }
            }
            edtBarcode.text.toString().toBigIntegerOrNull() == null -> {
                longFancyConfusing { "ШК пустой!" }
            }
            else -> {
                val inv = InvItem("", "", "", "", "")
                with(inv) {
                    barcode = txtBarcode.text.toString()
                    code = txtCode.text.toString()
                    name = txtName.text.toString()
                    plu = txtPLU.text.toString()
                    quantity = edtCount.text.toString()
                }
                viewModel.insertInventory(inv)
                clearFields()
                moveFocus(edtBarcode)
            }
        }
    }

    private fun clearInventoryData() {
        val builderAlert = AlertDialog.Builder(requireContext())
        with(builderAlert) {
            setTitle("Внимание")
            setMessage("Очистить все записи инвентаризации?")
            setPositiveButton("Да") { _: DialogInterface, _: Int ->
                viewModel.clearInventory()
            }
            setNegativeButton(
                "Нет"
            ) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            show()
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
        if (hasFocus) (view as TextInputEditText).selectAll()
    }

    private fun hideKeyboard() {
        this.hideSoftKeyboardExt()
    }

    private fun initViewModelObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadInventoryList()
            }
        }

        viewModel.inventoryList.observe(viewLifecycleOwner) { items ->
            data = items
            adapterInventory.updateList(data)
            adapterInventory.notifyDataSetChanged()
        }

        viewModel.urovoKeyboard.observe(viewLifecycleOwner) {
            if (edtCount.isFocused) {
                moveFocus(edtCount)
                addNomenclatureItem()
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

        viewModel.exceptionMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            fancyException { it }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            fancyError { it }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            fancySuccessShort { it }
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            when {
                it -> progressDialog.show()
                else -> progressDialog.dismiss()
            }
        }
    }

    private fun onReceiveScannerData(message: String) {
        clearFields()
        edtBarcode.apply {
            setText(message)
            moveFocus(this)
        }
    }

    private fun clearFields(allFields: Boolean = true) {
        "".also {
            edtCount.setText(it)
            txtName.text = it
            txtCode.text = it
            txtBarcode.text = it
            txtPrice.text = it
            txtPLU.text = it
            txtTotalCount.text = it
        }
        if (allFields) {
            edtBarcode.setText("")
        }
    }

    private fun moveFocus(view: EditText) {
        view.requestFocus();
        view.setSelection(view.text.length, 0);
    }

    private fun initUI() {
        rvInventory = binding.rvInventory
        edtBarcode = binding.edtBarcode
        edtCount = binding.edtCount
        txtName = binding.txtValueName
        txtCode = binding.txtValueCode
        txtBarcode = binding.txtValueBarcode
        txtPLU = binding.txtValuePLU
        txtPrice = binding.txtValuePrice
        txtTotalCount = binding.txtValueTotalCount

        progressDialog = ProgressDialog(requireContext()).apply {
            setTitle("Загрузка...")
            setMessage("Пожалуйста, ожидайте")
        }
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
                    LinearLayout.VERTICAL
                )
            )
            adapter = adapterInventory
        }
    }
}