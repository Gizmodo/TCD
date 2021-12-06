package com.shop.tcd

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.annotation.CheckResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shashank.sony.fancytoastlib.FancyToast
import com.shop.tcd.adapters.InvAdapter
import com.shop.tcd.bundlizer.bundle
import com.shop.tcd.databinding.ActivityRecalcBinding
import com.shop.tcd.model.InvItem
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.model.post.Payload
import com.shop.tcd.repository.main.RepositoryMain
import com.shop.tcd.repository.main.RetrofitServiceMain
import com.shop.tcd.room.dao.InvDao
import com.shop.tcd.room.dao.NomenclatureDao
import com.shop.tcd.room.database.TCDRoomDatabase
import com.shop.tcd.utils.Common
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import kotlin.coroutines.CoroutineContext


class RecalcActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main

//    val db1 by lazy { TCDRoomDatabase.getDatabase(this) }

    private var adapter: InvAdapter? = null
    private var rv: RecyclerView? = null
    private var db: TCDRoomDatabase? = null
    private lateinit var list: ArrayList<InvItem>
    private lateinit var binding: ActivityRecalcBinding

    //    private lateinit var bindingMaterial: ActivityRecalcMaterialBinding
    private var lst = mutableListOf<InvItem>()

    /** Network **/
    private val retrofit = RetrofitServiceMain.getInstance()
    private val repository = RepositoryMain(retrofit)

    private lateinit var progressDialog: ProgressDialog
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

    /* val swipeToDeleteCallback = object : SwipeToDeleteCallback() {
         override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
             val builder = AlertDialog.Builder(this@RecalcActivity)
             with(builder) {
                 setTitle("Внимание")
                 setMessage("Подтвердить удаление?")
                 setPositiveButton("Да") { dialog: DialogInterface, which: Int ->
                     val pos = viewHolder.adapterPosition
                     Log.d(tag, pos.toString())
                     val invDao: InvDao = db!!.invDao()
                     CoroutineScope(Dispatchers.IO).launch {
                         invDao.deleteInv(lst[pos].uid!!)
                         lst.removeAt(pos)
                         adapter!!.notifyItemRemoved(pos)
                     }
                 }
                 setNegativeButton("Нет") { dialog: DialogInterface, which: Int ->
                     adapter?.notifyDataSetChanged()
                 }
                 show()
             }
        }
    }
    private val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
   */
    private val onItemClick = object : InvAdapter.OnItemClickListener {
        override fun onClick(invItem: InvItem) {
            Timber.d("Item clicked with " + invItem.name)
            val bundle: Bundle = invItem.bundle(InvItem.serializer())
            val intent = Intent(this@RecalcActivity, DetailActivity::class.java)
                .apply {
                    putExtra("item", bundle)
                }
            startActivity(intent, bundle)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecalcBinding.inflate(layoutInflater)
        //  bindingMaterial = ActivityRecalcMaterialBinding.inflate(layoutInflater)
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
        getInvItems()

        binding.rgRecalc.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: RadioGroup?, p1: Int) {
                searchProduct(binding.edtRecalcEnter.text.toString())
            }
        })

        // attachBarCodeListener()
        attachBarCodeFlowListener()

        progressDialog = ProgressDialog(this).apply {
            setTitle("Загрузка...")
            setMessage("Пожалуйста, ожидайте")
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun btnAddInvOld(view: View) {
        val count = binding.edtCount.text.trim()
        if (TextUtils.isEmpty(count)) {
            binding.edtCount.error = "Пусто!"
            return
        } else {
            with(binding) {
                val inv = InvItem("", "", "", "", "")
                with(inv) {
                    barcode = edtRecalcBarcode.text.toString()
                    code = edtRecalcCode.text.toString()
                    name = edtRecalcGood.text.toString()
                    plu = edtRecalcPLU.text.toString()
                    quantity = count.toString()
                }
                CoroutineScope(Dispatchers.IO).launch {
                    Common.insertInv(inv, applicationContext)
                    adapter?.notifyDataSetChanged()
                }
            }
        }

    }

    private fun isEAN13(barcode: String): Boolean {
        var ch = 0
        var nch = 0
        val barcode12 = barcode.take(12)

        barcode12.forEachIndexed { index, c ->
            when {
                index % 2 == 0 -> ch += Character.digit(c, 10)
                else -> nch += Character.digit(c, 10)
            }
        }
        val checkSumDigit = ((10 - (ch + 3 * nch) % 10) % 10)

        return (((barcode.length == 13) && (checkSumDigit.toString() == barcode.last().toString())))
    }

    @Suppress("UNUSED_PARAMETER")
    fun btnDelete(view: View) {
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

    @Suppress("UNUSED_PARAMETER")
    fun btnAddInv(view: View) {
        when (binding.rgRecalc.checkedRadioButtonId) {
            binding.rbtRecalcCode.id -> checkCode()
            binding.rbtRecalcBarcode.id -> checkBarcode(binding.edtRecalcEnter.text.toString())
            binding.rbtRecalcPLU.id -> checkPLU()
        }
    }

    private fun checkBarcode(str: String) {
        val prefixWeight = Common.selectedShop.shopPrefixWeight
        val prefixWeightPLU = Common.selectedShop.shopPrefixWeightPLU
        val prefixSingle = Common.selectedShop.shopPrefixPiece

        var barcode = str.padStart(13, '0').takeLast(13)
        println(barcode)
        println("------------------------")
        if (barcode.first().toString() == "2") { //Весовой товар с кодом
            var weight = ""
            val prefix = barcode.take(2)

            when (prefix) {
                prefixWeight -> {
                    if (isEAN13(barcode)) {
                        val productCode = barcode.takeLast(11).take(5)
                        val productWeight = barcode.takeLast(6).take(5)
                        println("Код товара: $productCode Вес товара: $productWeight")
                        //TODO 	СтруктураРеквизитовНоменклатуры = НайтиНоменклатуруПоКоду(КодТовара);
                        val found = true
                        if (found && productWeight.isNotEmpty()) {

                            val kg = productWeight.take(2).toInt()
                            val gr = productWeight.takeLast(3).toInt()
                            val weight = "$kg,$gr"
                            println("Вес $weight")
// 01,408 -> 1,408
                        } else if (productWeight.isNotEmpty()) {

                        }
                    }
                }
                prefixWeightPLU -> {
                    if (isEAN13(barcode)) {
                        val productPLU = barcode.takeLast(11).take(5)
                        val productWeight = barcode.takeLast(6).take(5)
                        if (productPLU.toIntOrNull() == null) {
                            FancyToast.makeText(
                                applicationContext,
                                "Некорректный PLU! $productPLU",
                                FancyToast.LENGTH_LONG,
                                FancyToast.WARNING,
                                false
                            ).show()
                        } else {
                            val kg = productWeight.take(2).toInt()
                            val gr = productWeight.takeLast(3).toInt()
                            val weight = "$kg,$gr"
                            println("Вес $weight")
                            binding.edtCount.setText(weight)
                        }
                    }
                }
                prefixSingle -> {
                    if (isEAN13(barcode)) {
                        val productCode = barcode.substring(2, 9)
                        if (productCode.toIntOrNull() == null) {
                            FancyToast.makeText(
                                applicationContext,
                                "Некорректный код! $productCode",
                                FancyToast.LENGTH_LONG,
                                FancyToast.WARNING,
                                false
                            ).show()
                        } else {
                            binding.edtCount.setText(productCode)
                        }
                    }
                }

            }
        } else {
            //Весовой товар с ПЛУ
            println("Весовой товар с ПЛУ")
        }
        val countString = binding.edtCount.text.trim()
        val code = countString.toString()
        if (binding.edtRecalcBarcode.text.isEmpty()) {
            alert("Указанный штрих-код не найден, продолжить ввод?", code)
        } else {
            insert(code)
        }
    }

    private fun checkPLU() {
        val countString = binding.edtCount.text.trim()
        if (TextUtils.isEmpty(countString)) {
            FancyToast.makeText(
                applicationContext,
                "Пустое количество",
                FancyToast.LENGTH_LONG,
                FancyToast.WARNING,
                false
            ).show()
            return
        }
        val plu = countString.toString()
        with(binding) {
            if (edtRecalcBarcode.text.isEmpty() &&
                edtRecalcCode.text.isEmpty() &&
                edtRecalcPLU.text.isEmpty()
            ) {
                alert("Указанный PLU-код не найден, продолжить ввод?", plu)
            } else {
                insert(plu)
            }
        }
    }

    private fun checkCode() {
        val countString = binding.edtCount.text.trim()
        if (TextUtils.isEmpty(countString)) {
            FancyToast.makeText(
                applicationContext,
                "Пустое количество",
                FancyToast.LENGTH_LONG,
                FancyToast.WARNING,
                false
            ).show()
            return
        }
        val countInt = countString.toString().toIntOrNull()
        if (countInt == null) {
            FancyToast.makeText(
                applicationContext,
                "Количество не являеся числом",
                FancyToast.LENGTH_LONG,
                FancyToast.WARNING,
                false
            ).show()
            return
        }
        val code = countString.toString()
        if (binding.edtRecalcCode.text.isEmpty()) {
            alert("Указанный код не найден, продолжить ввод?", code)
        } else {
            insert(code)
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

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
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

    private fun sendInventory(list: ArrayList<InvItem>) {
        if (list.count() == 0) {
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

    @Suppress("UNUSED_PARAMETER")
    fun sendTo1C(view: View) {
        val builderAlert = AlertDialog.Builder(this)
        with(builderAlert) {
            setTitle("Внимание")
            setMessage("Выгрузить документ в программу?")
            setPositiveButton("Да") { _: DialogInterface, _: Int ->
                list = arrayListOf()
                val invDao: InvDao = db!!.invDao()
                invDao.selectAll().observe(this@RecalcActivity) { items ->
                    list = items as ArrayList<InvItem>
                    sendInventory(list)
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
        binding.edtRecalcEnter.setOnFocusChangeListener { _, _ -> hideKeyboard() }
        binding.edtRecalcEnter.setOnClickListener { hideKeyboard() }
        binding.edtRecalcEnter
            .textChanges()
            .distinctUntilChanged()
            .filterNot { it.isNullOrBlank() }
            .debounce(100)
            .flatMapLatest { searchProductFlow(it.toString()) }
            .onEach { populate(it) }
            .launchIn(lifecycleScope)
    }

    private fun insert(count: String) {
        val inv = InvItem("", "", "", "", "")
        with(binding) {
            with(inv) {
                barcode = edtRecalcBarcode.text.toString()
                code = edtRecalcCode.text.toString()
                name = edtRecalcGood.text.toString()
                plu = edtRecalcPLU.text.toString()
                quantity = count
            }

            when (binding.rgRecalc.checkedRadioButtonId) {
                binding.rbtRecalcCode.id -> inv.code = edtRecalcEnter.text.toString()
                binding.rbtRecalcBarcode.id -> inv.barcode = edtRecalcEnter.text.toString()
                binding.rbtRecalcPLU.id -> inv.plu = edtRecalcEnter.text.toString()
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
                    clearFields(true)
                }

            }
        }
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

    private fun alert(message: String, count: String) {
        val builder = AlertDialog.Builder(this)
        with(builder) {
            setTitle("Внимание")
            setMessage(message)
            setPositiveButton("Да") { _: DialogInterface, _: Int ->
                insert(count)
            }
            setNegativeButton(
                "Нет"
            ) { _: DialogInterface, _: Int ->
                clearFields(true)

            }
            show()
        }
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

    private fun isBarcodeSelected(): Boolean {
        with(binding) {
            return rgRecalc.checkedRadioButtonId == rbtRecalcBarcode.id
        }
    }

    private fun isCodeSelected(): Boolean {
        with(binding) {
            return rgRecalc.checkedRadioButtonId == rbtRecalcCode.id
        }
    }

    private fun isPLUSelected(): Boolean {
        with(binding) {
            return rgRecalc.checkedRadioButtonId == rbtRecalcPLU.id
        }
    }

    private fun getWeight(barcode: String): String {
        val productWeight = barcode.takeLast(6).take(5)
        val kg = productWeight.take(2).toInt()
        val gr = productWeight.takeLast(3).toInt()
        return "$kg.$gr".toFloat().toString().replace('.', ',')
    }

    private fun populate(item: NomenclatureItem?) = if (item != null) {
        with(binding) {
            edtRecalcCode.setText(item.code)
            edtRecalcPLU.setText(item.plu)
            edtRecalcBarcode.setText(item.barcode)
            edtRecalcGood.setText(item.name)
            edtRecalcPrice.setText(item.price)
            if (isBarcodeSelected()) {
                var barcode = edtRecalcBarcode.text.toString().padStart(13, '0').takeLast(13)
                if (barcode.first().toString() == "2") {
                    val prefixWeight = Common.selectedShop.shopPrefixWeight
                    val prefixWeightPLU = Common.selectedShop.shopPrefixWeightPLU
                    val prefixSingle = Common.selectedShop.shopPrefixPiece
                    val prefix = barcode.take(2)
                    when (prefix) {
                        prefixSingle -> {
                            if (isEAN13(barcode)) {
                                val productCode = barcode.substring(2, 9)
                                if (productCode.toIntOrNull() == null) {
                                    FancyToast.makeText(
                                        applicationContext,
                                        "Некорректный код! $productCode",
                                        FancyToast.LENGTH_LONG,
                                        FancyToast.WARNING,
                                        false
                                    ).show()
                                } else {
                                    edtCount.setText(productCode)
                                }
                            }
                        }
                        prefixWeight -> {
                            if (isEAN13(barcode)) {
                                val productCode = barcode.takeLast(11).take(5)
                                val weight = getWeight(barcode)
                                println("Код товара: $productCode Вес товара: $weight")
                                edtCount.setText(weight)
                            } else {
                                edtCount.setText("0")
                            }
                        }
                        prefixWeightPLU -> {
                            if (isEAN13(barcode)) {
                                val productPLU = barcode.takeLast(11).take(5)
                                if (productPLU.toIntOrNull() == null) {
                                    FancyToast.makeText(
                                        applicationContext,
                                        "Некорректный PLU! $productPLU",
                                        FancyToast.LENGTH_LONG,
                                        FancyToast.WARNING,
                                        false
                                    ).show()
                                } else {
                                    val weight = getWeight(barcode)
                                    println("PLU товара: $productPLU Вес товара: $weight")
                                    edtCount.setText(weight)
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        clearFields()
    }

    private fun clearFields(doClearCodeField: Boolean = false) {
        with(binding) {
            edtRecalcCode.setText("")
            edtRecalcPLU.setText("")
            edtRecalcBarcode.setText("")
            edtRecalcGood.setText("")
            edtRecalcPrice.setText("")
            edtCount.setText("")
            if (doClearCodeField) {
                edtRecalcEnter.setText("")
            }
        }
    }
}
