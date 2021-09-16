package com.shop.tcd

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.databinding.ActivityCatalogueGroupBinding
import com.shop.tcd.model.Group
import com.shop.tcd.model.Groups
import com.shop.tcd.repo.MainRepository
import com.shop.tcd.retro.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CatalogueGroupActivity : AppCompatActivity() {
    val TAG = this::class.simpleName

    private lateinit var binding: ActivityCatalogueGroupBinding
    private lateinit var rv: RecyclerView
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var groupsList: ArrayList<Group>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogueGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        doRequest()
    }

    fun btnCatalogGroupLoadSelected(view: View) {

    }

    private fun doRequest() {
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
                groupsList = response.body()?.group as ArrayList<Group>
                rv.adapter = MyAdapterBinding(groupsList)
//                languageList = response.body()?.group as ArrayList<Group>
                Log.d(TAG, "onResponse groupList.size=${groupsList.size}")
                Log.d(TAG, "onResponse: ${response.body()!!.group[2].name}")
//                adapter.notifyDataSetChanged()
//                rvAdapter = RvAdapter(languageList)
                // rvAdapter = RecyclerAdapter(groupsList)
                //      rvAdapter.notifyDataSetChanged()
                Toast.makeText(applicationContext,
                    "onResponse groupList.size=${groupsList.size}",
                    Toast.LENGTH_SHORT).show()
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
