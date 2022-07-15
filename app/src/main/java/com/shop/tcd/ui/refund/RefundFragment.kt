package com.shop.tcd.ui.refund

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shop.tcd.R
import com.shop.tcd.core.extension.fancyError
import com.shop.tcd.core.extension.fancyException
import com.shop.tcd.core.extension.getViewModel
import com.shop.tcd.core.extension.hideSoftKeyboardExt
import com.shop.tcd.core.extension.setTint
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.core.utils.StatefulData
import com.shop.tcd.data.dto.datamatrix.Goods
import com.shop.tcd.databinding.FragmentRefundBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class RefundFragment : Fragment(R.layout.fragment_refund) {
    private lateinit var dialog: AlertDialog
    private val binding by viewBindingWithBinder(FragmentRefundBinding::bind)
    private val viewModel: RefundViewModel by lazy {
        getViewModel { RefundViewModel() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initViewModelObservers()
    }

    private fun initUI() {
        binding.edtBarcode.apply {
            showSoftInputOnFocus = false
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
        }
        binding.edtDataMatrix.apply {
            showSoftInputOnFocus = false
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
        }
        binding.edtPdf417.apply {
            showSoftInputOnFocus = false
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
        }
        binding.btnClear.setOnClickListener {
            clearFields()
        }
        binding.btnSend.setOnClickListener {
            viewModel.send()
        }
    }

    private fun hideKeyboard() {
        this.hideSoftKeyboardExt()
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
        if (this::dialog.isInitialized) {
            dialog.dismiss()
        }
    }

    private fun initViewModelObservers() {
        viewModel.idataScanner.observe(viewLifecycleOwner) {
            //  onReceiveScannerData(it)
        }
        viewModel.datamatrix.observe(viewLifecycleOwner) {
            onReceiveDataMatrix(it)
        }
        viewModel.barcode.observe(viewLifecycleOwner) {
            onReceiveBarcode(it)
        }
        viewModel.pdf417.observe(viewLifecycleOwner) {
            onReceivePDF417(it)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest {
                    when (it) {
                        is StatefulData.Error -> {
                            hideShimmer()
                            fancyError { it.msg }
                        }
                        StatefulData.Loading -> {
                            Timber.d("Запрос на получение списания/возврат")
                            showShimmer()
                        }
                        is StatefulData.Notify -> {
                            hideShimmer()
                            fancyException { it.msg }
                        }
                        is StatefulData.Success -> {
                            hideShimmer()
                            binding.txtScanResult.text = it.result
                        }
                        StatefulData.Empty -> {}
                    }
                }
            }
        }
    }

    private fun setGreen(view: ImageView) {
        view.setTint(R.color.md_theme_dark_inversePrimary)
    }

    private fun setRed(view: ImageView) {
        view.setTint(R.color.md_theme_light_error)
    }

    private fun onReceivePDF417(code: String?) {
        setGreen(binding.imgPdf417)
        binding.edtPdf417.setText(code)
        binding.txtScanResult.text = code
    }

    private fun onReceiveBarcode(code: String?) {
        setGreen(binding.imgBarcode)
        binding.edtBarcode.setText(code)
        binding.txtScanResult.text = code
    }

    private fun moveFocus(view: EditText) {
        view.requestFocus()
        view.setSelection(view.text.length, 0)
    }

    private fun clearFields() {
        binding.edtDataMatrix.setText("")
        binding.edtBarcode.setText("")
        binding.edtPdf417.setText("")
        binding.txtScanResult.text = ""
        setRed(binding.imgDataMatrix)
        setRed(binding.imgBarcode)
        setRed(binding.imgPdf417)
    }

    private fun onReceiveDataMatrix(good: Goods) {
        setGreen(binding.imgDataMatrix)
        binding.txtScanResult.text = good.toString()
        binding.edtDataMatrix.setText(good.code)
        moveFocus(binding.edtDataMatrix)
    }
}
