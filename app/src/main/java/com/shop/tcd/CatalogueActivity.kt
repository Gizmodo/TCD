@file:Suppress("UNUSED_PARAMETER")

package com.shop.tcd

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

class CatalogueActivity : AppCompatActivity() {
    val TAG = this::class.simpleName

    private lateinit var binding: ActivityCatalogueBinding
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var nomenclatureList: ArrayList<NomenclatureItem>
    private lateinit var databaseTCD: TCDRoomDatabase
    private lateinit var nomDao: NomenclatureDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogueBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /**
     * Загружает всю номенклатуру. Сразу выполняем запрос к backend.
     */
    fun btnLoadFull(view: View) {
        val repository = MainRepository(retrofitService)
        val response = repository.getAllItems()

        response.enqueue(object : Callback<Nomenclature> {
            override fun onResponse(call: Call<Nomenclature>, response: Response<Nomenclature>) {
                Log.d(TAG, "Response good")
                nomenclatureList = response.body()?.nomenclature as ArrayList<NomenclatureItem>
                //   newRecyclerView.adapter = MyAdapterBinding(nomenclatureList)
//                languageList = response.body()?.group as ArrayList<Group>
                Log.d(TAG, "onResponse nomenclatureList.size=${nomenclatureList.size}")
                Log.d(TAG, "onResponse: ${response.body()!!.nomenclature[2].name}")
                Toast.makeText(applicationContext,
                    "onResponse nomenclatureList.size=${nomenclatureList.size}",
                    Toast.LENGTH_SHORT).show()
                Toast.makeText(applicationContext, "Запрос выполнен", Toast.LENGTH_SHORT).show()
                val tx: Long = response.raw().sentRequestAtMillis
                val rx: Long = response.raw().receivedResponseAtMillis
                Log.d(TAG, "response time : ${rx - tx} ms")
                saveNomenclature(nomenclatureList)
            }

            override fun onFailure(call: Call<Nomenclature>, t: Throwable) {
                Log.d(TAG, "Response bad")
                Log.e(TAG, t.message.toString())
                Toast.makeText(applicationContext, "Запрос не выполнен", Toast.LENGTH_SHORT).show()
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
                        Log.d(TAG, "Данные не получены: $response.body()?.message")
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

    }

    fun saveNomenclature(list: List<NomenclatureItem>) {
        databaseTCD = TCDRoomDatabase.getDatabase(application)
        nomDao = databaseTCD.nomDao()
        GlobalScope.launch {
            nomDao.insertNomenclature(list)
            Log.d(TAG, "Coroutine inside")
        }
        Log.d(TAG, "Coroutine outside")
    }
}