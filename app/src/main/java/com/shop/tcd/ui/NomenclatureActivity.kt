package com.shop.tcd.ui

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.view.Menu
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.R
import com.shop.tcd.adapters.NomenclatureAdapter
import com.shop.tcd.databinding.ActivityNomenclatureBinding
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.v2.domain.database.NomenclatureDao
import com.shop.tcd.room.database.TCDRoomDatabase
import java.util.*

class NomenclatureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNomenclatureBinding
    private lateinit var rv: RecyclerView
    private lateinit var tempArrayList: ArrayList<NomenclatureItem>
    private lateinit var newArrayList: ArrayList<NomenclatureItem>
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNomenclatureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this).apply {
            setTitle("Загрузка...")
            setMessage("Пожалуйста, ожидайте")
        }

        initRecyclerView()
        getNomenclatureList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)
        val item = menu.findItem(R.id.search_action)
        val searchView = item?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                tempArrayList.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())
                if (searchText.isNotEmpty()) {
                    newArrayList.forEach {
                        if (it.name.lowercase(Locale.getDefault()).contains(searchText) ||
                            it.code.lowercase(Locale.getDefault()).contains(searchText) ||
                            it.barcode.contains(searchText)
                        ) {
                            tempArrayList.add(it)
                        }
                    }
                    rv.adapter!!.notifyDataSetChanged()
                } else {
                    tempArrayList.clear()
                    tempArrayList.addAll(newArrayList)
                    rv.adapter!!.notifyDataSetChanged()
                }
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun getNomenclatureList() {
        val nomenclatureDao: NomenclatureDao
        val databaseTCD: TCDRoomDatabase = TCDRoomDatabase.getDatabase(applicationContext)
        val adapter = NomenclatureAdapter(tempArrayList)
        nomenclatureDao = databaseTCD.nomDao()
        progressDialog.show()
        nomenclatureDao.getAllLiveData().observe(this) { items ->
            newArrayList = items as ArrayList<NomenclatureItem>
            tempArrayList.addAll(newArrayList)
            rv.adapter = adapter
//            rv.adapter = NomenclatureAdapter(items)
            progressDialog.dismiss()
        }
    }

    private fun initRecyclerView() {
        rv = binding.rvNomenclature
        rv.layoutManager = LinearLayoutManager(this)
        rv.setHasFixedSize(true)
        rv.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        newArrayList = arrayListOf()
        tempArrayList = arrayListOf()
    }
}