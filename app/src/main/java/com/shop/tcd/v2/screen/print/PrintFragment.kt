package com.shop.tcd.v2.screen.print

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.work.*
import com.shashank.sony.fancytoastlib.FancyToast
import com.shop.tcd.databinding.FragmentPrintBinding
import com.shop.tcd.utils.Common
import com.shop.tcd.v2.data.printer.PrintersList
import com.shop.tcd.TcpClientService
import com.shop.tcd.v2.ExampleWorker
import com.shop.tcd.v2.utils.extension.getViewModel
import timber.log.Timber

class PrintFragment : Fragment() {
    /*  val vm = getViewModel<CatchViewModel>()
      val vm1: CatchViewModel = getViewModel()
      val activityScopedVm = activity?.getViewModel<CatchViewModel>()
      val activityScopedVm2 = activity?.getViewModel { CatchViewModel().apply { init(stuff) } }*/
    private lateinit var binding: FragmentPrintBinding
    private lateinit var btnPrint: Button
    private lateinit var shimmer: ConstraintLayout
    private lateinit var printersList: PrintersList
    private val viewModel: PrintViewModel by lazy {
        getViewModel { PrintViewModel() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPrintBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        setStateUI(enabled = false)
        restoreSelectedPrinter()
        initViewModelObservers()
        viewModel.loadPrinters()
    }

    private fun restoreSelectedPrinter() {
        if (Common.selectedPrinterPosition != -1) {
            binding.edtPrinter.setText(Common.selectedPrinter.ip)
            setStateUI(enabled = true)
        }
    }

    private fun initUI() {
        btnPrint = binding.btnPrint
        shimmer = binding.shimmer
        initUIListeners()
    }

    private fun initUIListeners() {
        btnPrint.setOnClickListener {
            runService()
        }
    }

    private fun showShimmer() {
        shimmer.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        shimmer.visibility = View.GONE
    }

    private fun setStateUI(enabled: Boolean) {
        btnPrint.isEnabled = enabled
    }

    private fun initViewModelObservers() {
// TODO: Двойной вызов!
        viewModel.printersLiveData.observe(viewLifecycleOwner) {
            Timber.d(it.toString())
            printersList = it
            setupAutoComplete(binding.edtPrinter, it)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            FancyToast.makeText(
                activity?.applicationContext,
                it,
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                false
            ).show()
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            when {
                it -> {
                    showShimmer()
                }
                else -> {
                    hideShimmer()
                }
            }
        }
    }

    private fun getRandomString(length: Int): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    private fun runService() {
        Timber.d("Print button clicked")
        val intent = Intent(requireContext(), TcpClientService::class.java)
        intent.putExtra("payload", getRandomString(15))
        intent.putExtra("ip", Common.selectedPrinter.ip)
        requireContext().stopService(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
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
                return items.printers[index].ip
            }

            override val size: Int
                get() = items.printers.size
        }
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, printersIP)
        view.setAdapter(adapter)

        view.setOnItemClickListener { _, _, position, _ ->
            Common.selectedPrinter = items.printers[position]
            Common.selectedPrinterPosition = position
            setStateUI(enabled = true)
        }
        if (Common.selectedPrinterPosition != -1) {
            view.setText(adapter.getItem(Common.selectedPrinterPosition), false)
        }
    }
}