package com.shop.tcd

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.adapters.NomenclatureAdapter
import com.shop.tcd.databinding.ActivityNomenclatureBinding
import com.shop.tcd.room.dao.NomenclatureDao
import com.shop.tcd.room.database.TCDRoomDatabase

class NomenclatureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNomenclatureBinding
    private lateinit var rv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNomenclatureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        getNomenclatureList()
    }

    private fun getNomenclatureList() {
        val nomenclatureDao: NomenclatureDao
        val databaseTCD: TCDRoomDatabase = TCDRoomDatabase.getDatabase(applicationContext)
        nomenclatureDao = databaseTCD.nomDao()
        nomenclatureDao.getAllLiveData().observe(this, Observer { items ->
            rv.adapter = NomenclatureAdapter(items)
        })
    }

    private fun initRecyclerView() {
        rv = binding.rvNomenclature
        rv.layoutManager = LinearLayoutManager(this)
        rv.setHasFixedSize(true)
        rv.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
    }
}