package com.shop.tcd

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.shop.tcd.databinding.ActivityRecalcBinding
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.room.dao.NomenclatureDao
import com.shop.tcd.room.database.TCDRoomDatabase

class RecalcActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecalcBinding
    val tag = this::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecalcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rgRecalc.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: RadioGroup?, p1: Int) {
                searchProduct(binding.edtRecalcEnter.text.toString())
            }
        })

        binding.edtRecalcEnter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchProduct(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }

    fun searchProduct(search: String) {
        val nomenclatureDao: NomenclatureDao
        val databaseTCD: TCDRoomDatabase = TCDRoomDatabase.getDatabase(applicationContext)
        nomenclatureDao = databaseTCD.nomDao()
        if (search.isBlank()) {
            clearFields()
        } else {
            when (binding.rgRecalc.checkedRadioButtonId) {
                binding.rbtRecalcBarcode.id -> {
                    nomenclatureDao.getByBarcode(search)
                        .observe(this) { item ->
                            populate(item)
                        }
                }
                binding.rbtRecalcCode.id -> {
                    nomenclatureDao.getByCode(search)
                        .observe(this) { item ->
                            populate(item)
                        }
                }
                binding.rbtRecalcPLU.id -> {
                    nomenclatureDao.getByPLU(search)
                        .observe(this) { item ->
                            populate(item)
                        }
                }
            }
        }
    }

    private fun populate(item: NomenclatureItem?) = if (item != null) {
        with(binding) {
            edtRecalcCode.setText(item.code)
            edtRecalcPLU.setText(item.plu)
            edtRecalcBarcode.setText(item.barcode)
            edtRecalcGood.setText(item.name)
            edtRecalcPrice.setText(item.price)
        }
    } else {
        clearFields()
    }

    private fun clearFields() {
        with(binding) {
            edtRecalcCode.setText("")
            edtRecalcPLU.setText("")
            edtRecalcBarcode.setText("")
            edtRecalcGood.setText("")
            edtRecalcPrice.setText("")
        }
    }
}