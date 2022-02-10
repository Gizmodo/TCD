package com.shop.tcd

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.shashank.sony.fancytoastlib.FancyToast
import com.shop.tcd.adapters.InvAdapter
import com.shop.tcd.bundlizer.bundle
import com.shop.tcd.databinding.ActivityZebraBinding
import com.shop.tcd.model.InvItem
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.model.post.Payload
import com.shop.tcd.repository.main.RepositoryMain
import com.shop.tcd.repository.main.RetrofitServiceMain
import com.shop.tcd.room.dao.InvDao
import com.shop.tcd.room.dao.NomenclatureDao
import com.shop.tcd.room.database.TCDRoomDatabase
import com.shop.tcd.utils.Common
import com.shop.tcd.utils.Common.parseBarcode
import com.shop.tcd.utils.Common.textChanges
import com.shop.tcd.utils.ResponseState
import com.shop.tcd.viewmodel.RecalcViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class RecalcNewActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var binding: ActivityZebraBinding

    /// UI
    private lateinit var tilBarcode: TextInputLayout
    private lateinit var edtBarcode: TextInputEditText
    private lateinit var tilCount: TextInputLayout
    private lateinit var edtCount: TextInputEditText
    private lateinit var txtGood: TextView
    private lateinit var txtCode: TextView
    private lateinit var txtBarcode: TextView
    private lateinit var txtTotal: TextView
    private lateinit var txtPrice: TextView
    private lateinit var rv: RecyclerView
    private lateinit var btnSend: Button
    private lateinit var btnInsert: FloatingActionButton

    private var db: TCDRoomDatabase? = null
    private lateinit var progressDialog: ProgressDialog

    private val model: RecalcViewModel by viewModels()
    private var adapter: InvAdapter? = null
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private var list = mutableListOf<InvItem>()

    /** Network **/
    private val retrofit = RetrofitServiceMain.getInstance()
    private val repository = RepositoryMain(retrofit)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
        binding = ActivityZebraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bindUI()
        bindListeners()
        db = TCDRoomDatabase.getDatabase(this)
        initRecyclerView()
        attachHideKeyboardListeners()
        initBarcodeFieldListener()
    }

    private fun bindListeners() {
        btnSend.setOnClickListener {
            sendTo1C()
        }
        btnInsert.setOnClickListener {
            Timber.d("Нажата кнопка добавления элемента в номенклатуру")
            insert()
        }
    }

    private fun sendInventory() {
        if (list.isEmpty()) {
            return
        }
        progressDialog.show()
        val payload = Payload(
            result = "success",
            message = "",
            operation = "revision",
            autor = Common.selectedUser.userLogin,
            shop = Common.selectedShop.shopName,
            prefix = Common.selectedShop.shopPrefix,
            document = list
        )

        val response = repository.postInventory(payload)

        response.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                progressDialog.dismiss()
                FancyToast.makeText(
                    applicationContext,
                    "Ошибка отправки запроса: ${t.message.toString()}",
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    false
                ).show()
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                Timber.i("Response " + response.body())
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    Common.deleteAllInv(applicationContext)
                    FancyToast.makeText(
                        applicationContext,
                        "Успешная загрузка документа",
                        FancyToast.LENGTH_LONG,
                        FancyToast.SUCCESS,
                        false
                    ).show()
                } else {
                    FancyToast.makeText(
                        applicationContext,
                        "Код: ${response.code()}. Ошибка ${response.message()}",
                        FancyToast.LENGTH_LONG,
                        FancyToast.WARNING,
                        false
                    ).show()
                }
            }
        })
    }

    fun sendTo1C() {
        val builderAlert = AlertDialog.Builder(this)
        with(builderAlert) {
            setTitle("Внимание")
            setMessage("Выгрузить документ в программу?")
            setPositiveButton("Да") { _: DialogInterface, _: Int ->
                list = arrayListOf()
                val invDao: InvDao = db!!.invDao()
                invDao.selectAll().observe(this@RecalcNewActivity) { items ->
                    list = items as ArrayList<InvItem>
                    sendInventory()
                }
            }
            setNegativeButton(
                "Нет"
            ) { _: DialogInterface, _: Int ->
                FancyToast.makeText(
                    applicationContext,
                    "Выгрузка отменена",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.INFO,
                    false
                ).show()
            }
            show()
        }
    }

    private fun initBarcodeFieldListener() {
        // TODO: Check focus on real device
        edtBarcode.setOnFocusChangeListener { _, _ -> hideKeyboard() }
        edtBarcode.setOnClickListener { hideKeyboard() }
        edtBarcode
            .textChanges()
            .distinctUntilChanged()
            // TODO: Remove as barcode can be empty
            .filterNot { it.isNullOrBlank() }
            .debounce(100)
            .flatMapLatest { getProduct(it.toString()) }
            .onEach { displayFoundedItem(it) }
            .launchIn(lifecycleScope)
    }

    private fun displayFoundedItem(item: NomenclatureItem?) = when {
        item != null -> {
            txtGood.text = item.name
            txtCode.text = item.code
            txtBarcode.text = item.barcode
            // TODO: Это поле заполняется из ответа
            //  на запрос к БД с группировкой по штрихкоду
            txtTotal.text = item.code
            txtPrice.text = item.price
            val barcode = edtBarcode
                .text
                .toString()
                .padStart(13, '0')
                .takeLast(13)

            when (val response = parseBarcode(barcode)) {
                is ResponseState.Success -> {
                    edtCount.setText(response.item)
                }
                is ResponseState.Error -> {
                    FancyToast.makeText(
                        applicationContext,
                        response.throwable.message,
                        FancyToast.LENGTH_LONG,
                        FancyToast.WARNING,
                        false
                    ).show()
                }
            }
        }
        else -> {
            clearFields()
        }
    }

    private fun clearFields() {
        "".also {
            edtBarcode.setText(it)
            edtCount.setText(it)
            txtGood.text = it
            txtCode.text = it
            txtBarcode.text = it
            txtTotal.text = it
            txtPrice.text = it
        }
    }

    private fun getProduct(barcode: String): Flow<NomenclatureItem?> {
        val nomenclatureDao: NomenclatureDao = db!!.nomDao()
        return nomenclatureDao.getByBarcode(barcode).asFlow()
    }

    private fun bindUI() {
        progressDialog = ProgressDialog(this).apply {
            setTitle("Загрузка...")
            setMessage("Пожалуйста, ожидайте")
        }

        tilBarcode = binding.tilBarcode
        edtBarcode = binding.edtBarcode
        tilCount = binding.tilCount
        edtCount = binding.edtCount
        txtGood = binding.txtZGood
        rv = binding.recyclerView
        txtCode = binding.txtZCode
        txtBarcode = binding.txtZBarcode
        txtTotal = binding.txtZTotal
        txtPrice = binding.txtZPrice
        btnInsert = binding.btnInsertItem
        btnSend = binding.btnSend1C
    }

    private fun insert() {
        val inv = InvItem("", "", "", "", "")
        with(inv) {
            barcode = edtBarcode.text.toString()
            code = txtCode.text.toString()
            name = txtGood.text.toString()
            // TODO: PLU пустой, а должен быть заполнен
            plu = edtBarcode.text.toString()
            quantity = edtCount.text.toString()
        }

        CoroutineScope(Dispatchers.IO).launch {
            Common.insertInv(inv, applicationContext)
            adapter?.notifyDataSetChanged()
            withContext(Dispatchers.Main) {
                FancyToast.makeText(
                    applicationContext,
                    "Товар добавлен",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.SUCCESS,
                    false
                ).show()
                clearFields()
            }
        }
    }

    private fun attachHideKeyboardListeners() {
        binding.tilCount.apply {
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
        }
        binding.tilBarcode.apply {
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
        }
        binding.edtCount.apply {
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
        }
        binding.edtBarcode.apply {
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
        }
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val hideMe = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)

        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun initRecyclerView() {
        adapter = InvAdapter(list, onItemClick)
        rv.apply {
            layoutManager = LinearLayoutManager(this@RecalcNewActivity)
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(this@RecalcNewActivity, LinearLayout.VERTICAL))
            adapter = adapter
        }
        getInventarisationItems()
    }

    private fun getInventarisationItems() {
        val invDao: InvDao = db!!.invDao()
        invDao.selectAll().observe(this, Observer { items ->
            list = items as ArrayList<InvItem>
            rv.adapter = InvAdapter(items, onItemClick)
        })
    }

    private val onItemClick = object : InvAdapter.OnItemClickListener {
        override fun onClick(invItem: InvItem) {
            Timber.d("Item clicked with " + invItem.name)
            val bundle: Bundle = invItem.bundle(InvItem.serializer())
            val intent = Intent(this@RecalcNewActivity, DetailActivity::class.java)
                .apply {
                    putExtra("item", bundle)
                }
            startActivity(intent, bundle)
        }
    }
}