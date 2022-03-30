package com.shop.tcd.v2.screen.inventory

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.textfield.TextInputEditText
import com.shop.tcd.R
import com.shop.tcd.databinding.FragmentInventoryBinding
import com.shop.tcd.model.InvItem
import com.shop.tcd.v2.core.extension.*
import com.shop.tcd.v2.core.utils.Common
import com.shop.tcd.v2.core.utils.Common.setReadOnly
import com.shop.tcd.v2.core.utils.Common.textChanges
import com.shop.tcd.v2.core.utils.Constants
import com.shop.tcd.v2.core.utils.ResponseState
import com.shop.tcd.v2.data.inventory.InventoryResult
import com.shop.tcd.v2.data.nomenclature.NomenclatureItem
import com.shop.tcd.v2.ui.adapters.InventoryAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import timber.log.Timber

class InventoryFragment : Fragment(R.layout.fragment_inventory) {
    //    UI
    private lateinit var edtBarcode: TextInputEditText
    private lateinit var edtCount: TextInputEditText
    private lateinit var txtGood: TextView
    private lateinit var txtCode: TextView
    private lateinit var txtBarcode: TextView
    private lateinit var txtTotal: TextView
    private lateinit var txtPrice: TextView
    private lateinit var rvInventory: RecyclerView
    private lateinit var progressDialog: ProgressDialog
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
        jobAuto?.cancel()
        jobManual?.cancel()
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
        attachHideKeyboardListeners()
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
            txtGood.text = item.name
            txtCode.text = item.code
            txtBarcode.text = item.barcode
            // TODO: Это поле заполняется из ответа
            //  на запрос к БД с группировкой по штрихкоду
            txtTotal.text = item.code // TODO: 1) Выводить общее кол-во!!!
            txtPrice.text = item.price
            val barcode = edtBarcode
                .text
                .toString()
                .padStart(13, '0')
                .takeLast(13)

            when (val response = Common.parseBarcode(barcode)) {
                is ResponseState.Success -> {
                    edtCount.setText(response.item)
                }
                is ResponseState.Error -> {
                    longFancy { response.throwable.message.toString() }
                }
            }
        }
        else -> {
            clearFields()
        }
    }

    private fun initUIListeners() {
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
                R.id.menu_setting -> {
                    showMenu(binding.navView, R.menu.menu_inventory_settings)
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
//                 var list = mutableListOf<InvItem>()
//                list = arrayListOf()
                viewModel.fetchInventarisationItems()
                viewModel.getInventarisationItems().observe(viewLifecycleOwner) { items ->
                    var list = items as ArrayList<InvItem>
                    sendInventory(list)
                }
            }
            setNegativeButton("Нет") { _, _ -> fancyInfo { "Выгрузка отменена" } }
            show()
        }
    }

    private fun sendInventory(list: ArrayList<InvItem>) {
        if (list.isEmpty()) {
            return
        }
        progressDialog.show()
        val inventoryResult = InventoryResult(
            result = "success",
            message = "",
            operation = "revision",
            autor = Common.selectedUserModel.name,
            shop = Common.selectedShopModel.name,
            prefix = Common.selectedShopModel.prefix,
            document = list
        )
        viewModel.postInventory(inventoryResult)
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
                    barcode = edtBarcode.text.toString()
                    code = txtCode.text.toString()
                    name = txtGood.text.toString()
                    // TODO: PLU пустой, а должен быть заполнен
                    plu = edtBarcode.text.toString()
                    quantity = edtCount.text.toString()
                }
                viewModel.insertInventory(inv)
                clearFieldsAfterInsert()
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

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_mode_auto -> {
                    Common.currentScanMode = Common.MODESCAN.AUTO
                    edtBarcode.setReadOnly(true)
                    it.isChecked = true
                    jobManual.cancel()
                    jobAuto = createJobAuto()
                    true
                }
                R.id.menu_mode_manual -> {
                    Common.currentScanMode = Common.MODESCAN.MANUAL
                    edtBarcode.setReadOnly(false)
                    it.isChecked = true
                    jobAuto.cancel()
                    jobManual = createJobManual()
                    true
                }
                R.id.menu_find_barcode -> {
                    it.isChecked = true
                    Common.currentSearchMode = Common.SEARCHBY.BARCODE
                    true
                }
                R.id.menu_find_code -> {
                    it.isChecked = true
                    Common.currentSearchMode = Common.SEARCHBY.CODE
                    true
                }
                else -> false
            }
        }
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
        if (hasFocus) (view as TextInputEditText).selectAll()
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

    private fun onReceiveScannerData(message: String) {
        clearFields()
        edtBarcode.apply {
            setText(message)
            requestFocus()
        }
    }

    private fun clearFieldsAfterInsert() {
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
        edtBarcode = binding.edtBarcode
        edtCount = binding.edtCount
        txtGood = binding.txtZGood
        txtCode = binding.txtZCode
        txtBarcode = binding.txtZBarcode
        txtTotal = binding.txtZTotal
        txtPrice = binding.txtZPrice

        progressDialog = ProgressDialog(requireContext()).apply {
            setTitle("Загрузка...")
            setMessage("Пожалуйста, ожидайте")
        }
    }

    // TODO: привязать к выбору элемента из списка
    /* private val onItemClick = object : InvAdapter.OnItemClickListener {
         override fun onClick(invItem: InvItem, position: Int) {
             val bundle: Bundle = invItem.bundle(InvItem.serializer())
             val intent = Intent(this@InventoryFragment, DetailActivity::class.java)
                 .apply {
                     putExtra("item", bundle)
                 }
             startActivity(intent, bundle)
         }
     }*/
}