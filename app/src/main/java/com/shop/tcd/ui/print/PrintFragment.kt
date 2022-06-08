package com.shop.tcd.ui.print

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.shop.tcd.ExampleWorker
import com.shop.tcd.R
import com.shop.tcd.core.extension.*
import com.shop.tcd.core.utils.Constants.SelectedObjects.PrinterModel
import com.shop.tcd.core.utils.Constants.SelectedObjects.PrinterModelPosition
import com.shop.tcd.core.utils.Constants.SelectedObjects.isPrinterSelected
import com.shop.tcd.core.utils.TcpClientService
import com.shop.tcd.data.dto.printer.PrintersList
import com.shop.tcd.databinding.FragmentPrintBinding
import com.shop.tcd.ui.print.adapter.PriceTagAdapter

class PrintFragment : Fragment(R.layout.fragment_print) {
    /*  val vm = getViewModel<CatchViewModel>()
      val vm1: CatchViewModel = getViewModel()
      val activityScopedVm = activity?.getViewModel<CatchViewModel>()
      val activityScopedVm2 = activity?.getViewModel { CatchViewModel().apply { init(stuff) } }*/
    private val binding by viewBindingWithBinder(FragmentPrintBinding::bind)
    private lateinit var edtBarcode: TextInputEditText
    private lateinit var rvPriceTags: RecyclerView
    private lateinit var printersList: PrintersList
    private lateinit var adapter: PriceTagAdapter
    private lateinit var dialog: AlertDialog
    val list: MutableList<String> = mutableListOf()
    private val viewModel: PrintViewModel by lazy {
        getViewModel { PrintViewModel() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initUIListeners()
        initNavigationMenuListeners()
        initRecyclerView()
        restoreSelectedPrinter()
        initViewModelObservers()
    }

    private fun initUIListeners() {
        edtBarcode.setOnKeyListener { view, i, keyEvent ->
            if (i == 66 && keyEvent.action == KeyEvent.ACTION_UP) {
                addBarcode()
            }
            false
        }
    }

    private fun initRecyclerView() {
        adapter = PriceTagAdapter(list)
        rvPriceTags.adapter = adapter
        rvPriceTags.setHasFixedSize(true)
        rvPriceTags.layoutManager = LinearLayoutManager(requireContext())
        rvPriceTags.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayout.VERTICAL
            )
        )
    }

    private fun restoreSelectedPrinter() {
        if (PrinterModelPosition != -1) {
            binding.edtPrinter.setText(PrinterModel.name)
        }
    }

    private fun initUI() {
        edtBarcode = binding.edtBarcode
        rvPriceTags = binding.rvPriceTags
        rvPriceTags.isFocusable = false
        binding.edtBarcode.apply {
            showSoftInputOnFocus = false
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
        }
    }

    private fun hideKeyboard() {
        this.hideSoftKeyboardExt()
    }

    private fun initNavigationMenuListeners() {
        binding.navView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_add -> {
                    addBarcode()
                    true
                }
                R.id.menu_print -> {
                    if (list.isNotEmpty()) {
                        viewModel.loadPrintInfoByBarcodes(list)
                    } else {
                        fancyError { "Список для печати пустой!" }
                    }
                    true
                }

                R.id.menu_clean -> {
                    list.removeAll(list)
                    adapter.notifyDataSetChanged()
                    true
                }
                else -> false
            }
        }
    }

    private fun addBarcode() {
        val inputString = edtBarcode.text.toString()
        if (inputString.isNotEmpty()) {
            list.add(0, inputString)
            adapter.notifyItemInserted(0)
            clearFields()
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
        viewModel.printersLiveData.observe(viewLifecycleOwner) {
            printersList = it
            setupAutoComplete(binding.edtPrinter, it)
        }

        viewModel.printerPayloadLiveData.observe(viewLifecycleOwner) {
            runService(it)
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
            if (edtBarcode.isFocused) {
                moveFocus(edtBarcode)
                addBarcode()
            }
        }

        viewModel.urovoScanner.observe(viewLifecycleOwner) {
            onReceiveScannerData(it)
        }

        viewModel.idataScanner.observe(viewLifecycleOwner) {
            onReceiveScannerData(it)
        }
    }

    private fun moveFocus(view: EditText) {
        view.requestFocus()
        view.setSelection(view.text.length, 0)
    }

    private fun clearFields() {
        edtBarcode.setText("")
    }

    private fun onReceiveScannerData(message: String) {
        if (message.isNotEmpty()) {
            clearFields()
            edtBarcode.apply {
                setText(message)
                moveFocus(this)
            }
            addBarcode()
        }
    }

    private fun runService(list: List<String>) {
        if (isPrinterSelected()) {
            val intent = Intent(requireContext(), TcpClientService::class.java)
            val payload = list.joinToString(separator = "")
            intent.putExtra("payload", payload)
            intent.putExtra("ip", PrinterModel.ip)
            requireContext().stopService(intent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }
        } else {
            fancyError { "Не выбран принтер!" }
        }
    }

    fun runHeavyWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val heavyWorkRequest: WorkRequest =
            OneTimeWorkRequest.Builder(ExampleWorker::class.java)
                .setConstraints(constraints)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

        WorkManager
            .getInstance(requireContext())
            .enqueue(heavyWorkRequest)
    }

    private fun setupAutoComplete(view: AutoCompleteTextView, items: PrintersList) {
        val printersIP: AbstractList<String?> = object : AbstractList<String?>() {
            override fun get(index: Int): String {
                return items[index].name
            }

            override val size: Int
                get() = items.size
        }
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, printersIP)
        view.setAdapter(adapter)

        view.setOnItemClickListener { _, _, position, _ ->
            PrinterModel = items[position]
            PrinterModelPosition = position
        }
        if (PrinterModelPosition != -1) {
            view.setText(adapter.getItem(PrinterModelPosition), false)
        }
    }
}
