package com.shop.tcd.ui.remains

import android.app.Application
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.utils.Constants
import com.shop.tcd.core.utils.ReceiverLiveData
import com.shop.tcd.data.dto.remains.request.RemainsBarcodeFieldRequest
import com.shop.tcd.data.dto.remains.request.RemainsRequestBody
import com.shop.tcd.data.dto.remains.response.RemainsResponse
import com.shop.tcd.data.remote.ShopRepository
import com.shop.tcd.data.repository.Repository
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class RemainsViewModel : ViewModel() {
    /**
     * Сотояния для UI
     **/
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _exceptionMessage = MutableLiveData<String>()
    val exceptionMessage: LiveData<String> get() = _exceptionMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

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

    private var _remains = MutableLiveData<RemainsResponse>()
    val remainsLiveData: LiveData<RemainsResponse>
        get() = _remains

    private var job: Job? = null
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
        initDeviceObservables()
    }

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var shopRepository: ShopRepository

    private fun initDeviceObservables() {
        _urovoScanner = ReceiverLiveData(
            context,
            IntentFilter("android.intent.ACTION_DECODE_DATA")
        ) { _, intent ->
            var data = ""
            intent.extras?.let { data = it["barcode_string"].toString() }
            data
        }

        _urovoKeyboard =
            ReceiverLiveData(
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

    private fun convertToRemainsRequestBody(list: MutableList<String>): RemainsRequestBody {
        val barcodeList: MutableList<RemainsBarcodeFieldRequest> = mutableListOf()
        list.mapTo(barcodeList) { RemainsBarcodeFieldRequest(it) }
        return RemainsRequestBody(Constants.SelectedObjects.ShopModel.prefix, barcodeList)
    }

    fun loadRemainInfoByBarcodes(list: MutableList<String>) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            when (val response: NetworkResult<RemainsResponse> =
                shopRepository.getRemains(convertToRemainsRequestBody(list))) {
                is NetworkResult.Error -> {
                    onError("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException(response.e)
                }
                is NetworkResult.Success -> {
                    val payload = response.data
                    _remains.postValue(payload)
                }
            }
            _loading.postValue(false)
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
}