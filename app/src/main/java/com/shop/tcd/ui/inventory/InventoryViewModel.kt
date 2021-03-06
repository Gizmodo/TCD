package com.shop.tcd.ui.inventory

import android.app.Application
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.AppModule
import com.shop.tcd.core.di.DaggerViewModelInjector
import com.shop.tcd.core.di.DataBaseModule
import com.shop.tcd.core.di.DataStoreModule
import com.shop.tcd.core.di.NetworkModule
import com.shop.tcd.core.di.ViewModelInjector
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
import com.shop.tcd.core.utils.SingleLiveEvent
import com.shop.tcd.core.utils.StatefulData
import com.shop.tcd.data.dto.inventory.InvItem
import com.shop.tcd.data.dto.inventory.InventoryPair
import com.shop.tcd.data.dto.inventory.InventoryResult
import com.shop.tcd.data.dto.nomenclature.NomenclatureItem
import com.shop.tcd.data.remote.ShopRepository
import com.shop.tcd.data.repository.Repository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class InventoryViewModel : ViewModel() {
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _exceptionMessage = MutableLiveData<String>()
    val exceptionMessage: LiveData<String> get() = _exceptionMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private var _successMessage = SingleLiveEvent<String>()
    val successMessage: SingleLiveEvent<String> get() = _successMessage

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onException(throwable)
    }

    private var _idataScanner = MutableLiveData<String>()
    val idataScanner: LiveData<String> get() = _idataScanner

    private var _inventoryList = MutableLiveData<List<InvItem>>()
    val inventoryList: LiveData<List<InvItem>> get() = _inventoryList

    private var job: Job = Job()
    private val context = App.applicationContext() as Application

    private val injector: ViewModelInjector =
        DaggerViewModelInjector.builder().app(AppModule(context)).dbm(DataBaseModule(context))
            .nm(NetworkModule).datastore(DataStoreModule).build()

    init {
        injector.inject(this)
        loadInventoryList()
        initDeviceObservables()
    }

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var shopRepository: ShopRepository

    fun cancelCurrentJob() {
        job.cancel()
    }

    fun clearInventory() {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            repository.deleteAllInventory()
            _inventoryList.postValue(repository.loadInventoryGrouped())
        }
    }

    fun sendResults() {
        _loading.value = true
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            val list = repository.selectAllSuspend()
            if (list.isNotEmpty()) {
                val inventoryResult = InventoryResult(
                    result = "success",
                    message = "",
                    operation = "revision",
                    autor = Constants.SelectedObjects.UserModel.name,
                    shop = Constants.SelectedObjects.ShopModel.name,
                    prefix = Constants.SelectedObjects.ShopModel.prefix,
                    document = list
                )
                when (val response = shopRepository.postInventory(inventoryResult)) {
                    is NetworkResult.Error -> {
                        onError("${response.code} ${response.message}")
                    }
                    is NetworkResult.Exception -> {
                        onException(response.e)
                    }
                    is NetworkResult.Success -> {
                        Timber.d("???????????? ?????????????? ????????????????????")
                        repository.deleteAllInventory()
                        loadInventoryList()
                        _successMessage.postValue("???????????? ?????????????? ????????????????????")
                    }
                }
                _loading.postValue(false)
            }
        }
    }

    suspend fun searchProduct(productString: String): Flow<StatefulData<InventoryPair>> {
        val len = productString.length
        var previousItem: String? = ""
        var currentItem: NomenclatureItem? = NomenclatureItem("", "", "", "", "")

        when {
            len <= CODE_LENGTH -> {
                job.cancel()
                job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                    previousItem = repository.selectInventoryItemByCode(productString)
                    currentItem = repository.selectNomenclatureItemByCode(productString)
                }
                job.join()
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
                        Timber.d("?????????? ???? ??????????????")
                        when (shopTemplate.searchType) {
                            SearchType.SearchByCode -> {
                                searchString = searchString.toInt().toString()
                                Timber.d("?????????? ???? ???????? $searchString")
                                job.cancel()
                                job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                                    previousItem =
                                        repository.selectInventoryItemByCode(searchString)
                                    currentItem =
                                        repository.selectNomenclatureItemByCode(
                                            searchString
                                        )
                                }
                                job.join()
                                return searchResult(currentItem, previousItem)
                            }
                            SearchType.SearchByPLU -> {
                                Timber.d("?????????? ???? PLU $searchString")
                                job.cancel()
                                job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                                    previousItem =
                                        repository.selectInventoryItemByPLUCode(searchString)
                                    currentItem =
                                        repository.selectNomenclatureItemByPLUCode(
                                            searchString
                                        )
                                }
                                job.join()
                                return searchResult(currentItem, previousItem)
                            }
                            else -> {
                                Timber.e("???? ???????????? ???????????????? ????????")
                                return searchResult(currentItem, previousItem)
                            }
                        }
                    }
                    false -> {
                        Timber.d("?????????? ???? ??????????????????")
                        val barcode =
                            productString.padStart(BARCODE_LENGTH, '0').takeLast(BARCODE_LENGTH)
                        job.cancel()
                        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                            previousItem = repository.selectInventoryItemByBarcode(barcode)
                            currentItem =
                                repository.selectNomenclatureItemByBarcode(barcode)
                        }
                        job.join()
                        return searchResult(currentItem, previousItem)
                    }
                }
            }
        }
    }

    private fun searchResult(
        currentItem: NomenclatureItem?,
        previousItem: String?,
    ) = flow {
        if (currentItem == null && previousItem == null) {
            emit(StatefulData.Error("?????????? ?????????????????????? ?? ????????????????????????"))
        } else {
            emit(
                StatefulData.Success(
                    InventoryPair(
                        currentItem = currentItem,
                        previousItem = previousItem
                    )
                )
            )
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
        return (barcode.length == BARCODE_LENGTH) && (
                checksumDigit.toString() == barcode.last()
                    .toString()
                )
    }

    private fun getWeight(barcode: String): String {
        val productWeight = barcode.substring(
            shopTemplate.weightPosition.first,
            shopTemplate.weightPosition.second
        )
        val kg = productWeight.take(2)
        val gr = productWeight.takeLast(3)
        return "$kg.$gr".toFloat().toString().replace('.', ',')
    }

    private fun isWeightProduct(barcode: String, code: String?): Boolean {
        var paddedCode: String = code.toString()
        code?.let {
            paddedCode = it.padStart(5, '0')
        }

        when {
            barcode.length.equals(BARCODE_LENGTH) && shopTemplate.prefix.equals(barcode.take(2)) && paddedCode.equals(
                barcode.substring(
                    shopTemplate.infoPosition.first,
                    shopTemplate.infoPosition.second
                )
            ) -> {
                return true
            }
            else -> {
                return false
            }
        }
    }

    fun parseBarcode(item: NomenclatureItem?, barcode: String): StatefulData<String> {
        var result: StatefulData<String> = StatefulData.Error("")
        item.notNull {
            Timber.d("parseBarcode $barcode")
            when {
                isEAN13(barcode) -> {
                    if (isWeightProduct(barcode, item?.code)) {
                        result = StatefulData.Success(getWeight(barcode))
                    } else {
                        result = StatefulData.Notify("?????????????????? ??????????")
                    }
                }
                else -> {
                    result = StatefulData.Error("???? ???? ????????????")
                }
            }
        }
        return result
    }

    private fun initDeviceObservables() {
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
        job.cancel()
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
        job.cancel()
    }

    fun insertInventory(inv: InvItem) {
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            repository.insertInventory(inv)
            val response: List<InvItem> = repository.loadInventoryGrouped()
            _inventoryList.postValue(response)
        }
    }
}
