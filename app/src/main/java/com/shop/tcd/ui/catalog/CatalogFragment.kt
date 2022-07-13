package com.shop.tcd.ui.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shop.tcd.R
import com.shop.tcd.core.extension.fancyError
import com.shop.tcd.core.extension.fancyException
import com.shop.tcd.core.extension.getViewModel
import com.shop.tcd.core.extension.navigateExt
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.core.utils.ShimmerState
import com.shop.tcd.databinding.FragmentCatalogBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class CatalogFragment : Fragment(R.layout.fragment_catalog) {
    private val binding by viewBindingWithBinder(FragmentCatalogBinding::bind)
    private lateinit var btnLoadFull: Button
    private lateinit var btnLoadRemainders: Button
    private lateinit var btnLoadByGroups: Button
    private lateinit var btnLoadByPeriod: Button
    private lateinit var btnRemains: Button
    private val viewModel: CatalogViewModel by lazy { getViewModel { CatalogViewModel() } }
    private lateinit var dateBegin: String
    private lateinit var dateEnd: String
    private lateinit var txtState: TextView
    private lateinit var dialogView: View
    private lateinit var dialogBuilder: MaterialAlertDialogBuilder
    private lateinit var dialog: AlertDialog
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initViewModelObservers()
    }

    private fun showShimmer() {
        dialogBuilder = MaterialAlertDialogBuilder(requireContext())
        dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_shimmer, null, false)
        txtState = dialogView.findViewById(R.id.txtState)
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
        viewModel.state.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach {
                when (it) {
                    is ShimmerState.Loading -> {
                        showShimmer()
                    }
                    ShimmerState.Finishing -> {
                        hideShimmer()
                    }
                    ShimmerState.Empty -> {}
                    is ShimmerState.State -> {
                        txtState.text = it.result
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.exceptionMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            fancyException { it }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            fancyError { it }
        }
    }

    private fun initUI() {
        btnLoadFull = binding.btnLoadFull
        btnLoadRemainders = binding.btnLoadRemainders
        btnLoadByGroups = binding.btnLoadByGroups
        btnLoadByPeriod = binding.btnLoadByPeriod
        btnRemains = binding.btnRemains

        btnLoadByGroups.setOnClickListener {
            navigateExt(CatalogFragmentDirections.actionCatalogFragmentToGroupFragment())
        }
        btnLoadFull.setOnClickListener {
            viewModel.loadNomenclatureFull()
        }
        btnLoadRemainders.setOnClickListener {
            viewModel.loadNomenclatureRemainders()
        }
        btnLoadByPeriod.setOnClickListener {
            showPeriodDialog()
        }
        btnRemains.setOnClickListener {
            navigateExt(CatalogFragmentDirections.actionCatalogFragmentToRemainsFragment())
        }
    }

    private fun showPeriodDialog() {
        val dialogPeriodBuilder = MaterialAlertDialogBuilder(requireContext())
        val dialogPeriodView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_period, null, false)
        val edtBegin = dialogPeriodView.findViewById<EditText>(R.id.edtDateBegin)
        val edtEnd = dialogPeriodView.findViewById<EditText>(R.id.edtDateEnd)

        fun clearFields() {
            dateBegin = ""
            dateEnd = ""
            edtBegin.setText("")
            edtEnd.setText("")
        }

        dialogPeriodBuilder.setView(dialogPeriodView)
            .setCancelable(true)
            .setTitle(R.string.period_select)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                clearFields()
                dialog.dismiss()
            }
            .setPositiveButton(R.string.ok) { dialog, _ ->
                if (dateBegin.isNotEmpty() && dateEnd.isNotEmpty()) {
                    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyy")
                    val firstDate: LocalDate = LocalDate.parse(dateBegin, formatter)
                    val secondDate: LocalDate = LocalDate.parse(dateEnd, formatter)
                    var period = ""
                    when {
                        firstDate.isAfter(secondDate) -> {
                            period = "$dateEnd 0:00:00,$dateBegin 23:59:59"
                        }
                        firstDate.isBefore(secondDate) -> {
                            period = "$dateBegin 0:00:00,$dateEnd 23:59:59"
                        }
                        firstDate.isEqual(secondDate) -> {
                            period = "$dateBegin 0:00:00,$dateEnd 23:59:59"
                        }
                    }
                    viewModel.loadNomenclatureByPeriod(period)
                } else {
                    clearFields()
                    fancyError { "Диапазон указан не полностью" }
                }
                dialog.dismiss()
            }

        edtBegin.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                edtBegin.callOnClick()
            }
        }

        edtEnd.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                edtEnd.callOnClick()
            }
        }

        edtBegin.setOnClickListener {
            val currentTimeInMillis = Calendar.getInstance().timeInMillis
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Начальная дата")
                .setSelection(currentTimeInMillis).build()

            datePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.time = Date(it)
                val format = SimpleDateFormat("dd.MM.yyyy", Locale("ru-RU"))
                dateBegin = format.format(calendar.time)
                edtBegin.setText(dateBegin)
            }

            datePicker.show(requireActivity().supportFragmentManager, datePicker.toString())
        }

        edtEnd.setOnClickListener {
            val currentTimeInMillis = Calendar.getInstance().timeInMillis
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Конечная дата")
                .setSelection(currentTimeInMillis).build()

            datePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.time = Date(it)
                val format = SimpleDateFormat("dd.MM.yyyy", Locale("ru-RU"))
                dateEnd = format.format(calendar.time)
                edtEnd.setText(dateEnd)
            }
            datePicker.show(requireActivity().supportFragmentManager, datePicker.toString())
        }
        dialogPeriodBuilder.show()
    }
}
