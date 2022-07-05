package com.shop.tcd.ui.refund

import android.app.Application
import android.content.IntentFilter
import androidx.core.view.MotionEventCompat
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
import com.shop.tcd.core.utils.Constants
import com.shop.tcd.core.utils.ReceiverLiveData
import com.shop.tcd.data.dto.datamatrix.Goods
import com.shop.tcd.data.dto.refund.request.RefundRequestBody
import com.shop.tcd.data.remote.ShopRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RefundViewModel : ViewModel() {

    private var _idataScanner = MutableLiveData<String>()
    val idataScanner: LiveData<String> get() = _idataScanner

    private var _datamatrix = MutableLiveData<Goods>()
    val datamatrix: LiveData<Goods> get() = _datamatrix
    private var _datamatrixRaw: String = ""
    private var _barcode = MutableLiveData<String>()
    val barcode: LiveData<String> get() = _barcode

    private var _pdf417 = MutableLiveData<String>()
    val pdf417: LiveData<String> get() = _pdf417

    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _exceptionMessage = MutableLiveData<String>()
    val exceptionMessage: LiveData<String> get() = _exceptionMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onException(throwable)
    }
    private var _refundResponse = MutableLiveData<String>()
    val refundResponse: LiveData<String> get() = _refundResponse

    private var job: Job = Job()
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
    lateinit var shopRepository: ShopRepository

    fun cancelCurrentJob() {
        job.cancel()
    }

    fun GetSymbType(_type: Int): String? {
        return when (_type) {
            MotionEventCompat.AXIS_GENERIC_15 -> "DotCode"
            60 -> "Code 32"
            67 -> "CanadaPost"
            68 -> "EAN-8"
            69 -> "UPC-E"
            70 -> "IATA2of5"
            72 -> "Han Xin"
            73 -> "EAN-128"
            80 -> "PostNet"
            82 -> "MicroPDF417"
            88 -> "Grid Matrix"
            89 -> "COMPOSITE"
            97 -> "Codabar"
            98 -> "Code 39"
            99 -> "UPC-A"
            100 -> "EAN-13"
            101 -> "Interleaved 2 of 5"
            102 -> "standerd2of5"
            103 -> "MSI"
            104 -> "Code 11"
            105 -> "Code 93"
            106 -> "Code 128"
            108 -> "Code 49"
            109 -> "Matrix2of5"
            113 -> "CodablockF"
            114 -> "PDF417"
            115 -> "QR Code"
            119 -> "Data Matrix"
            120 -> "MaxiCode"
            121 -> "Rss"
            122 -> "Aztec"
            else -> "Undefined"
        }
    }

    private fun initDeviceObservables() {
        _idataScanner = ReceiverLiveData(
            context,
            IntentFilter("android.intent.action.SCANRESULT")
        ) { _, intent ->
            var data = ""
            var type: Int = 0
            val extras = intent?.extras?.keySet()?.map { "$it: ${intent.extras?.get(it)}" }
                ?.joinToString { it }
            Timber.d(extras.toString())
            intent.extras?.let {
                data = it["value"].toString()
                type = it["type"] as Int
            }
            parseCode(data, type)
            data
        }
    }

    private fun parseCode(data: String, type: Int) {
        Timber.d("${GetSymbType(type)} -> $data")
        when (type) {
            68 -> {
                _barcode.postValue(data.padStart(13, '0'))
            }
            100 -> {
                _barcode.postValue(data)
            }
            119 -> {
                when (data.length) {
                    29 -> {
                        Timber.d("Табак, Пачка")
                        val tobacco = Goods().apply {
                            code = data.take(14)
                            serial = data.substring(14).take(7)
                            mrc = data.substring(14 + 7).take(4)
                            checkCode = data.substring(14 + 7 + 4).take(4)
                        }
                        _datamatrix.postValue(tobacco)
                    }
                    31 -> {
                        Timber.d("Молочная продукция")
                        val milk = Goods().apply {
                            code = data.substring(2).take(14)
                            serial = data.substring(2 + 14 + 2).take(6)
                            checkCode = data.substring(14 + 7 + 4 + 2).take(4)
                        }
                        _datamatrix.postValue(milk)
                    }
                    38 -> {
                        Timber.d("Пиво, Антисептики, БАД, Упакованная вода")
                        val water = Goods().apply {
                            code = data.substring(2).take(14)
                            serial = data.substring(2 + 14 + 2).take(14)
                            checkCode = data.substring(2 + 14 + 2 + 14 + 2).take(4)
                        }
                        _datamatrix.postValue(water)
                    }
                    42 -> {
                        Timber.d("Молочная продукция с весом")
                        val milk = Goods().apply {
                            code = data.substring(2).take(14)
                            serial = data.substring(2 + 14 + 2).take(6)
                            checkCode = data.substring(2 + 14 + 2 + 6 + 3).take(5)
                            weight = data.substring(2 + 14 + 2 + 6 + 3 + 9).take(6)
                        }
                        _datamatrix.postValue(milk)
                    }
                    85 -> {
                        Timber.d("Велосипеды, Лекарства, Шины и покрышки, Духи")
                        val bicycle = Goods().apply {
                            code = data.substring(2).take(14)
                            serial = data.substring(2 + 14 + 2).take(14)
                            idCheckCode = data.substring(2 + 14 + 2 + 14 + 2).take(5)
                            checkCode = data.substring(2 + 14 + 2 + 14 + 5 + 2 + 2).take(44)
                        }
                        _datamatrix.postValue(bicycle)
                    }
                    92 -> {
                        Timber.d("Фото")
                        val photo = Goods().apply {
                            code = data.substring(2).take(14)
                            serial = data.substring(2 + 14 + 2).take(21)
                            idCheckCode = data.substring(2 + 14 + 2 + 21 + 2).take(5)
                            checkCode = data.substring(2 + 14 + 2 + 14 + 5 + 2 + 2 + 7).take(44)
                        }
                        _datamatrix.postValue(photo)
                    }
                    129 -> {
                        Timber.d("Обувь, Легпром")
                        val shoes = Goods().apply {
                            code = data.substring(2).take(14)
                            serial = data.substring(2 + 14 + 2).take(14)
                            idCheckCode = data.substring(2 + 14 + 2 + 14 + 2).take(5)
                            checkCode = data.substring(2 + 14 + 2 + 14 + 2 + 5 + 2).take(88)
                        }
                        _datamatrix.postValue(shoes)
                    }
                    78 -> {
                        Timber.d("Консервы")
                        val conserv = Goods().apply {
                            code = data.substring(2).take(14)
                            serial = data.substring(2 + 14 + 2).take(7)
                            idCheckCode = data.substring(2 + 14 + 2 + 7 + 2).take(5)
                            checkCode = data.substring(2 + 14 + 2 + 7 + 2 + 5 + 2).take(44)
                        }
                        _datamatrix.postValue(conserv)
                    }
                    else -> {
                        Timber.e("Unknown DataMatrix")
                    }
                }
                _datamatrixRaw = data
            }
            82, 114 -> {
                _pdf417.postValue(data)
            }
        }
    }

    fun send() {
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            val payload = RefundRequestBody(
                doctype = "refund",
                prefix = Constants.SelectedObjects.ShopModel.prefix,
                barcode = _barcode.value,
                datamatrix = _datamatrix.value,
                datamatrixRaw = _datamatrixRaw,
                pdf417 = _pdf417.value
            )
            when (val response = shopRepository.getRefund(payload = payload)) {
                is NetworkResult.Error -> {
                    onError("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException(response.e)
                }
                is NetworkResult.Success -> {
                    _refundResponse.postValue(response.data)
                }
            }
            _loading.postValue(false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

    private fun onError(message: String) {
        Timber.e(message)
        _errorMessage.postValue(message)
        _loading.postValue(false)
    }

    private fun onException(throwable: Throwable) {
        Timber.e(throwable)
        _exceptionMessage.postValue(throwable.message)
    }
}
