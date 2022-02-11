package com.shop.tcd

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shashank.sony.fancytoastlib.FancyToast
import com.shop.tcd.databinding.ActivityCatalogueGroupBinding
import com.shop.tcd.model.Group
import com.shop.tcd.model.Groups
import com.shop.tcd.model.Nomenclature
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.repository.main.RepositoryMain
import com.shop.tcd.repository.main.RetrofitServiceMain
import com.shop.tcd.utils.Common.saveNomenclatureList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class CatalogueGroupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCatalogueGroupBinding
    private lateinit var recyclerView: RecyclerView

    private lateinit var groupsList: ArrayList<Group>
    private lateinit var nomenclatureList: ArrayList<NomenclatureItem>

    /** Network **/
    private val retrofit = RetrofitServiceMain.getInstance()
    private val repository = RepositoryMain(retrofit)

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
        Timber.d(filterString)

        val response = repository.getByGroup(filterString)

        response.enqueue(object : Callback<Nomenclature> {
            override fun onFailure(call: Call<Nomenclature>, t: Throwable) {
                val message = "Запрос не исполнен: ${t.message.toString()}"
                Timber.e(message)
                FancyToast.makeText(
                    applicationContext,
                    message,
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    false
                ).show()
            }

            override fun onResponse(
                call: Call<Nomenclature>,
                response: Response<Nomenclature>,
            ) {
                if (response.isSuccessful) {
                    if (response.body()?.result.equals("success", false)) {
                        nomenclatureList =
                            response.body()?.nomenclature as java.util.ArrayList<NomenclatureItem>
                        FancyToast.makeText(
                            applicationContext,
                            "Загружено объектов ${nomenclatureList.size}",
                            FancyToast.LENGTH_LONG,
                            FancyToast.SUCCESS,
                            false
                        ).show()
                        GlobalScope.launch {
                            saveNomenclatureList(nomenclatureList, applicationContext)
                        }
                    } else {
                        val message = "Данные не получены: " + response.body()?.message.toString()
                        FancyToast.makeText(
                            applicationContext,
                            message,
                            FancyToast.LENGTH_LONG,
                            FancyToast.WARNING,
                            false
                        ).show()
                        Timber.d(message)
                    }
                } else {
                    val message = "Код ответа ${response.code()}"
                    FancyToast.makeText(
                        applicationContext,
                        message,
                        FancyToast.LENGTH_LONG,
                        FancyToast.WARNING,
                        false
                    ).show()
                    Timber.d(message)
                }
            }
        })
    }

    private fun getGroups() {
        val response = repository.getAllGroups()
        response.enqueue(object : Callback<Groups> {
            override fun onResponse(call: Call<Groups>, response: Response<Groups>) {
                if (response.isSuccessful) {
                    groupsList = response.body()?.group as ArrayList<Group>
                    recyclerView.adapter = GroupAdapter(groupsList)
                    FancyToast.makeText(
                        applicationContext,
                        "Запрос выполнен",
                        FancyToast.LENGTH_SHORT,
                        FancyToast.INFO,
                        false
                    ).show()
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

            override fun onFailure(call: Call<Groups>, t: Throwable) {
                FancyToast.makeText(
                    applicationContext,
                    "Сервер не отвечает!",
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    false
                ).show()
            }
        })
    }

    private fun initRecyclerView() {
        recyclerView = binding.rvCatalogueGroup
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
    }
}
