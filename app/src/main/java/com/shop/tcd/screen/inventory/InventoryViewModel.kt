package com.shop.tcd.screen.inventory

import android.app.Application
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.core.utils.Constants.Inventory.BARCODE_LENGTH
import com.shop.tcd.core.utils.Constants.Inventory.BARCODE_LENGTH_PREFIX
import com.shop.tcd.core.utils.Constants.Inventory.BARCODE_LENGTH_WO_CRC
import com.shop.tcd.core.utils.Constants.Inventory.CODE_LENGTH
import com.shop.tcd.core.utils.Constants.SelectedObjects.shopTemplate
import com.shop.tcd.core.utils.ReceiverLiveData
import com.shop.tcd.core.utils.SingleLiveEvent
import com.shop.tcd.core.utils.StatefulData
import com.shop.tcd.data.dao.InventoryDao
import com.shop.tcd.data.dao.NomenclatureDao
import com.shop.tcd.data.inventory.InvItem
import com.shop.tcd.data.inventory.InventoryPair
import com.shop.tcd.data.inventory.InventoryResult
import com.shop.tcd.data.nomenclature.NomenclatureItem
import com.shop.tcd.domain.SearchType
import com.shop.tcd.domain.rest.ShopApi
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

    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

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
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = shopAPI.postInventory("", inventoryResult)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) inventoryDao.deleteAll() else onError("Данные не отправлены")
            }
        }
    }

    suspend fun searchProduct(productString: String): Flow<StatefulData<InventoryPair>> {
        val len = productString.length
        var previousItem: InvItem? = InvItem("", "", "", "", "")
        var currentItem: NomenclatureItem? = NomenclatureItem("", "", "", "", "")

        when {
            len <= CODE_LENGTH -> {
                job?.cancel()
                job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                    previousItem = inventoryDao.selectInventoryItemByCode(productString)
                    currentItem = nomenclatureDao.selectNomenclatureItemByCode(productString)
                }
                job?.join()
                return searchResult(currentItem, previousItem)
            }
            else -> {
                var searchString =
                    productString
                        .padStart(BARCODE_LENGTH, '0')
                        .takeLast(BARCODE_LENGTH)
                val productCode = searchString.take(BARCODE_LENGTH_PREFIX)

                searchString = searchString.substring(
                    shopTemplate.infoPosition.first,
                    shopTemplate.infoPosition.second,
                )

                when (productCode == shopTemplate.prefix) {
                    true -> {
                        Timber.d("Поиск по шаблону")
                        when (shopTemplate.searchType) {
                            SearchType.SearchByCode -> {
                                Timber.d("Поиск по коду $searchString")
                                job?.cancel()
                                job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                                    previousItem =
                                        inventoryDao.selectInventoryItemByCode(searchString)
                                    currentItem =
                                        nomenclatureDao.selectNomenclatureItemByCode(searchString)
                                }
                                job?.join()
                                return searchResult(currentItem, previousItem)
                            }
                            SearchType.SearchByPLU -> {
                                Timber.d("Поиск по PLU $searchString")
                                job?.cancel()
                                job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                                    previousItem =
                                        inventoryDao.selectInventoryItemByPLUCode(searchString)
                                    currentItem =
                                        nomenclatureDao.selectNomenclatureItemByPLUCode(searchString)
                                }
                                job?.join()
                                return searchResult(currentItem, previousItem)
                            }
                            else -> {
                                Timber.e("Не должно попадать сюда")
                                return searchResult(currentItem, previousItem)
                            }
                        }
                    }
                    false -> {
                        Timber.d("Поиск по штрихкоду")
                        val barcode = productString
                            .padStart(BARCODE_LENGTH, '0')
                            .takeLast(BARCODE_LENGTH)
                        job?.cancel()
                        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                            previousItem = inventoryDao.selectInventoryItemByBarcode(barcode)
                            currentItem =
                                nomenclatureDao.selectNomenclatureItemByBarcode(barcode)
                        }
                        job?.join()
                        return searchResult(currentItem, previousItem)
                    }
                }
            }
        }
    }

    private fun searchResult(
        currentItem: NomenclatureItem?,
        previousItem: InvItem?,
    ) = flow {
        if (currentItem == null && previousItem == null) {
            emit(StatefulData.Error("Товар отсутствует в номенклатуре"))
        } else {
            emit(StatefulData.Success(InventoryPair(
                currentItem = currentItem,
                previousItem = previousItem
            )))
        }
    }

    private fun isEAN13(barcode: String): Boolean {
        var ch = 0
        var nch = 0
        val radix = 10
        val barcode12 = barcode.take(BARCODE_LENGTH_WO_CRC)
        barcode12.forEachIndexed { index, c ->
            when {
                index % 2 == 0 -> ch += Character.digit(c, radix)
                else -> nch += Character.digit(c, radix)
            }
        }
        val checksumDigit = ((10 - (ch + 3 * nch) % 10) % 10)
        return (((barcode.length == BARCODE_LENGTH) && (checksumDigit.toString() == barcode.last()
            .toString())))
    }

    private fun getWeight(barcode: String): String {
        val productWeight = barcode.substring(
            shopTemplate.weightPosition.first,
            shopTemplate.weightPosition.second
        )
        val kg = productWeight.take(2).toInt()
        val gr = productWeight.takeLast(3).toInt()
        return "$kg.$gr".toFloat().toString().replace('.', ',')
    }

    fun parseBarcode(barcode: String): StatefulData<String> {
        Timber.d("parseBarcode $barcode")
        return when (isEAN13(barcode)) {
            false -> StatefulData.Error("ШК не верный")
            true -> StatefulData.Success(getWeight(barcode))
        }
    }

    private fun initDeviceObservables() {
        _urovoScanner = ReceiverLiveData(
            context,
            IntentFilter("android.intent.ACTION_DECODE_DATA")
        ) { _, intent ->
            var data = ""
            intent.extras?.let { data = it["barcode_string"].toString() }
            data
        }

        _urovoKeyboard = ReceiverLiveData(
            context,
            IntentFilter("android.intent.action_keyboard")
        ) { _, intent ->
            var data = false
            intent.extras?.let {
                data = it["kbrd_enter"].toString() == "enter"
            }
            data
        }

        _idataScanner = ReceiverLiveData(
            context,
            IntentFilter("android.intent.action.SCANRESULT")
        ) { _, intent ->
            var data = ""
            intent.extras?.let { data = it["value"].toString() }
            data
        }
    }

    fun loadInventoryList() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response: List<InvItem> = inventoryDao.loadInventoryGrouped()
            _inventoryList.postValue(response)
        }
    }

    private fun onError(message: String) {
        _errorMessage.postValue(message)
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
        }
    }
}