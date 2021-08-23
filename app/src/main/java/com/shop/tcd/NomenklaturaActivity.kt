package com.shop.tcd

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.shop.tcd.Common.Common
import com.shop.tcd.model.Groups
import com.shop.tcd.model.Nomenclature
import com.shop.tcd.repo.MainRepository
import com.shop.tcd.retro.RetrofitService
import com.shop.tcd.retro.RetrofitServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NomenklaturaActivity : AppCompatActivity() {
    var mService = Common.retrofitService
    val movieList = MutableLiveData<Nomenclature>()
    val errorMessage = MutableLiveData<String>()
    val TAG = this::class.simpleName
    private val retrofitService = RetrofitService.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nomenklatura)
    }

    fun workedRequestToGroups() {
        val repository = MainRepository(retrofitService)
        val response = repository.getAllGroups()
        response.enqueue(object : Callback<Groups> {
            override fun onFailure(call: Call<Groups>, t: Throwable) {
                Log.d(TAG, "Response bad")
                Log.e(TAG, t.message.toString())
                Toast.makeText(applicationContext, "Запрос не выполнен", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Groups>, response: Response<Groups>) {
                Log.d(TAG, "Response good")
                Toast.makeText(applicationContext, "Запрос выполнен", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getAllGroups(view: View) {
        val intent = Intent(this, GroupsRVActivity::class.java).apply {
        }
        startActivity(intent)
    }

    fun getAllGoods(view: View) {
        val repository = MainRepository(retrofitService)
        val response = repository.getAllGoods()
        response.enqueue(object : Callback<Nomenclature> {
            override fun onResponse(call: Call<Nomenclature>, response: Response<Nomenclature>) {
                Log.d(TAG, response.isSuccessful.toString())
                Log.d(TAG, "Response getAllGoods good")
                //  movieList.postValue(response.body())
            }

            override fun onFailure(call: Call<Nomenclature>, t: Throwable) {
                Log.d(TAG, "Response bad")
                Log.e(TAG, t.message.toString())
                errorMessage.postValue(t.message)
            }
        })
    }
}