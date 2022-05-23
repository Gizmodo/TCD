package com.shop.tcd.components

import android.os.Bundle
import android.view.View
import androidx.compose.material.MaterialTheme
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.shop.tcd.R
import com.shop.tcd.core.extension.getViewModel
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.databinding.FragmentPlantDetailBinding
import com.shop.tcd.ui.remains.RemainsViewModel

class RemainsFragmentCompose : Fragment(R.layout.fragment_plant_detail) {
    private val binding by viewBindingWithBinder(FragmentPlantDetailBinding::bind)
    private lateinit var edtBarcode: TextInputEditText
    private lateinit var shimmer: ConstraintLayout

    private val viewModel: RemainsViewModel by lazy {
        getViewModel { RemainsViewModel() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeView.setContent {
            MaterialTheme {
                PlantDetailDescription()
            }
        }
        /*  initUI()
          initUIListeners()
          initNavigationMenuListeners()
          initViewModelObservers()*/
    }
/*
    private fun initUI() {
        shimmer = binding.shimmer
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
        shimmer.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        shimmer.visibility = View.GONE
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
        viewModel.urovoKeyboard.observe(viewLifecycleOwner) {
            if (it) {
                getRemains()
            }
        }

        viewModel.urovoScanner.observe(viewLifecycleOwner) {
            onReceiveScannerData(it)
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
    }*/
}