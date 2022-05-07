package com.shop.tcd.ui.inventory

import android.app.Application
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.extension.notNull
import com.shop.tcd.core.utils.Constants
import com.shop.tcd.core.utils.Constants.Inventory.BARCODE_LENGTH
import com.shop.tcd.core.utils.Constants.Inventory.BARCODE_LENGTH_PREFIX
import com.shop.tcd.core.utils.Constants.Inventory.BARCODE_LENGTH_WO_CRC
import com.shop.tcd.core.utils.Constants.Inventory.CODE_LENGTH
import com.shop.tcd.core.utils.Constants.SelectedObjects.shopTemplate
import com.shop.tcd.core.utils.ReceiverLiveData
import com.shop.tcd.core.utils.SearchType
import com.shop.tcd.core.utils.StatefulData
import com.shop.tcd.data.dto.inventory.InvItem
import com.shop.tcd.data.dto.inventory.InventoryPair
import com.shop.tcd.data.dto.inventory.InventoryResult
import com.shop.tcd.data.dto.nomenclature.NomenclatureItem
import com.shop.tcd.data.repository.Repository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class InventoryViewModel : ViewModel() {
    /**
     * Сотояния для UI
     **/
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _exceptionMessage = MutableLiveData<String>()
    val exceptionMessage: LiveData<String> get() = _exceptionMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private var _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> get() = _successMessage

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onException(throwable)
    }

    /**
     * Наблюдатели для терминалов
     */
    private var _urovoScanner = MutableLiveData<String>()
    val urovoScanner: LiveData<String> get() = _urovoScanner

    private var _idataScanner = MutableLiveData<String>()
    val idataScanner: LiveData<String> get() = _idataScanner

    private var _urovoKeyboard = MutableLiveData<Boolean>()
    val urovoKeyboard: LiveData<Boolean> get() = _urovoKeyboard

    private var _inventoryList = MutableLiveData<List<InvItem>>()
    val inventoryList: LiveData<List<InvItem>> get() = _inventoryList

    private var job: Job? = null
    private val context = App.applicationContext() as Application

    private val injector: ViewModelInjector =
        DaggerViewModelInjector.builder().app(AppModule).dbm(DataBaseModule(context))
            .nm(NetworkModule).datastore(DataStoreModule).build()

    init {
        injector.inject(this)
        loadInventoryList()
        initDeviceObservables()
    }

    @Inject
    lateinit var repository: Repository

    fun clearInventory() {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            repository.deleteAllInventory()
            _inventoryList.postValue(repository.loadInventoryGrouped())
        }
    }

    fun sendResults() {
        _loading.value = true
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            val list = repository.selectAllSuspend()
            if (list.isNotEmpty()) {
                val inventoryResult = InventoryResult(result = "success",
                    message = "",
                    operation = "revision",
                    autor = Constants.SelectedObjects.UserModel.name,
                    shop = Constants.SelectedObjects.ShopModel.name,
                    prefix = Constants.SelectedObjects.ShopModel.prefix,
                    document = list)
                when (val response = repository.postInventory(inventoryResult)) {
                    is NetworkResult.Error -> {
                        onError("${response.code} ${response.message}")
                    }
                    is NetworkResult.Exception -> {
                        onException(response.e)
                    }
                    is NetworkResult.Success -> {
                        Timber.d("Данные успешно отправлены")
                        repository.deleteAllInventory()
                        loadInventoryList()
                        _successMessage.postValue("Данные успешно отправлены")
                    }
                }
                _loading.postValue(false)
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
                    previousItem = repository.selectInventoryItemByCode(productString)
                    currentItem = repository.selectNomenclatureItemByCode(productString)
                }
                job?.join()
                return searchResult(currentItem, previousItem)
            }
            else -> {
                var searchString =
                    productString.padStart(BARCODE_LENGTH, '0').takeLast(BARCODE_LENGTH)
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
                                        repository.selectInventoryItemByCode(searchString)
                                    currentItem =
                                        repository.selectNomenclatureItemByCode(
                                            searchString)
                                }
                                job?.join()
                                return searchResult(currentItem, previousItem)
                            }
                            SearchType.SearchByPLU -> {
                                Timber.d("Поиск по PLU $searchString")
                                job?.cancel()
                                job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                                    previousItem =
                                        repository.selectInventoryItemByPLUCode(searchString)
                                    currentItem =
                                        repository.selectNomenclatureItemByPLUCode(
                                            searchString)
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
                        val barcode =
                            productString.padStart(BARCODE_LENGTH, '0').takeLast(BARCODE_LENGTH)
                        job?.cancel()
                        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                            previousItem = repository.selectInventoryItemByBarcode(barcode)
                            currentItem =
                                repository.selectNomenclatureItemByBarcode(barcode)
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
            emit(StatefulData.Success(InventoryPair(currentItem = currentItem,
                previousItem = previousItem)))
        }
    }

    private fun isEAN13(barcode: String): Boolean {
        var odd = 0
        var even = 0
        val radix = 10
        val barcode12 = barcode.take(BARCODE_LENGTH_WO_CRC)
        barcode12.forEachIndexed { index, c ->
            when {
                index % 2 == 0 -> odd += Character.digit(c, radix)
                else -> even += Character.digit(c, radix)
            }
        }
        val checksumDigit = ((10 - ((odd + 3 * even) % 10)) % 10)
        return (((barcode.length == BARCODE_LENGTH) && (checksumDigit.toString() == barcode.last()
            .toString())))
    }

    private fun getWeight(item: NomenclatureItem): String {
        val productWeight = item.barcode.substring(shopTemplate.weightPosition.first,
            shopTemplate.weightPosition.second)
        val kg = productWeight.take(2).toInt()
        val gr = productWeight.takeLast(3).toInt()
        return "$kg.$gr".toFloat().toString().replace('.', ',')
    }

    private fun isWeightProduct(item: NomenclatureItem): Boolean {
        val barcode = item.barcode
        when {
            barcode.length.equals(BARCODE_LENGTH) && shopTemplate.prefix.equals(barcode.take(2)) && item.code.equals(
                barcode.substring(shopTemplate.infoPosition.first,
                    shopTemplate.infoPosition.second)) -> {
                return true
            }
            else -> {
                return false
            }
        }
    }

    fun parseBarcode(item: NomenclatureItem?): StatefulData<String> {
        var result: StatefulData<String> = StatefulData.Error("")
        item.notNull {
            val barcode = it.barcode
            Timber.d("parseBarcode $barcode")
            when {
                isEAN13(barcode) -> {
                    if (isWeightProduct(it)) {
                        result = StatefulData.Success(getWeight(it))
                    } else {
                        result = StatefulData.Notify("Невесовой товар")
                    }
                }
                else -> {
                    result = StatefulData.Error("ШК не верный")
                }
            }
        }
        return result
    }

    private fun initDeviceObservables() {
        _urovoScanner = ReceiverLiveData(context,
            IntentFilter("android.intent.ACTION_DECODE_DATA")) { _, intent ->
            var data = ""
            intent.extras?.let { data = it["barcode_string"].toString() }
            data
        }

        _urovoKeyboard =
            ReceiverLiveData(context,
                IntentFilter("android.intent.action_keyboard")) { _, intent ->
                var data = false
                intent.extras?.let {
                    data = it["kbrd_enter"].toString() == "enter"
                }
                data
            }

        _idataScanner = ReceiverLiveData(context,
            IntentFilter("android.intent.action.SCANRESULT")) { _, intent ->
            var data = ""
            intent.extras?.let { data = it["value"].toString() }
            data
        }
    }

    fun loadInventoryList() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response: List<InvItem> = repository.loadInventoryGrouped()
            _inventoryList.postValue(response)
        }
    }

    private fun onError(message: String) {
        Timber.e(message)
        _errorMessage.postValue(message)
        _loading.postValue(false)
    }

    private fun onException(throwable: Throwable) {
        Timber.e(throwable)
        _exceptionMessage.postValue(throwable.message)
        _loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }

    fun insertInventory(inv: InvItem) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            repository.insertInventory(inv)
            val response: List<InvItem> = repository.loadInventoryGrouped()
            _inventoryList.postValue(response)
        }
    }
}