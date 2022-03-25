package com.shop.tcd.v2.screen.inventory

import android.app.Application
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import com.shop.tcd.App
import com.shop.tcd.broadcast.ReceiverLiveData
import com.shop.tcd.model.InvItem
import com.shop.tcd.v2.core.di.*
import com.shop.tcd.v2.core.utils.Common
import com.shop.tcd.v2.core.utils.Common.currentSearchMode
import com.shop.tcd.v2.core.utils.Common.selectedShopModel
import com.shop.tcd.v2.core.utils.Constants
import com.shop.tcd.v2.data.nomenclature.NomenclatureItem
import com.shop.tcd.v2.domain.database.InvDao
import com.shop.tcd.v2.domain.database.NomenclatureDao
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InventoryViewModel : ViewModel() {
    /***
     * Наблюдатели для терминалов
     */
    private var _urovoScanner = MutableLiveData<String>()
    val urovoScanner: LiveData<String> get() = _urovoScanner

    private var _idataScanner = MutableLiveData<String>()
    val idataScanner: LiveData<String> get() = _idataScanner

    private var _urovoKeyboard = MutableLiveData<Boolean>()
    val urovoKeyboard: LiveData<Boolean> get() = _urovoKeyboard

    /***
     * Наблюдатели за результатом поиска товара
     */
    private var _productByCode = MutableLiveData<NomenclatureItem>()
    val productByCode: LiveData<NomenclatureItem> get() = _productByCode

    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private var _inventoryList = MutableLiveData<List<InvItem>>()
    val inventoryList: LiveData<List<InvItem>> get() = _inventoryList

    private var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Возникло исключение: ${throwable.localizedMessage}")
    }
    private val context = App.applicationContext() as Application

    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .app(AppModule(context))
        .dbm(DataBaseModule(context))
        .datastore(DataStoreModule)
        .build()

    init {
        injector.inject(this)
        loadInventoryList()
        initDeviceObservables()
    }

    @Inject
    lateinit var inventoryDao: InvDao

    @Inject
    lateinit var nomenclatureDao: NomenclatureDao

    fun getProduct(data: String): Flow<NomenclatureItem> {
        val prefix = data.take(2)
        when (currentSearchMode) {
            Common.SEARCHBY.BARCODE -> {
                return if (prefix == selectedShopModel.prefixWeight) {
                    val productCode = data.takeLast(11).take(5)
                    nomenclatureDao.getByCode(productCode).asFlow()
                } else {
                    nomenclatureDao.getByBarcode(data).asFlow()
                }
            }
            Common.SEARCHBY.CODE -> {
                return nomenclatureDao.getByCode(data).asFlow()
            }
        }
    }

    private fun initDeviceObservables() {
        _urovoScanner = ReceiverLiveData<String>(context,
            IntentFilter("android.intent.ACTION_DECODE_DATA")
        ) { _, intent ->
            var data = ""
            intent.extras?.let {
                data = it["barcode_string"].toString()
                data = "0000000000000$data".takeLast(Constants.Inventory.BARCODE_LENGTH)
            }
            return@ReceiverLiveData data
        }

        _urovoKeyboard = ReceiverLiveData(context,
            IntentFilter("android.intent.action_keyboard")
        ) { _, intent ->
            var data = false
            intent.extras?.let {
                data = it["kbrd_enter"].toString() == "enter"
            }
            return@ReceiverLiveData data
        }

        _idataScanner = ReceiverLiveData(context,
            IntentFilter("android.intent.action.SCANRESULT")
        ) { _, intent ->
            var data = ""
            intent.extras?.let { data = it["value"].toString() }
            return@ReceiverLiveData data
        }
    }

    private fun loadInventoryList() {
        _loading.value = true
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response: List<InvItem> = inventoryDao.loadInventoryGrouped()
            _inventoryList.postValue(response)
            _loading.value = false
        }
    }

    private fun onError(message: String) {
        _errorMessage.postValue(message)
        _loading.postValue(false)
    }

    private fun onSuccess(message: String) {
        _loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}