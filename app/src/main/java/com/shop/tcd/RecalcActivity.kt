package com.shop.tcd

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.RadioGroup
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.shop.tcd.databinding.ActivityRecalcBinding
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.room.dao.NomenclatureDao
import com.shop.tcd.room.database.TCDRoomDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

class RecalcActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    private lateinit var binding: ActivityRecalcBinding
    val tag = this::class.simpleName
    val watcher = object : TextWatcher {
        private var searchFor = ""

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val searchText = s.toString().trim()
            if (searchText == searchFor)
                return
            searchFor = searchText
            launch {
                delay(300)  //debounce timeOut
                if (searchText != searchFor)
                    return@launch
                searchProduct(searchFor)
            }
        }

        override fun afterTextChanged(s: Editable?) = Unit
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    }

    @ExperimentalCoroutinesApi
    @CheckResult
    fun EditText.textChanges(): Flow<CharSequence?> {
        return callbackFlow {
            val listener = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) = Unit
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    trySend(s)
                }
            }
            addTextChangedListener(listener)
            awaitClose { removeTextChangedListener(listener) }
        }.onStart { emit(text) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecalcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rgRecalc.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: RadioGroup?, p1: Int) {
                searchProduct(binding.edtRecalcEnter.text.toString())
            }
        })

        // attachBarCodeListener()

        attachBarCodeFlowListener()
    }

    private fun attachBarCodeListener() {
        binding.edtRecalcEnter.addTextChangedListener(watcher)
    }

    private fun attachBarCodeFlowListener() {
        binding.edtRecalcEnter
            .textChanges()
            .distinctUntilChanged()
            .filterNot { it.isNullOrBlank() }
            .debounce(300)
            .flatMapLatest { searchProductFlow(it.toString()) }
            .onEach { populate(it) }
            .launchIn(lifecycleScope)
    }

    private fun searchProductFlow(search: String): Flow<NomenclatureItem?> {
        val nomenclatureDao: NomenclatureDao
        val databaseTCD: TCDRoomDatabase = TCDRoomDatabase.getDatabase(applicationContext)
        nomenclatureDao = databaseTCD.nomDao()
        var res: Flow<NomenclatureItem?> = emptyFlow()
        when (binding.rgRecalc.checkedRadioButtonId) {
            binding.rbtRecalcBarcode.id -> res = nomenclatureDao.getByBarcode(search).asFlow()
            binding.rbtRecalcCode.id -> res = nomenclatureDao.getByCode(search).asFlow()
            binding.rbtRecalcPLU.id -> res = nomenclatureDao.getByPLU(search).asFlow()
        }
        return res
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