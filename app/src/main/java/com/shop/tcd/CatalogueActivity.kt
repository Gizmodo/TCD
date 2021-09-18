@file:Suppress("UNUSED_PARAMETER")

package com.shop.tcd

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import androidx.core.util.component1
import androidx.core.util.component2
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialDatePicker.INPUT_MODE_CALENDAR
import com.shop.tcd.databinding.ActivityCatalogueBinding
import com.shop.tcd.model.Nomenclature
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.repo.MainRepository
import com.shop.tcd.retro.RetrofitService
import com.shop.tcd.room.dao.NomenclatureDao
import com.shop.tcd.room.database.TCDRoomDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class CatalogueActivity : AppCompatActivity() {
    val TAG = this::class.simpleName

    private lateinit var binding: ActivityCatalogueBinding
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var nomenclatureList: ArrayList<NomenclatureItem>
    private lateinit var databaseTCD: TCDRoomDatabase
    private lateinit var nomDao: NomenclatureDao
    private lateinit var dateBegin: String
    private lateinit var dateEnd: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogueBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /**
     * Загружает всю номенклатуру. Сразу выполняем запрос к backend.
     */
    fun btnLoadFull(view: View) {
        Log.d(TAG, "Загрузка остатков")
        val repository = MainRepository(retrofitService)
        val response = repository.getAllItems()

        response.enqueue(object : Callback<Nomenclature> {
            override fun onResponse(call: Call<Nomenclature>, response: Response<Nomenclature>) {
                Log.d(TAG, "Код ответа: ${response.code()}")
                Log.d(TAG,
                    "Время ответа: ${response.raw().receivedResponseAtMillis - response.raw().sentRequestAtMillis} ms")
                if (response.isSuccessful) {
                    if (response.body()?.result.equals("success", false)) {
                        Log.d(TAG, "Данные получены")
                        nomenclatureList =
                            response.body()?.nomenclature as ArrayList<NomenclatureItem>
                        //   newRecyclerView.adapter = MyAdapterBinding(nomenclatureList)
//                languageList = response.body()?.group as ArrayList<Group>
                        Log.d(TAG, "onResponse nomenclatureList.size=${nomenclatureList.size}")
                        Log.d(TAG, "onResponse: ${response.body()!!.nomenclature[2].name}")
                        Toast.makeText(applicationContext,
                            "onResponse nomenclatureList.size=${nomenclatureList.size}",
                            Toast.LENGTH_SHORT).show()
                        Toast.makeText(applicationContext, "Запрос выполнен", Toast.LENGTH_SHORT)
                            .show()
                        //TODO: Сделать
                        //  saveNomenclature(nomenclatureList)
                    } else {
                        Log.d(TAG, "Данные не получены: ${response.body()?.message.toString()}")
                    }
                } else {
                    Log.d(TAG, "Ошибка сервера")
                }
            }

            override fun onFailure(call: Call<Nomenclature>, t: Throwable) {
                val errorString = "Запрос не исполнен: ${t.message.toString()}"
                Log.e(TAG, errorString)
                Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Загружает товары по остаткам. Сразу выполняем запрос к backend.
     */
    fun btnLoadRemainders(view: View) {
        Log.d(TAG, "Загрузка остатков")
        val repository = MainRepository(retrofitService)
        val response = repository.getRemainders()

        response.enqueue(object : Callback<Nomenclature> {
            override fun onResponse(call: Call<Nomenclature>, response: Response<Nomenclature>) {
                Log.d(TAG, "Код ответа: ${response.code()}")
                Log.d(TAG,
                    "Время ответа: ${response.raw().receivedResponseAtMillis - response.raw().sentRequestAtMillis} ms")
                if (response.isSuccessful) {
                    if (response.body()?.result.equals("success", false)) {
                        Log.d(TAG, "Данные получены")
                        nomenclatureList =
                            response.body()?.nomenclature as ArrayList<NomenclatureItem>
                        //   newRecyclerView.adapter = MyAdapterBinding(nomenclatureList)
//                languageList = response.body()?.group as ArrayList<Group>
                        Log.d(TAG, "onResponse nomenclatureList.size=${nomenclatureList.size}")
                        Log.d(TAG, "onResponse: ${response.body()!!.nomenclature[2].name}")
                        Toast.makeText(applicationContext,
                            "onResponse nomenclatureList.size=${nomenclatureList.size}",
                            Toast.LENGTH_SHORT).show()
                        Toast.makeText(applicationContext, "Запрос выполнен", Toast.LENGTH_SHORT)
                            .show()
                        //TODO: Сделать
                        //  saveNomenclature(nomenclatureList)
                    } else {
                        Log.d(TAG, "Данные не получены: ${response.body()?.message.toString()}")
                    }
                } else {
                    Log.d(TAG, "Ошибка сервера")
                }
            }

            override fun onFailure(call: Call<Nomenclature>, t: Throwable) {
                val errorString = "Запрос не исполнен: ${t.message.toString()}"
                Log.e(TAG, errorString)
                Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
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
        showCustomDialog()
    }

    private fun showCustomDialog() {
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
                //TODO Make backend request

                Toast.makeText(this@CatalogueActivity, "$dateBegin и $dateEnd", Toast.LENGTH_SHORT)
                    .show()
                getByPeriod()
            } else {
                clearText()
                Toast.makeText(this@CatalogueActivity,
                    "Диапазон указан не полностью",
                    Toast.LENGTH_LONG)
                    .show()
            }
            dateDialog.dismiss()
        }

        edtEnd.onFocusChangeListener = object : OnFocusChangeListener {
            override fun onFocusChange(view: View, hasFocus: Boolean) {
                if (hasFocus) {
                    edtEnd.callOnClick()
                }
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
        dateDialog.setOnCancelListener {
            //TODO: send http request
        }
        dateDialog.show()
    }

    /**
     * Сохранение полученного ответа по номенклатуре в БД.
     */
    fun saveNomenclature(list: List<NomenclatureItem>) {
        databaseTCD = TCDRoomDatabase.getDatabase(application)
        nomDao = databaseTCD.nomDao()
        GlobalScope.launch {
            nomDao.insertNomenclature(list)
            Log.d(TAG, "Coroutine inside")
        }
        Log.d(TAG, "Coroutine outside")
    }

    private fun getByPeriod() {
        Log.d(TAG, "Загрузка за период")
        val repository = MainRepository(retrofitService)
        val filterString = dateBegin + "  0:00:00," + dateEnd + " 23:59:59"
        val response = repository.getPeriod(filterString)

        response.enqueue(object : Callback<Nomenclature> {
            override fun onResponse(call: Call<Nomenclature>, response: Response<Nomenclature>) {
                Log.d(TAG, "Код ответа: ${response.code()}")
                Log.d(TAG,
                    "Время ответа: ${response.raw().receivedResponseAtMillis - response.raw().sentRequestAtMillis} ms")
                if (response.isSuccessful) {
                    if (response.body()?.result.equals("success", false)) {
                        Log.d(TAG, "Данные получены")
                        nomenclatureList =
                            response.body()?.nomenclature as ArrayList<NomenclatureItem>
                        //   newRecyclerView.adapter = MyAdapterBinding(nomenclatureList)
//                languageList = response.body()?.group as ArrayList<Group>
                        Log.d(TAG, "onResponse nomenclatureList.size=${nomenclatureList.size}")
                        Log.d(TAG, "onResponse: ${response.body()!!.nomenclature[2].name}")
                        Toast.makeText(applicationContext,
                            "onResponse nomenclatureList.size=${nomenclatureList.size}",
                            Toast.LENGTH_SHORT).show()
                        Toast.makeText(applicationContext, "Запрос выполнен", Toast.LENGTH_SHORT)
                            .show()
                        //TODO: Сделать
                        //  saveNomenclature(nomenclatureList)
                    } else {
                        Log.d(TAG, "Данные не получены: ${response.body()?.message.toString()}")
                    }
                } else {
                    Log.d(TAG, "Ошибка сервера")
                }
            }

            override fun onFailure(call: Call<Nomenclature>, t: Throwable) {
                val errorString = "Запрос не исполнен: ${t.message.toString()}"
                Log.e(TAG, errorString)
                Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
            }
        })
    }
}