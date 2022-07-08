package com.shop.tcd.ui.print

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
import com.shop.tcd.core.utils.Constants.SelectedObjects.ShopModel
import com.shop.tcd.core.utils.ReceiverLiveData
import com.shop.tcd.data.dto.pricetag.BarcodeTag
import com.shop.tcd.data.dto.pricetag.PriceTag
import com.shop.tcd.data.dto.pricetag.response.PriceTagResponse
import com.shop.tcd.data.dto.pricetag.response.PriceTagResponseItem
import com.shop.tcd.data.dto.printer.PrintersList
import com.shop.tcd.data.remote.SettingsRepository
import com.shop.tcd.data.remote.ShopRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class PrintViewModel : ViewModel() {
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _exceptionMessage = MutableLiveData<String>()
    val exceptionMessage: LiveData<String> get() = _exceptionMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onException(throwable)
    }

    private var _idataScanner = MutableLiveData<String>()
    val idataScanner: LiveData<String> get() = _idataScanner

    private var _printersLiveData = MutableLiveData<PrintersList>()
    val printersLiveData: LiveData<PrintersList>
        get() = _printersLiveData

    private var _printerPayload = MutableLiveData<List<String>>()
    val printerPayloadLiveData: LiveData<List<String>>
        get() = _printerPayload

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
        loadPrinters()
        initDeviceObservables()
    }

    @Inject
    lateinit var repository: SettingsRepository

    @Inject
    lateinit var shopRepository: ShopRepository

    fun cancelCurrentJob() {
        job.cancel()
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

    fun loadPrintInfoByBarcodes(list: MutableList<String>) {
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            when (
                val response: NetworkResult<PriceTagResponse> =
                    shopRepository.getPrintInfoByBarcodes(converterToPriceTag(list))
            ) {
                is NetworkResult.Error -> {
                    onError("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException(response.e)
                }
                is NetworkResult.Success -> {
                    val payload: PriceTagResponse = response.data
                    _printerPayload.postValue(createTSPLRequest(payload))
                }
            }
            _loading.postValue(false)
        }
    }

    private fun loadPrinters() {
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            when (val response = repository.printers(ShopModel.prefix)) {
                is NetworkResult.Error -> {
                    onError("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException(response.e)
                }
                is NetworkResult.Success -> {
                    _printersLiveData.postValue(response.data)
                }
            }
            _loading.postValue(false)
        }
    }

    private fun converterToPriceTag(list: MutableList<String>): PriceTag {
        val barcodeList: MutableList<BarcodeTag> = mutableListOf()
        list.mapTo(barcodeList) { BarcodeTag(it) }
        return PriceTag(ShopModel.prefix, barcodeList)
    }

    private fun createTSPLRequest(body: PriceTagResponse?): List<String> {
        val printerJob = mutableListOf<String>()
        var task: String
        body?.let {
            printerJob.add(priceTagHeader())
            for (tag in body.filter { it.found == true }) {
                task = priceTagBody(!tag.stock.equals(0f), tag)
                printerJob.add(task)
            }
        }
        return printerJob
    }

    private fun priceTagHeader(): String {
        return "SIZE 55 mm, 39 mm\n" +
                "DIRECTION 0,0\n" +
                "REFERENCE 0,0\n" +
                "OFFSET 0 mm\n" +
                "SET PEEL OFF\n" +
                "SET CUTTER OFF\n" +
                "SET TEAR ON\n"
    }

    private fun getWidth(fontSize: Int, string: String): Int {
        // %./(-\)*,+!'=`№&°:?[]
        var position = 0
        val basePointFont12 = 202
        val basePointFont10 = 204
        when (fontSize) {
            12 -> {
                string.forEach {
                    if (it.isLetterOrDigit() || it == '+' || it == '=') {
                        position += 10
                    } else if (it.isWhitespace() || it == '.' || it == '-' || it == '!' || it == ':' || it == '[' || it == ']'
                    ) {
                        position += 5
                    } else if (it == '%') {
                        position += 13
                    } else if (it == '/' || it == '\\' || it == '*') {
                        position += 8
                    } else if (it == '(' || it == ')' || it == '`') {
                        position += 6
                    } else if (it == ',') {
                        position += 4
                    } else if (it == '\'') {
                        position += 3
                    } else if (it == '№') {
                        position += 18
                    } else if (it == '&') {
                        position += 11
                    } else if (it == '°') {
                        position += 7
                    } else if (it == '?') {
                        position += 9
                    }
                }
                return basePointFont12 - position
            }
            10 -> {
                string.forEach {
                    if (it.isLetterOrDigit() || it == '+' || it == '=') {
                        position += 8
                    } else if (it.isWhitespace() || it == '.' || it == '-' || it == '!' || it == ':' || it == '[' || it == ']'
                    ) {
                        position += 4
                    } else if (it == '%') {
                        position += 11
                    } else if (it == '/' || it == '\\' || it == '*') {
                        position += 6
                    } else if (it == '(' || it == ')' || it == '`') {
                        position += 5
                    } else if (it == ',') {
                        position += 3
                    } else if (it == '\'') {
                        position += 3
                    } else if (it == '№') {
                        position += 15
                    } else if (it == '&') {
                        position += 9
                    } else if (it == '°') {
                        position += 5
                    } else if (it == '?') {
                        position += 7
                    }
                }
                return basePointFont10 - position
            }
            else -> {
                return basePointFont12
            }
        }
    }

    private fun priceTagBody(discount: Boolean, tag: PriceTagResponseItem): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val formatted = current.format(formatter)

        val response: String
        val price = "%.2f".format(tag.price).replace(',', '.').split(".")
        val stock = "%.2f".format(tag.stock).replace(',', '.').split(".")

        val string1: String = if (tag.string1.isNotEmpty()) {
            "TEXT " + getWidth(
                12,
                tag.string1
            ).toString() + ", 26, \"ROBOTOR.TTF\",0,12,12,\"${tag.string1}\"\n"
        } else {
            ""
        }
        val string2: String = if (tag.string2.isNotEmpty()) {
            "TEXT " + getWidth(
                10,
                tag.string2
            ).toString() + ", 66, \"ROBOTOR.TTF\",0,10,10,\"${tag.string2}\"\n"
        } else {
            ""
        }
        val string3: String = if (tag.string3.isNotEmpty()) {
            "TEXT " + getWidth(
                10,
                tag.string3
            ).toString() + ", 105, \"ROBOTOR.TTF\",0,10,10,\"${tag.string3}\"\n"
        } else {
            ""
        }
        val string4: String = if (tag.string4.isNotEmpty()) {
            "TEXT " + getWidth(
                10,
                tag.string4
            ).toString() + ", 145, \"ROBOTOR.TTF\",0,10,10,\"${tag.string4}\"\n"
        } else {
            ""
        }
        val plu: String = if (tag.plu != 0) {
            " (${tag.plu})"
        } else {
            ""
        }
        val nodiscount: String = if (tag.nodiscount) {
            "TEXT 194,5,\"ROBOTOR.TTF\",0,6,6,\"Скидка не распространяется\"\n"
        } else {
            ""
        }
        if (discount) {
            val oldPricePosition = (109 - (price[0].length - 1) * 29).toString()
            val newPricePosition = (327 - (stock[0].length - 1) * 49).toString()
            response = "CLS\n" +
                    "CODEPAGE UTF-8\n" +
                    string1 +
                    string2 +
                    string3 +
                    "TEXT 6,170,\"ROBOTOR.TTF\",0,6,6,\"${tag.manufacturer}\"\n" +
                    "TEXT 6,291,\"ROBOTOR.TTF\",0,6,6,\"${tag.barcode}\"\n" +
                    "TEXT 218,291,\"ROBOTOR.TTF\",0,6,6,\"${tag.code}" + plu + "\"\n" +
                    "TEXT 340,291,\"ROBOTOR.TTF\",0,6,6,\"${formatted}\"\n" +
                    "TEXT " + newPricePosition + ", 209,\"ROBOTOB.TTF\",0,30,30,\"${stock[0]}\"\n" +
                    "TEXT 377,198,\"ROBOTOR.TTF\",0,13,12,\"${stock[1]}\"\n" +
                    "BAR 0,0, 424, 3\n" +
                    "BAR 421,3, 3, 306\n" +
                    "BAR 0,309, 424, 3\n" +
                    "BAR 0,3, 3, 306\n" +
                    "BAR 0,187, 422, 3\n" +
                    "BAR 2,21, 420, 3\n" +
                    "BAR 144,243, 32, 1\n" +
                    "BAR 377,225, 42, 2\n" +
                    "DIAGONAL 15,278,176,230,2\n" +
                    string4 +
                    "TEXT " + oldPricePosition + ", 233,\"ROBOTOR.TTF\",0,18,18,\"${price[0]}\"\n" +
                    "TEXT 144,220,\"ROBOTOR.TTF\",0,10,10,\"${price[1]}\"\n" +
                    nodiscount +
                    "PRINT 1,1\n"
        } else {
            val pricePosition = (220 - (price[0].length - 1) * 50).toString()
            response = "CLS\n" +
                    "CODEPAGE UTF-8\n" +
                    string1 +
                    string2 +
                    string3 +
                    "TEXT 6,170,\"ROBOTOR.TTF\",0,6,6,\"${tag.manufacturer}\"\n" +
                    "TEXT 6,291,\"ROBOTOR.TTF\",0,6,6,\"${tag.barcode}\"\n" +
                    "TEXT 218,291,\"ROBOTOR.TTF\",0,6,6,\"${tag.code}" + plu + "\"\n" +
                    "TEXT 340,291,\"ROBOTOR.TTF\",0,6,6,\"${formatted}\"\n" +
                    "TEXT " + pricePosition + ", 209,\"ROBOTOB.TTF\",0,30,30,\"${price[0]}\"\n" +
                    "TEXT 276,198,\"ROBOTOR.TTF\",0,12,12,\"${price[1]}\"\n" +
                    "BAR 276,2225, 40, 2\n" +
                    "BAR 0,0, 424, 3\n" +
                    "BAR 421,3, 3, 306\n" +
                    "BAR 0,309, 424, 3\n" +
                    "BAR 0,3, 3, 306\n" +
                    "BAR 0,187, 422, 3\n" +
                    "BAR 2,21, 420, 3\n" +
                    string4 +
                    nodiscount +
                    "PRINT 1,1\n"
        }
        return response
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
}
