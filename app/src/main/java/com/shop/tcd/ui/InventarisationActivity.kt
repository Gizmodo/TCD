package com.shop.tcd.ui

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.shashank.sony.fancytoastlib.FancyToast
import com.shop.tcd.R
import com.shop.tcd.adapters.InvAdapter
import com.shop.tcd.broadcast.ReceiverLiveData
import com.shop.tcd.bundlizer.bundle
import com.shop.tcd.databinding.ActivityInventarisationBinding
import com.shop.tcd.model.InvItem
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.model.post.Payload
import com.shop.tcd.repository.main.RepositoryMain
import com.shop.tcd.repository.main.RetrofitServiceMain
import com.shop.tcd.room.dao.InvDao
import com.shop.tcd.room.dao.NomenclatureDao
import com.shop.tcd.room.database.DatabaseHelperImpl
import com.shop.tcd.room.database.TCDRoomDatabase
import com.shop.tcd.utils.Common
import com.shop.tcd.utils.Common.parseBarcode
import com.shop.tcd.utils.Common.selectedShop
import com.shop.tcd.utils.Common.setReadOnly
import com.shop.tcd.utils.Common.textChanges
import com.shop.tcd.utils.Constants.Inventory.BARCODE_LENGTH
import com.shop.tcd.utils.Constants.Inventory.DEBOUNCE_TIME
import com.shop.tcd.utils.ResponseState
import com.shop.tcd.viewmodel.InventarisationViewModel
import com.shop.tcd.viewmodel.InventarisationViewModelFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class InventarisationActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var binding: ActivityInventarisationBinding

    //    UI
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
    private lateinit var btnInsert: Button

    private var db: TCDRoomDatabase? = null
    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: InventarisationViewModel
    private lateinit var viewModelFactory: InventarisationViewModelFactory
    private var adapter: InvAdapter? = null
    override val coroutineContext: CoroutineContext = Dispatchers.Main
    private var list = mutableListOf<InvItem>()
    private var idataBarcodeObserver: MutableLiveData<String>? = null
    private var urovoKeyboard: MutableLiveData<Boolean>? = null
    private var urovoScanner: MutableLiveData<String>? = null

    /** Network **/
    private val retrofit = RetrofitServiceMain.getInstance()
    private val repository = RepositoryMain(retrofit)
    private lateinit var jobAuto: Job
    private lateinit var jobManual: Job
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
        binding = ActivityInventarisationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModelFactory = InventarisationViewModelFactory(
            DatabaseHelperImpl(
                TCDRoomDatabase.getDatabase(applicationContext)
            )
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[InventarisationViewModel::class.java]
        bindUI()
        bindListeners()
        db = TCDRoomDatabase.getDatabase(this)
        initRecyclerView()
        attachHideKeyboardListeners()
        initBarcodeFieldListener()
        initBarcodeListener()
    }

    private fun moveFocus(view: EditText) {
        view.requestFocus()
    }

    private fun moveFocus(btn: Button) {
        btn.requestFocus()
    }

    private fun doOnGetBarcode(data: String) {
        edtBarcode.apply {
            setText(data)
            requestFocus()
        }
    }

    private fun bindListeners() {
        btnSend.setOnClickListener {
            sendTo1C()
        }
        btnInsert.setOnClickListener {
            addNomenclatureItem()
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
            shop = selectedShop.shopName,
            prefix = selectedShop.shopPrefix,
            document = list
        )

        val response = repository.postInventory(payload)

        response.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                progressDialog.dismiss()
                FancyToast.makeText(
                    applicationContext,
                    "Ошибка отправки запроса: ${t.message}",
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.recalc_menu, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)
        return super.onCreateOptionsMenu(menu)
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.removeItems -> {
                removeAll()
                true
            }
            R.id.menu_mode_auto -> {
                edtBarcode.setReadOnly(value = true)
                item.isChecked = true
                jobAuto = edtBarcode
                    .textChanges()
                    .distinctUntilChanged()
                    .filterNot { it.isNullOrBlank() }
                    .debounce(DEBOUNCE_TIME)
                    .flatMapLatest { getProduct(it.toString()) }
                    .onEach { displayFoundedItem(it) }
                    .launchIn(lifecycleScope)
                true
            }
            R.id.menu_mode_manual -> {
                edtBarcode.setReadOnly(value = false)
                item.isChecked = true
                jobAuto.cancel()
                jobManual = edtBarcode
                    .textChanges()
                    .distinctUntilChanged()
                    .filter { it?.length == BARCODE_LENGTH }
                    .debounce(DEBOUNCE_TIME)
                    .flatMapLatest { getProduct(it.toString()) }
                    .onEach { displayFoundedItem(it) }
                    .launchIn(lifecycleScope)
                true
            }
            R.id.menuInventoryList -> {
                val intent = Intent(this, ListActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun removeAll() {
        val builderAlert = AlertDialog.Builder(this)
        with(builderAlert) {
            setTitle("Внимание")
            setMessage("Очистить все записи документов?")
            setPositiveButton("Да") { _: DialogInterface, _: Int ->
                Common.deleteAllInv(applicationContext)
            }
            setNegativeButton(
                "Нет"
            ) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun sendTo1C() {
        val builderAlert = AlertDialog.Builder(this)
        with(builderAlert) {
            setTitle("Внимание")
            setMessage("Выгрузить документы в 1С?")
            setPositiveButton("Да") { _: DialogInterface, _: Int ->
                list = arrayListOf()
                viewModel.fetchInventarisationItems()
                viewModel.getInventarisationItems().observe(this@InventarisationActivity) { items ->
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

    private fun sendTo1Old() {
        val builderAlert = AlertDialog.Builder(this)
        with(builderAlert) {
            setTitle("Внимание")
            setMessage("Выгрузить документы в 1С?")
            setPositiveButton("Да") { _: DialogInterface, _: Int ->
                list = arrayListOf()
                val invDao: InvDao = db!!.invDao()
                invDao.selectAllSingle()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { it ->
                        list = it as ArrayList<InvItem>
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

    private fun initBarcodeListener() {
        idataBarcodeObserver = ReceiverLiveData(
            applicationContext,
            IntentFilter("android.intent.action.SCANRESULT")
        ) { _, intent ->
            var data = ""
            intent.extras?.let { data = it["value"].toString() }
            return@ReceiverLiveData data
        }

        urovoScanner = ReceiverLiveData(
            applicationContext,
            IntentFilter("android.intent.ACTION_DECODE_DATA")
        ) { _, intent ->
            var data = ""
            intent.extras?.let { data = it["barcode_string"].toString() }
            return@ReceiverLiveData data
        }

        urovoKeyboard = ReceiverLiveData(
            applicationContext,
            IntentFilter("android.intent.action_keyboard")
        ) { _, intent ->
            var data = false
            intent.extras?.let {
                data = it["kbrd_enter"].toString() == "enter"
            }
            return@ReceiverLiveData data
        }

        urovoKeyboard?.observe(this) {
            if (edtCount.isFocused) {
                edtCount.setSelection(0)
                moveFocus(btnInsert)
                btnInsert.callOnClick()
            } else if (edtBarcode.isFocused) {
                moveFocus(edtCount)
            } else if (btnInsert.isFocused) {
                moveFocus(edtBarcode)
            }
        }

        urovoScanner?.observe(this) {
            clearFields()
            doOnGetBarcode(it)
        }
        idataBarcodeObserver?.observe(this) {
            doOnGetBarcode(it)
        }
    }

    private fun initBarcodeFieldListener() {
        edtBarcode.setOnFocusChangeListener { _, _ -> hideKeyboard() }
        edtBarcode.setOnClickListener { hideKeyboard() }
        edtBarcode.setReadOnly(value = true)
        jobAuto = edtBarcode
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

    private fun getProduct(string: String): Flow<NomenclatureItem?> {
        val nomenclatureDao: NomenclatureDao = db!!.nomDao()
        val prefix = string.take(2)
        return if (prefix == selectedShop.shopPrefixWeight) {
//Весовой товар
            val productCode = string.takeLast(11).take(5)
            nomenclatureDao.getByCode(productCode).asFlow()
        } else {
//Обычный ШК
            nomenclatureDao.getByBarcode(string).asFlow()
        }
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

    private fun addNomenclatureItem() {
        val count = edtCount.text.toString().replace(',', '.')
        if (count.toFloatOrNull() == null) {
            FancyToast.makeText(
                applicationContext,
                "Укажите количество!",
                FancyToast.LENGTH_SHORT,
                FancyToast.CONFUSING,
                false
            ).show()
        } else {
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
                    moveFocus(edtBarcode)
                }
            }
        }
    }

    private fun onFocus(view: View?, hasFocus: Boolean, s: String) {
        if (hasFocus) {
            (view as TextInputEditText).selectAll()
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

        edtCount.apply {
            showSoftInputOnFocus = false
            setSelectAllOnFocus(true)
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
            setOnFocusChangeListener { view, hasFocus -> onFocus(view, hasFocus, "edtCount") }
        }
        edtBarcode.apply {
            showSoftInputOnFocus = false
            setSelectAllOnFocus(true)
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
            setOnFocusChangeListener { view, hasFocus -> onFocus(view, hasFocus, "edtBarcode") }
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
        rv.apply {
            isFocusable = false
            setOnFocusChangeListener { _, _ -> Timber.d("onFocus recyclerView") }
        }
        adapter = InvAdapter(list, onItemClick)
        rv.apply {
            layoutManager = LinearLayoutManager(this@InventarisationActivity)
            setHasFixedSize(true)
            addItemDecoration(
                DividerItemDecoration(
                    this@InventarisationActivity,
                    LinearLayout.VERTICAL
                )
            )
            adapter = adapter
        }
        getInventarisationItemsGroupByBarcode()
    }

    private fun getInventarisationItemsGroupByBarcode() {
        val invDao: InvDao = db!!.invDao()
        invDao.selectSumGroupByBarcode().observe(this) { items ->
            list = items as ArrayList<InvItem>
            rv.adapter = InvAdapter(items, onItemClick)
        }
    }

    private val onItemClick = object : InvAdapter.OnItemClickListener {
        override fun onClick(invItem: InvItem, position: Int) {
            Timber.d("Item clicked with " + invItem.name)
            val bundle: Bundle = invItem.bundle(InvItem.serializer())
            val intent = Intent(this@InventarisationActivity, DetailActivity::class.java)
                .apply {
                    putExtra("item", bundle)
                }
            startActivity(intent, bundle)
        }
    }
}