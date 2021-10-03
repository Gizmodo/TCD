package com.shop.tcd

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shop.tcd.Common.Common
import com.shop.tcd.adapters.InvAdapter
import com.shop.tcd.databinding.ActivityRecalcBinding
import com.shop.tcd.databinding.ActivityRecalcMaterialBinding
import com.shop.tcd.model.InvItem
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.retro.RetrofitService
import com.shop.tcd.room.dao.InvDao
import com.shop.tcd.room.dao.NomenclatureDao
import com.shop.tcd.room.database.TCDRoomDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

class RecalcActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

    val db1 by lazy { TCDRoomDatabase.getDatabase(this) }

    private var adapter: InvAdapter? = null
    private var rv: RecyclerView? = null
    private var db: TCDRoomDatabase? = null
    private lateinit var list: ArrayList<InvItem>
    private lateinit var binding: ActivityRecalcBinding
    private lateinit var bindingMaterial: ActivityRecalcMaterialBinding
    private var lst = mutableListOf<InvItem>()
    private val retrofitService = RetrofitService.getInstance()

    private val tag = this::class.simpleName
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
    val swipeToDeleteCallback = object : SwipeToDeleteCallback() {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val pos = viewHolder.bindingAdapterPosition
            val invDao: InvDao = db!!.invDao()
            CoroutineScope(Dispatchers.IO).launch {
                invDao.deleteInv(lst[pos].uid!!)
            }
            lst.removeAt(pos)
            adapter!!.notifyItemRemoved(pos)
        }
    }
    private val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
    private val onItemClick = object : InvAdapter.OnItemClickListener {
        override fun onClick(invItem: InvItem) {
            Log.d(tag, "Item clicked with ${invItem.name}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecalcBinding.inflate(layoutInflater)
        bindingMaterial = ActivityRecalcMaterialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = TCDRoomDatabase.getDatabase(this)
        rv = binding.rvRecalc
//        adapter = InvAdapter(arrayListOf())
        /* val onClickItem = object : InvAdapter.OnItemClickListener {
             override fun onClick(invItem: InvItem) {
                 Log.d(tag, "Item clicked with ${invItem.name}")
             }
         }*/

        adapter = InvAdapter(lst, onItemClick)

        rv!!.layoutManager = LinearLayoutManager(this)
        rv!!.setHasFixedSize(true)
        rv!!.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        rv!!.adapter = adapter
        itemTouchHelper.attachToRecyclerView(rv)
        getInvItems();

        binding.rgRecalc.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: RadioGroup?, p1: Int) {
                searchProduct(binding.edtRecalcEnter.text.toString())
            }
        })

        // attachBarCodeListener()

        attachBarCodeFlowListener()
    }

    fun btnAddInv(view: View) {
        val count = binding.edtCount.text.trim()
        if (TextUtils.isEmpty(count)) {
            binding.edtCount.error = "Пусто!"
            return
        } else {
            with(binding) {
                val inv = InvItem("", "", "", "", 0)
                with(inv) {
                    barcode = edtRecalcBarcode.text.toString()
                    code = edtRecalcCode.text.toString()
                    name = edtRecalcGood.text.toString()
                    plu = edtRecalcPLU.text.toString()
                    quantity = count.toString().toInt()
                }
                CoroutineScope(Dispatchers.IO).launch {
                    Common.insertInv(inv, applicationContext)
                    adapter?.notifyDataSetChanged()
                }
            }
        }

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
                ) {
                    hideKeyboard()
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    trySend(s)
                }
            }
            addTextChangedListener(listener)
            awaitClose { removeTextChangedListener(listener) }
        }.onStart { emit(text) }
    }

    fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val hideMe = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)

        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    fun post(list: ArrayList<InvItem>) {
        Log.d(tag, list.toString())

    }

    fun sendTo1C(view: View) {
        list = arrayListOf()
        val invDao: InvDao = db!!.invDao()
        invDao.selectAll().observe(this, Observer { items ->
            list = items as ArrayList<InvItem>
//            Log.d(tag, list.toString())
            post(list)
        })
/*
        val payload = Gson().toJson("{}")
        val repository = MainRepository(retrofitService)
        val response = repository.post(payload)
        response.enqueue(object : Callback<Response1C> {
            override fun onFailure(call: Call<Response1C>, t: Throwable) {
                Log.e(tag, t.message.toString())

            }

            override fun onResponse(call: Call<Response1C>, response: Response<Response1C>) {
                val res = response.body()
                Log.d(tag, res.toString())
            }

        })*/
    }

    private fun getInvItems() {
        val invDao: InvDao = db!!.invDao()
        invDao.selectAll().observe(this, Observer { items ->
            lst = items as ArrayList<InvItem>
            rv!!.adapter = InvAdapter(items, onItemClickListener = onItemClick)
        })
    }

    private fun attachBarCodeListener() {
        binding.edtRecalcEnter.addTextChangedListener(watcher)
    }

    private fun attachBarCodeFlowListener() {
        binding.edtRecalcEnter.setOnFocusChangeListener { t, te -> hideKeyboard() }
        binding.edtRecalcEnter.setOnClickListener { hideKeyboard() }
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