@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package com.shop.tcd.ui

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.shashank.sony.fancytoastlib.FancyToast
import com.shop.tcd.adapters.InvAdapter
import com.shop.tcd.bundlizer.bundle
import com.shop.tcd.databinding.ActivityInventarisationBinding
import com.shop.tcd.model.InvItem
import com.shop.tcd.model.post.Payload
import com.shop.tcd.repository.main.RepositoryMain
import com.shop.tcd.repository.main.RetrofitServiceMain
import com.shop.tcd.room.database.DatabaseHelperImpl
import com.shop.tcd.room.database.TCDRoomDatabase
import com.shop.tcd.v2.core.utils.Common
import com.shop.tcd.v2.core.utils.Common.currentScanMode
import com.shop.tcd.v2.core.utils.Common.currentSearchMode
import com.shop.tcd.v2.core.utils.Common.parseBarcode
import com.shop.tcd.v2.core.utils.Common.selectedShop
import com.shop.tcd.v2.core.utils.Common.textChanges
import com.shop.tcd.v2.core.utils.Constants.Inventory.DEBOUNCE_TIME
import com.shop.tcd.v2.core.utils.ResponseState
import com.shop.tcd.v2.data.nomenclature.NomenclatureItem
import com.shop.tcd.v2.domain.database.InvDao
import com.shop.tcd.v2.domain.database.NomenclatureDao
import com.shop.tcd.viewmodel.InventarisationViewModel
import com.shop.tcd.viewmodel.InventarisationViewModelFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

    /** Network **/
    private val retrofit = RetrofitServiceMain.getInstance()
    private val repository = RepositoryMain(retrofit)
    private lateinit var jobAuto: Job
    private lateinit var jobManual: Job
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventarisationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModelFactory = InventarisationViewModelFactory(
            DatabaseHelperImpl(
                TCDRoomDatabase.getDatabase(applicationContext)
            )
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[InventarisationViewModel::class.java]
        bindUI()
        db = TCDRoomDatabase.getDatabase(this)
        initRecyclerView()
        attachHideKeyboardListeners()
    }

    private fun moveFocus(view: EditText) {
        view.requestFocus()
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
            autor = Common.selectedUserModel.name,
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
                progressDialog.dismiss()
                if (response.isSuccessful) {
//                    Common.deleteAllInv(applicationContext)
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

    private fun createJobAuto() = edtBarcode
        .textChanges()
        .distinctUntilChanged()
        // TODO: Remove as barcode can be empty
        .filterNot { it.isNullOrBlank() }
        .debounce(DEBOUNCE_TIME)
        .flatMapLatest { getProduct(it.toString()) }
        .onEach { displayFoundedItem(it) }
        .launchIn(lifecycleScope)

    private fun createJobManual() = edtBarcode
        .textChanges()
        .distinctUntilChanged()
        .filter {
            it?.isNotBlank() == true
        }
        .debounce(DEBOUNCE_TIME)
        .flatMapLatest {
            getProduct(it.toString())
        }
        .onEach {
            displayFoundedItem(it)
        }
        .launchIn(lifecycleScope)

    private fun displayFoundedItem(item: NomenclatureItem?) = when {
        item != null -> {
            txtGood.text = item.name
            txtCode.text = item.code
            txtBarcode.text = item.barcode
            // TODO: Это поле заполняется из ответа
            //  на запрос к БД с группировкой по штрихкоду
            txtTotal.text = item.code // TODO: 1) Выводить общее кол-во!!!
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

    private fun clearFieldsAfterInsert() {
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

    private fun clearFields() {
        when (currentScanMode) {
            Common.MODESCAN.AUTO -> {
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
            Common.MODESCAN.MANUAL -> {
                "".also {
                    edtCount.setText(it)
                    txtGood.text = it
                    txtCode.text = it
                    txtBarcode.text = it
                    txtTotal.text = it
                    txtPrice.text = it
                }
            }
        }

    }

    private fun getProduct(
        string: String,
    ): Flow<NomenclatureItem?> {
        val nomenclatureDao: NomenclatureDao = db!!.nomDao()
        val prefix = string.take(2)
        when (currentSearchMode) {
            Common.SEARCHBY.BARCODE -> {
                return if (prefix == selectedShop.shopPrefixWeight) {
                    //Весовой товар
                    val productCode = string.takeLast(11).take(5)
                    nomenclatureDao.getByCode(productCode).asFlow()
                } else {
                    //Обычный ШК
                    nomenclatureDao.getByBarcode(string).asFlow()
                }
            }
            Common.SEARCHBY.CODE -> {
                return nomenclatureDao.getByCode(string).asFlow()
            }
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
        } else if (edtBarcode.text.toString().toBigIntegerOrNull() == null) {
            FancyToast.makeText(
                applicationContext, "ШК пустой!",
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
                    clearFieldsAfterInsert()
                    moveFocus(edtBarcode)
                }
            }
        }
    }

    private fun onFocus(view: View?, hasFocus: Boolean) {
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
            setOnFocusChangeListener { view, hasFocus -> onFocus(view, hasFocus) }
        }
        edtBarcode.apply {
            showSoftInputOnFocus = false
            setSelectAllOnFocus(true)
            setOnClickListener { hideKeyboard() }
            setOnFocusChangeListener { _, _ -> hideKeyboard() }
            setOnFocusChangeListener { view, hasFocus -> onFocus(view, hasFocus) }
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
            isFocusable = false
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
            val bundle: Bundle = invItem.bundle(InvItem.serializer())
            val intent = Intent(this@InventarisationActivity, DetailActivity::class.java)
                .apply {
                    putExtra("item", bundle)
                }
            startActivity(intent, bundle)
        }
    }
}