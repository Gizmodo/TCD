package com.shop.tcd

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.databinding.ActivityGroupsRvactivityBinding
import com.shop.tcd.model.Group
import com.shop.tcd.model.Groups
import com.shop.tcd.repo.MainRepository
import com.shop.tcd.retro.RetrofitService
import okhttp3.internal.notify
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupsRVActivity : AppCompatActivity() {
    val TAG = this::class.simpleName

    private lateinit var binding: ActivityGroupsRvactivityBinding

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapter
    private lateinit var rv: RecyclerView
    private val retrofitService = RetrofitService.getInstance()

    private var groupsList: ArrayList<Group> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups_rvactivity)
        binding = ActivityGroupsRvactivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        linearLayoutManager = LinearLayoutManager(this@GroupsRVActivity)
        adapter = RecyclerAdapter(groupsList)
        rv.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        if (groupsList.size == 0) {
            requestGroups()
            adapter.notifyDataSetChanged()
        }
    }

    fun requestGroups() {
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
                Log.d(TAG, "onResponse: ${response.body()!!.group[2].name}")
                adapter.notifyDataSetChanged()
                Toast.makeText(applicationContext, "Запрос выполнен", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun loadSelectedGroups(view: View) {

    }
}