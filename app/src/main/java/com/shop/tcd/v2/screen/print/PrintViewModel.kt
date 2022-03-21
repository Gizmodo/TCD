package com.shop.tcd.v2.screen.print

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.v2.core.di.*
import com.shop.tcd.v2.core.utils.Common
import com.shop.tcd.v2.data.pricetag.BarcodeTag
import com.shop.tcd.v2.data.pricetag.PriceTag
import com.shop.tcd.v2.data.pricetag.response.PriceTagResponse
import com.shop.tcd.v2.data.printer.PrintersList
import com.shop.tcd.v2.domain.rest.SettingsApi
import com.shop.tcd.v2.domain.rest.ShopApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class PrintViewModel : ViewModel() {
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private var _printersLiveData = MutableLiveData<PrintersList>()
    val printersLiveData: LiveData<PrintersList>
        get() = _printersLiveData

    private var _priceTagsLiveData = MutableLiveData<PriceTagResponse>()
    val priceTagsLiveData: LiveData<PriceTagResponse>
        get() = _priceTagsLiveData

    private var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }
    private val context = App.applicationContext() as Application
    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .app(AppModule(context))
        .nm(NetworkModule)
        .dbm(DataBaseModule(context))
        .datastore(DataStoreModule)
        .build()

    init {
        injector.inject(this)
    }

    @Inject
    lateinit var settingsApi: SettingsApi

    @Inject
    lateinit var shopApi: ShopApi

    fun loadPrinters() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = settingsApi.getPrinters()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    _printersLiveData.postValue(response.body())
                } else {
                    onError("Error : ${response.message()} ")
                }
                _loading.value = false
            }
        }
    }

    private fun converterToPriceTag(list: MutableList<String>): PriceTag {
        val barcodeList: MutableList<BarcodeTag> = mutableListOf()
        list.mapTo(barcodeList) { BarcodeTag(it) }
        return PriceTag(
            Common.selectedShopModel.prefix,
            barcodesList = barcodeList
        )
    }

    fun loadPriceTagsObservable(list: MutableList<String>) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val priceTag = converterToPriceTag(list)
            val response = shopApi.postPriceTag(priceTag)
            withContext(Dispatchers.Main) {
                response.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _loading.value = false
                        if (it.isSuccessful) {
                            _priceTagsLiveData.postValue(it.body())
                        } else {
                            Timber.d("Not successfully")
                        }
                    }, {
                        onError("Error : $it")
                    })
            }
        }
    }

    private fun onError(message: String) {
        _errorMessage.postValue(message)
        _loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }

    /* private fun test() {
         homeApi.getUsers().observeOn(AndroidSchedulers.mainThread())
             .subscribe({
                 _usersLiveData.value = it
             }, {
                 val item: InvItem = InvItem("code", "name", "PLU", "2216017008221", "100500")
                 viewModelScope.launch {
                     invDao.insert(item)
                 }
                 Timber.e(it)
             })
     }*/
}