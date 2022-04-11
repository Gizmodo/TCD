package com.shop.tcd.screen.catalog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.shop.tcd.R
import com.shop.tcd.core.extension.getViewModel
import com.shop.tcd.core.extension.longFancy
import com.shop.tcd.core.extension.navigateExt
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.databinding.FragmentCatalogBinding
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
    private lateinit var shimmer: ConstraintLayout
    private val viewModel: CatalogViewModel by lazy { getViewModel { CatalogViewModel() } }
    private lateinit var dateBegin: String
    private lateinit var dateEnd: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
//        setStateUI(enabled = false)
        initViewModelObservers()
    }

    private fun showShimmer() {
        shimmer.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        shimmer.visibility = View.GONE
    }

    private fun initViewModelObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            longFancy { it }
            Timber.e(it)
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

    private fun initUI() {
        btnLoadFull = binding.btnLoadFull
        btnLoadRemainders = binding.btnLoadRemainders
        btnLoadByGroups = binding.btnLoadByGroups
        btnLoadByPeriod = binding.btnLoadByPeriod
        shimmer = binding.shimmer

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
    }

    private fun showPeriodDialog() {
        val dateDialog = Dialog(requireContext())
        dateDialog.setCancelable(true)
        dateDialog.setContentView(R.layout.dialog_period)

        val btnOk = dateDialog.findViewById<Button>(R.id.btnDialogOk)
        val btnCancel = dateDialog.findViewById<Button>(R.id.btnDialogCancel)
        val edtBegin = dateDialog.findViewById<EditText>(R.id.edtDateBegin)
        val edtEnd = dateDialog.findViewById<EditText>(R.id.edtDateEnd)

        fun clearText() {
            dateBegin = ""
            dateEnd = ""
            edtBegin.setText("")
            edtEnd.setText("")
        }

        btnCancel.setOnClickListener {
            clearText()
            dateDialog.dismiss()
        }

        btnOk.setOnClickListener {
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
                clearText()
                longFancy { "Диапазон указан не полностью" }
            }
            dateDialog.dismiss()
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
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Начальная дата")
                .setSelection(currentTimeInMillis)
                .build()

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
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Конечная дата")
                .setSelection(currentTimeInMillis)
                .build()

            datePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.time = Date(it)
                val format = SimpleDateFormat("dd.MM.yyyy", Locale("ru-RU"))
                dateEnd = format.format(calendar.time)
                edtEnd.setText(dateEnd)
            }
            datePicker.show(requireActivity().supportFragmentManager, datePicker.toString())
        }
        dateDialog.show()
    }
}