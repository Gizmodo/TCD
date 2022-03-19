package com.shop.tcd.v2.screen.catalog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.shashank.sony.fancytoastlib.FancyToast
import com.shop.tcd.R
import com.shop.tcd.databinding.FragmentCatalogBinding
import com.shop.tcd.v2.core.extension.getViewModel
import com.shop.tcd.v2.core.extension.navigateExt
import com.shop.tcd.v2.core.extension.viewBindingWithBinder
import java.text.SimpleDateFormat
import java.util.*

class CatalogFragment : Fragment(R.layout.fragment_catalog) {
    private val binding by viewBindingWithBinder(FragmentCatalogBinding::bind)
    private lateinit var btnLoadFull: Button
    private lateinit var btnLoadRemainders: Button
    private lateinit var btnLoadByGroups: Button
    private lateinit var btnLoadByPeriod: Button
    private val viewModel: CatalogViewModel by lazy {
        getViewModel { CatalogViewModel() }
    }
    private lateinit var dateBegin: String
    private lateinit var dateEnd: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
//        setStateUI(enabled = false)
//        initViewModelObservers()
    }

    private fun initUI() {
        btnLoadFull = binding.btnLoadFull
        btnLoadRemainders = binding.btnLoadRemainders
        btnLoadByGroups = binding.btnLoadByGroups
        btnLoadByPeriod = binding.btnLoadByPeriod

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
            // TODO: Сделать показ диалога с датами
            showPeriodDialog()
        }
    }

    private fun showPeriodDialog() {
        val dateDialog = Dialog(requireContext())
        dateDialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)
        dateDialog.setCancelable(true)
        dateDialog.setContentView(R.layout.custom_dialog)

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
                FancyToast.makeText(
                    requireContext(),
                    "$dateBegin и $dateEnd",
                    FancyToast.LENGTH_LONG,
                    FancyToast.INFO,
                    false
                ).show()
                val period = "$dateBegin 0:00:00,$dateEnd 23:59:59"
                viewModel.loadNomenclatureByPeriod(period)
            } else {
                clearText()
                FancyToast.makeText(
                    requireContext(),
                    "Диапазон указан не полностью",
                    FancyToast.LENGTH_LONG,
                    FancyToast.WARNING,
                    false
                ).show()
            }
            dateDialog.dismiss()
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