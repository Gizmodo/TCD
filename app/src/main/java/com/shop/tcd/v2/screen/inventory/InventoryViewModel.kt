package com.shop.tcd.v2.screen.inventory

import android.app.Application
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shop.tcd.App
import com.shop.tcd.model.InvItem
import com.shop.tcd.v2.core.di.*
import com.shop.tcd.v2.core.utils.ReceiverLiveData
import com.shop.tcd.v2.core.utils.SingleLiveEvent
import com.shop.tcd.v2.core.utils.StatefulData
import com.shop.tcd.v2.data.dao.InventoryDao
import com.shop.tcd.v2.data.dao.NomenclatureDao
import com.shop.tcd.v2.data.inventory.InventoryPair
import com.shop.tcd.v2.data.inventory.InventoryResult
import com.shop.tcd.v2.data.nomenclature.NomenclatureItem
import com.shop.tcd.v2.domain.rest.ShopApi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
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
        .nm(NetworkModule)
        .datastore(DataStoreModule)
        .build()

    init {
        injector.inject(this)
        loadInventoryList()
        initDeviceObservables()
    }

    private var _items = SingleLiveEvent<List<InvItem>>()
    fun getInventarisationItems(): SingleLiveEvent<List<InvItem>> = _items

    @Inject
    lateinit var inventoryDao: InventoryDao

    @Inject
    lateinit var nomenclatureDao: NomenclatureDao

    @Inject
    lateinit var shopAPI: ShopApi

    fun clearInventory() {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            inventoryDao.deleteAll()
            _inventoryList.postValue(inventoryDao.loadInventoryGrouped())
        }
    }

    fun fetchInventarisationItems() {
        viewModelScope.launch {
            try {
                _items.postValue(inventoryDao.selectAllSuspend())
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun postInventory(inventoryResult: InventoryResult) {
        _loading.value = true
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = shopAPI.postInventory("", inventoryResult)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) inventoryDao.deleteAll() else onError("Данные не отправлены")
                _loading.value = false
            }
        }
    }

    fun searchProduct(inputString: String): Flow<StatefulData<InventoryPair>> {
        var previousItem: InvItem?
        var currentItem: NomenclatureItem?

        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            previousItem = inventoryDao.selectInventoryItemByCode(inputString)
            currentItem = nomenclatureDao.selectNomenclatureItemByCode(inputString)
        /*    val res = flow {
                emit(StatefulData.Success(InventoryPair(currentItem, previousItem)))
            }*/
        }
        runBlocking {
            val job: Job = launch(context = Dispatchers.Default) {
                println("[${Thread.currentThread().name}] Launched coroutine")
                delay(100)
                println("[${Thread.currentThread().name}] Finished coroutine")
            }
            println("[${Thread.currentThread().name}] Created coroutine")
            job.join()
            println("[${Thread.currentThread().name}] Finished coroutine")
            10
        }
        job?.join()
        return flow {
            emit(StatefulData.Success(InventoryPair(
                currentItem = currentItem,
                previousItem = previousItem
            )))
        }
//        val previousItem = inventoryDao.selectInventoryItemByCode(inputString)
//        val currentItem = nomenclatureDao.selectNomenclatureItemByCode(inputString)

//        return res
    }
    /* fun searchProduct(inputString: String): Flow<StatefulData<InventoryPair>> {
         val len = inputString.length
         when {
             len <= CODE_LENGTH -> {
                 // TODO: Добавить поиск по этому же коду среди таблицы Inventory
                 val previousItem = inventoryDao.selectInventoryItemByCode(inputString)
                 val currentItem = nomenclatureDao.selectNomenclatureItemByCode(inputString)
                 return StatefulData.Success(InventoryPair(currentItem, previousItem))
             }
             else -> {
                 *//*val barcode = inputString.padStart(BARCODE_LENGTH, '0')
                val productCode =
                    barcode.takeLast(BARCODE_LENGTH_WO_PREFIX).take(BARCODE_LENGTH_WEIGHT_SUFFIX)
                return when (productCode == selectedShopModel.prefixWeight) {
                    true -> {
                        // TODO: Добавить поиск по этому же коду среди таблицы Inventory
                        nomenclatureDao.getByCode(productCode).asFlow()
                    }
                    false -> {
                        // TODO: Добавить поиск по этому же штрихкоду среди таблицы Inventory
                        nomenclatureDao.getByBarcode(barcode).asFlow()
                    }
                }*//*
            }
        }
    }*/

    private fun initDeviceObservables() {
        _urovoScanner = ReceiverLiveData<String>(
            context,
            IntentFilter("android.intent.ACTION_DECODE_DATA")
        ) { _, intent ->
            var data = ""
            intent.extras?.let { data = it["barcode_string"].toString() }
            return@ReceiverLiveData data
        }

        _urovoKeyboard = ReceiverLiveData(
            context,
            IntentFilter("android.intent.action_keyboard")
        ) { _, intent ->
            var data = false
            intent.extras?.let {
                data = it["kbrd_enter"].toString() == "enter"
            }
            return@ReceiverLiveData data
        }

        _idataScanner = ReceiverLiveData(
            context,
            IntentFilter("android.intent.action.SCANRESULT")
        ) { _, intent ->
            var data = ""
            intent.extras?.let { data = it["value"].toString() }
            return@ReceiverLiveData data
        }
    }

    fun loadInventoryList() {
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

    fun insertInventory(inv: InvItem) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            inventoryDao.insert(inv)
            val response: List<InvItem> = inventoryDao.loadInventoryGrouped()
            _inventoryList.postValue(response)
            _loading.value = false
        }
    }
}