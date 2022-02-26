package com.shop.tcd.ui

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.R
import com.shop.tcd.adapters.InvAdapter
import com.shop.tcd.databinding.ActivityListBinding
import com.shop.tcd.model.InvItem
import com.shop.tcd.room.database.DatabaseHelperImpl
import com.shop.tcd.room.database.TCDRoomDatabase
import com.shop.tcd.viewmodel.InventoryListViewModel
import com.shop.tcd.viewmodel.InventoryListViewModelFactory
import timber.log.Timber

private lateinit var binding: ActivityListBinding
private lateinit var rv: RecyclerView
private lateinit var viewModel: InventoryListViewModel
private lateinit var viewModelFactory: InventoryListViewModelFactory
private lateinit var adapter: InvAdapter

class ListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModelInit()
        bindUI()
        initObservers()
    }

    private fun viewModelInit() {
        viewModelFactory = InventoryListViewModelFactory(
            DatabaseHelperImpl(
                TCDRoomDatabase.getDatabase(applicationContext)
            )
        )

        viewModel = ViewModelProvider(this, viewModelFactory)[InventoryListViewModel::class.java]
    }

    private fun initObservers() {
        viewModel.inventoryList.observe(this) {
            adapter = InvAdapter(it, onItemClick)
            rv.adapter = adapter
        }
    }

    private val onItemClick = object : InvAdapter.OnItemClickListener {
        override fun onClick(invItem: InvItem, position: Int) {
            val dialog = Dialog(this@ListActivity)
            dialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_inventory_quantity)

            val edtQuantity = dialog.findViewById<EditText>(R.id.edtQuantity)
            val btnUpdateQuantity = dialog.findViewById<Button>(R.id.btnUpdateQuantity)
            val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
            val txtName = dialog.findViewById<TextView>(R.id.txtName)

            txtName.text = invItem.name
            edtQuantity.setText(invItem.quantity)

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnUpdateQuantity.setOnClickListener {
                val quantity = edtQuantity.text.toString().replace(',', '.')
                Timber.d(quantity)
                quantity.toFloatOrNull()?.let {
                    viewModel.updateInventoryQuantity(invItem.uid!!, quantity)
                    adapter.notifyDataSetChanged()
                }
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    private fun bindUI() {
        rv = binding.rvInventoryList
        rv.apply {
            layoutManager = LinearLayoutManager(this@ListActivity)
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(this@ListActivity, LinearLayout.VERTICAL))
        }
    }
}