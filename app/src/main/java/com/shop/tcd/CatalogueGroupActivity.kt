package com.shop.tcd

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.Common.Common.saveNomenclatureList
import com.shop.tcd.databinding.ActivityCatalogueGroupBinding
import com.shop.tcd.model.Group
import com.shop.tcd.model.Groups
import com.shop.tcd.model.Nomenclature
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.repo.MainRepository
import com.shop.tcd.retro.RetrofitService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CatalogueGroupActivity : AppCompatActivity() {
    val tag = this::class.simpleName

    private lateinit var binding: ActivityCatalogueGroupBinding
    private lateinit var rv: RecyclerView
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var groupsList: ArrayList<Group>
    private lateinit var nomenclatureList: ArrayList<NomenclatureItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogueGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        getGroups()
    }

    fun btnCatalogGroupLoadSelected(view: View) {
        val filterString = groupsList
            .filter { it.checked }.joinToString { it.code }
        Log.d(tag, filterString)

        val repository = MainRepository(retrofitService)
        val response = repository.getByGroup(filterString)

        response.enqueue(object : Callback<Nomenclature> {
            override fun onFailure(call: Call<Nomenclature>, t: Throwable) {
                val errorString = "Запрос не исполнен: ${t.message.toString()}"
                Log.e(tag, errorString)
                Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<Nomenclature>, response: Response<Nomenclature>,
            ) {
                Log.d(tag, "Код ответа: ${response.code()}")
                Log.d(tag,
                    "Время ответа: ${response.raw().receivedResponseAtMillis - response.raw().sentRequestAtMillis} ms")
                if (response.isSuccessful) {
                    if (response.body()?.result.equals("success", false)) {
                        Log.d(tag, "Данные получены")
                        nomenclatureList =
                            response.body()?.nomenclature as java.util.ArrayList<NomenclatureItem>
                        Log.d(tag, "onResponse nomenclatureList.size=${nomenclatureList.size}")
                        Log.d(tag, "onResponse: ${response.body()!!.nomenclature[2].name}")
                        Toast.makeText(applicationContext,
                            "onResponse nomenclatureList.size=${nomenclatureList.size}",
                            Toast.LENGTH_SHORT).show()
                        Toast.makeText(applicationContext, "Запрос выполнен", Toast.LENGTH_SHORT)
                            .show()
                        GlobalScope.launch {
                            saveNomenclatureList(nomenclatureList, applicationContext)
                        }
                    } else {
                        Log.d(tag, "Данные не получены: ${response.body()?.message.toString()}")
                    }
                } else {
                    Log.d(tag, "Ошибка сервера")
                }
            }
        })
    }

    private fun getGroups() {
        val repository = MainRepository(retrofitService)
        val response = repository.getAllGroups()
        response.enqueue(object : Callback<Groups> {
            override fun onFailure(call: Call<Groups>, t: Throwable) {
                Toast.makeText(applicationContext, "Запрос не выполнен", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Groups>, response: Response<Groups>) {
                groupsList = response.body()?.group as ArrayList<Group>
                rv.adapter = GroupAdapter(groupsList)
                Log.d(tag, "onResponse groupList.size=${groupsList.size}")
                Log.d(tag, "onResponse: ${response.body()!!.group[2].name}")
                Toast.makeText(applicationContext, "Запрос выполнен", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initRecyclerView() {
        rv = binding.rvCatalogueGroup
        rv.layoutManager = LinearLayoutManager(this)
        rv.setHasFixedSize(true)
    }
}
