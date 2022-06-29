package com.shop.tcd.ui.remains

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.shop.tcd.R
import com.shop.tcd.core.extension.fancyError
import com.shop.tcd.core.extension.fancyException
import com.shop.tcd.core.extension.getViewModel
import com.shop.tcd.core.extension.hideSoftKeyboardExt
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.data.dto.remains.response.RemainsResponse
import com.shop.tcd.databinding.FragmentRemainsBinding

class RemainsFragment : Fragment(R.layout.fragment_remains) {
    private val binding by viewBindingWithBinder(FragmentRemainsBinding::bind)
    private lateinit var edtBarcode: TextInputEditText
    private lateinit var dialog: AlertDialog
    private val viewModel: RemainsViewModel by lazy {
        getViewModel { RemainsViewModel() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initUIListeners()
        initNavigationMenuListeners()
        initViewModelObservers()
    }

    private fun initUI() {
        edtBarcode = binding.edtBarcode
        binding.edtBarcode.apply {
            showSoftInputOnFocus = false
            setOnClickListener { hideSoftKeyboardExt() }
            setOnFocusChangeListener { _, _ -> hideSoftKeyboardExt() }
        }
    }

    private fun initNavigationMenuListeners() {
        binding.navView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_request_remains -> {
                    getRemains()
                    true
                }

                R.id.menu_clean -> {
                    clearFields()
                    true
                }
                else -> false
            }
        }
    }

    private fun initUIListeners() {
        edtBarcode.setOnKeyListener { _, i, keyEvent ->
            if (i == 66 && keyEvent.action == KeyEvent.ACTION_UP) {
                getRemains()
            }
            false
        }
    }

    private fun getRemains() {
        val barcode = edtBarcode.text.toString()
        if (barcode.isNotEmpty()) {
            val listBarcodes: MutableList<String> = mutableListOf(barcode)
            viewModel.loadRemainInfoByBarcodes(listBarcodes)
        } else {
            fancyError { "Не указан штрихкод/код товара!" }
        }
    }

    private fun showShimmer() {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_shimmer, null, false)
        dialogBuilder.setView(dialogView)
            .setCancelable(true)
            .setTitle("Ожидайте")
            .setNegativeButton("Отмена") { dialog, _ ->
                viewModel.cancelCurrentJob()
                dialog.dismiss()
            }
            .setOnDismissListener {
                viewModel.cancelCurrentJob()
            }
        dialog = dialogBuilder.show()
    }

    private fun hideShimmer() {
        dialog.dismiss()
    }

    private fun initViewModelObservers() {

        viewModel.remainsLiveData.observe(viewLifecycleOwner) {
            displayRemains(it)
        }

        viewModel.exceptionMessage.observe(viewLifecycleOwner) {
            fancyException { it }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            fancyError { it }
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            when {
                it -> showShimmer()
                else -> hideShimmer()
            }
        }

        viewModel.idataScanner.observe(viewLifecycleOwner) {
            onReceiveScannerData(it)
        }
    }

    private fun displayRemains(remains: RemainsResponse) {
        when (val item = remains.firstOrNull()) {
            null -> {
                clearFields()
            }
            else -> {
                when (item.found) {
                    true -> {
                        binding.txtRemainsName.text = item.name
                        binding.txtRemainsBarcode.text = item.barcode
                        binding.txtRemainsCode.text = item.code
                        binding.txtRemainsPLU.text = item.plu.toString()
                        binding.txtRemainsPrice.text = item.price.toString()
                        binding.txtRemainsRemain.text = item.remain.toString()
                        binding.txtRemainsDoc.text = item.doc
                        binding.txtRemainsSales.text = item.sales.toString()
                    }
                    false -> {
                        clearFields()
                    }
                }
            }
        }
    }

    private fun moveFocus(view: EditText) {
        view.requestFocus()
        view.setSelection(view.text.length, 0)
    }

    private fun clearFields() {
        edtBarcode.setText("")
        binding.txtRemainsName.text = ""
        binding.txtRemainsBarcode.text = ""
        binding.txtRemainsCode.text = ""
        binding.txtRemainsPLU.text = ""
        binding.txtRemainsPrice.text = ""
        binding.txtRemainsRemain.text = ""
        binding.txtRemainsDoc.text = ""
        binding.txtRemainsSales.text = ""
    }

    private fun onReceiveScannerData(message: String) {
        if (message.isNotEmpty()) {
            clearFields()
            edtBarcode.apply {
                setText(message)
                moveFocus(this)
            }
            getRemains()
        }
    }
}
