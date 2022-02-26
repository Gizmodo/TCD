@file:Suppress("UNUSED_PARAMETER")

package com.shop.tcd.ui

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.shashank.sony.fancytoastlib.FancyToast
import com.shop.tcd.R
import com.shop.tcd.databinding.ActivityCatalogueBinding
import com.shop.tcd.model.Nomenclature
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.repository.main.RepositoryMain
import com.shop.tcd.repository.main.RetrofitServiceMain
import com.shop.tcd.utils.Common
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CatalogueActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCatalogueBinding
    private lateinit var nomenclatureList: ArrayList<NomenclatureItem>
    private lateinit var dateBegin: String
    private lateinit var dateEnd: String

    /** Network **/
    private val retrofit = RetrofitServiceMain.getInstance()
    private val repository = RepositoryMain(retrofit)

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this).apply {
            setTitle("Загрузка...")
            setMessage("Пожалуйста, ожидайте")
        }
    }

    /**
     * Загружает всю номенклатуру. Сразу выполняем запрос к backend.
     */
    fun btnLoadFull(view: View) {
        Timber.d("Загружает всю номенклатуру")
        val response = repository.getAllItems()
        progressDialog.show()

        response.enqueue(object : Callback<Nomenclature> {
            override fun onResponse(call: Call<Nomenclature>, response: Response<Nomenclature>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    if (response.body()?.result.equals("success", false)) {
                        nomenclatureList =
                            response.body()?.nomenclature as ArrayList<NomenclatureItem>
                        FancyToast.makeText(
                            applicationContext,
                            "Загружено объектов ${nomenclatureList.size}",
                            FancyToast.LENGTH_LONG,
                            FancyToast.SUCCESS,
                            false
                        ).show()
                        GlobalScope.launch {
                            Common.saveNomenclatureList(nomenclatureList, applicationContext)
                        }
                    } else {
                        FancyToast.makeText(
                            applicationContext,
                            "Данные не получены: ${response.body()?.message.toString()}",
                            FancyToast.LENGTH_LONG,
                            FancyToast.WARNING,
                            false
                        ).show()
                    }
                } else {
                    FancyToast.makeText(
                        applicationContext,
                        "Код ответа ${response.code()}",
                        FancyToast.LENGTH_LONG,
                        FancyToast.WARNING,
                        false
                    ).show()
                }
            }

            override fun onFailure(call: Call<Nomenclature>, t: Throwable) {
                val errorString = "Запрос не исполнен: ${t.message.toString()}"
                progressDialog.dismiss()
                Timber.e(errorString)
                FancyToast.makeText(
                    applicationContext,
                    errorString,
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    false
                ).show()
            }
        })
    }

    /**
     * Загружает товары по остаткам. Сразу выполняем запрос к backend.
     */
    fun btnLoadRemainders(view: View) {
        Timber.d("Загрузка остатков")
        val response = repository.getRemainders()
        progressDialog.show()
        response.enqueue(object : Callback<Nomenclature> {
            override fun onResponse(call: Call<Nomenclature>, response: Response<Nomenclature>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    if (response.body()?.result.equals("success", false)) {
                        nomenclatureList =
                            response.body()?.nomenclature as ArrayList<NomenclatureItem>
                        FancyToast.makeText(
                            applicationContext,
                            "Загружено объектов ${nomenclatureList.size}",
                            FancyToast.LENGTH_LONG,
                            FancyToast.SUCCESS,
                            false
                        ).show()
                        GlobalScope.launch {
                            Common.saveNomenclatureList(nomenclatureList, applicationContext)
                        }

                    } else {
                        FancyToast.makeText(
                            applicationContext,
                            "Данные не получены: ${response.body()?.message.toString()}",
                            FancyToast.LENGTH_LONG,
                            FancyToast.WARNING,
                            false
                        ).show()
                    }
                } else {
                    FancyToast.makeText(
                        applicationContext,
                        "Код ответа ${response.code()}",
                        FancyToast.LENGTH_LONG,
                        FancyToast.WARNING,
                        false
                    ).show()
                }
            }

            override fun onFailure(call: Call<Nomenclature>, t: Throwable) {
                val errorString = "Запрос не исполнен: ${t.message.toString()}"
                progressDialog.dismiss()
                Timber.e(errorString)
                FancyToast.makeText(
                    applicationContext,
                    errorString,
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    false
                ).show()
            }
        })
    }

    /**
     * Загрузка по группам. Открывается дочернее окно со списком групп.
     */
    fun btnLoadByGroups(view: View) {
        val intent = Intent(this, CatalogueGroupActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, "test")
        }
        startActivity(intent)
    }

    /**
     * Загрузка за период. Запрос на диапазон дат и кнопка Загрузить.
     */
    fun btnLoadByPeriod(view: View) {
        showPeriodDialog()
    }

    private fun showPeriodDialog() {
        val dateDialog = Dialog(this)
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
                    this@CatalogueActivity,
                    "$dateBegin и $dateEnd",
                    FancyToast.LENGTH_LONG,
                    FancyToast.INFO,
                    false
                ).show()
                getByPeriod()
            } else {
                clearText()
                FancyToast.makeText(
                    this@CatalogueActivity,
                    "Диапазон указан не полностью",
                    FancyToast.LENGTH_LONG,
                    FancyToast.WARNING,
                    false
                ).show()
            }
            dateDialog.dismiss()
        }

        edtEnd.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
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
            datePicker.show(supportFragmentManager, datePicker.toString())
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
            datePicker.show(supportFragmentManager, datePicker.toString())
        }
        dateDialog.show()
    }

    private fun getByPeriod() {
        Timber.d("Загрузка за период")
        val filterString = "$dateBegin 0:00:00,$dateEnd 23:59:59"
        val response = repository.getPeriod(filterString)
        progressDialog.show()
        response.enqueue(object : Callback<Nomenclature> {
            override fun onResponse(call: Call<Nomenclature>, response: Response<Nomenclature>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    if (response.body()?.result.equals("success", false)) {
                        nomenclatureList =
                            response.body()?.nomenclature as ArrayList<NomenclatureItem>
                        FancyToast.makeText(
                            applicationContext,
                            "Загружено объектов ${nomenclatureList.size}",
                            FancyToast.LENGTH_LONG,
                            FancyToast.SUCCESS,
                            false
                        ).show()
                        GlobalScope.launch {
                            Common.saveNomenclatureList(nomenclatureList, applicationContext)
                        }
                    } else {
                        FancyToast.makeText(
                            applicationContext,
                            "Данные не получены: ${response.body()?.message.toString()}",
                            FancyToast.LENGTH_LONG,
                            FancyToast.WARNING,
                            false
                        ).show()
                    }
                } else {
                    FancyToast.makeText(
                        applicationContext,
                        "Код ответа ${response.code()}",
                        FancyToast.LENGTH_LONG,
                        FancyToast.WARNING,
                        false
                    ).show()
                }
            }

            override fun onFailure(call: Call<Nomenclature>, t: Throwable) {
                val errorString = "Запрос не исполнен: ${t.message.toString()}"
                progressDialog.dismiss()
                Timber.e(errorString)
                FancyToast.makeText(
                    applicationContext,
                    errorString,
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    false
                ).show()
            }
        })
    }
}