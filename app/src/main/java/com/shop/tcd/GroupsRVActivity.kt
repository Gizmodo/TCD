package com.shop.tcd

import BlogAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.databinding.ActivityGroupsRvactivityBinding
import com.shop.tcd.model.Group
import com.shop.tcd.model.Groups
import com.shop.tcd.repo.MainRepository
import com.shop.tcd.retro.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.sql.DataSource

class GroupsRVActivity : AppCompatActivity() {
    val TAG = this::class.simpleName

    // view binding for the activity
    private var _binding: ActivityGroupsRvactivityBinding? = null
    private val binding get() = _binding!!

//    private lateinit var binding: ActivityGroupsRvactivityBinding

//    private lateinit var linearLayoutManager: LinearLayoutManager

    //    private lateinit var adapter: RecyclerAdapter
//    private lateinit var rv: RecyclerView
    private val retrofitService = RetrofitService.getInstance()

    private var groupsList: ArrayList<Group> = ArrayList()

    // create reference to the adapter and the list
    // in the list pass the model of Language
    private lateinit var rvAdapter: RecyclerAdapter

    //    private lateinit var rvAdapter: RvAdapter
    private lateinit var languageList: List<Group>
    private lateinit var blogAdapter: BlogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_groups_rvactivity)
        _binding = ActivityGroupsRvactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        languageList = listOf<Group>()
        // initialize the adapter, and pass the required argument
//        rvAdapter = RvAdapter(languageList)
        rvAdapter = RecyclerAdapter(groupsList)
        // attach adapter to the recycler view
        binding.recView.adapter = rvAdapter

/*

        rv = binding.recyclerView
        linearLayoutManager = LinearLayoutManager(this)
        rv.layoutManager = linearLayoutManager

        //linearLayoutManager = LinearLayoutManager(this@GroupsRVActivity)
        adapter = RecyclerAdapter(groupsList)
        rv.adapter = adapter*/
        initRecyclerView()
        addDataSet()
    }
private fun addDataSet(){
blogAdapter.submitList(groupsList)
}
    private fun initRecyclerView() {
        /*binding.recView.layoutManager = LinearLayoutManager(this@GroupsRVActivity)
        blogAdapter = BlogAdapter()
        binding.recView.adapter = blogAdapter
        */
        binding.recView.apply {
            layoutManager = LinearLayoutManager(this@GroupsRVActivity)
            blogAdapter = BlogAdapter()
            adapter = blogAdapter

        }
    }

    override fun onStart() {
        super.onStart()
        requestGroups()
        /*if (groupsList.size == 0 || languageList.size == 0) {
            requestGroups()
//            adapter.notifyDataSetChanged()
          //  rvAdapter.notifyDataSetChanged()
        }*/
    }

    // on destroy of view make the binding reference to null
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
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
//                languageList = response.body()?.group as ArrayList<Group>
                Log.d(TAG, "onResponse groupList.size=${groupsList.size}")
                Log.d(TAG, "onResponse: ${response.body()!!.group[2].name}")
//                adapter.notifyDataSetChanged()
//                rvAdapter = RvAdapter(languageList)
                rvAdapter = RecyclerAdapter(groupsList)
                //      rvAdapter.notifyDataSetChanged()
                Toast.makeText(applicationContext, "Запрос выполнен", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun loadSelectedGroups(view: View) {

    }
}