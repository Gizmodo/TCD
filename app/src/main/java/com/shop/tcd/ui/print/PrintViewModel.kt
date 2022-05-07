package com.shop.tcd.ui.print

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.utils.Constants.SelectedObjects.ShopModel
import com.shop.tcd.data.dto.pricetag.BarcodeTag
import com.shop.tcd.data.dto.pricetag.PriceTag
import com.shop.tcd.data.dto.pricetag.response.PriceTagResponse
import com.shop.tcd.data.dto.pricetag.response.PriceTagResponseItem
import com.shop.tcd.data.dto.printer.PrintersList
import com.shop.tcd.data.repository.Repository
import kotlinx.coroutines.*
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class PrintViewModel : ViewModel() {
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


    private var _printersLiveData = MutableLiveData<PrintersList>()
    val printersLiveData: LiveData<PrintersList>
        get() = _printersLiveData

    private var _printerPayload = MutableLiveData<List<String>>()
    val printerPayloadLiveData: LiveData<List<String>>
        get() = _printerPayload

    private var job: Job? = null
    private val context = App.applicationContext() as Application
    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .app(AppModule)
        .nm(NetworkModule)
        .dbm(DataBaseModule(context))
        .datastore(DataStoreModule)
        .build()

    init {
        injector.inject(this)
        loadPrinters()
    }

    @Inject
    lateinit var repository: Repository

    fun loadPrintInfoByBarcodes(list: MutableList<String>) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            when (val response: NetworkResult<PriceTagResponse> =
                repository.getPrintInfoByBarcodes(converterToPriceTag(list))) {
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
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            when (val response = repository.printers()) {
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
        return "SIZE 41.5 mm, 66.1 mm\n" +
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
        val basePointFont12 = 250
        val basePointFont10 = 252
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
            "TEXT 296," + getWidth(12,
                tag.string1).toString() + ",\"ROBOTOR.TTF\",90,12,12,\"${tag.string1}\"\n"
        } else {
            ""
        }
        val string2: String = if (tag.string2.isNotEmpty()) {
            "TEXT 258," + getWidth(10,
                tag.string2).toString() + ",\"ROBOTOR.TTF\",90,10,10,\"${tag.string2}\"\n"
        } else {
            ""
        }
        val string3: String = if (tag.string3.isNotEmpty()) {
            "TEXT 228," + getWidth(10,
                tag.string3).toString() + ",\"ROBOTOR.TTF\",90,10,10,\"${tag.string3}\"\n"
        } else {
            ""
        }
        val string4: String = if (tag.string4.isNotEmpty()) {
            "TEXT 197," + getWidth(10,
                tag.string4).toString() + ",\"ROBOTOR.TTF\",90,10,10,\"${tag.string4}\"\n"
        } else {
            ""
        }
        val plu: String = if (tag.plu != 0) {
            " (${tag.plu})"
        } else {
            ""
        }
        val nodiscount: String = if (tag.nodiscount) {
            "TEXT 321,265,\"ROBOTOR.TTF\",90,6,6,\"Скидка не распространяется\"\n"
        } else {
            ""
        }
        if (discount) {
            val oldPricePosition = (125 - (price[0].length - 1) * 29).toString()
            val newPricePosition = (405 - (stock[0].length - 1) * 49).toString()
            response = "CLS\n" +
                    "CODEPAGE UTF-8\n" +
                    string1 +
                    string2 +
                    string3 +
                    "TEXT 162,24,\"ROBOTOR.TTF\",90,6,6,\"${tag.manufacturer}\"\n" +
                    "TEXT 49,25,\"ROBOTOR.TTF\",90,6,6,\"${tag.barcode}\"\n" +
                    "TEXT 49,273,\"ROBOTOR.TTF\",90,6,6,\"${tag.code}" + plu + "\"\n" +
                    "TEXT 49,430,\"ROBOTOR.TTF\",90,6,6,\"${formatted}\"\n" +
                    "TEXT 127," + newPricePosition + ",\"ROBOTOB.TTF\",90,30,30,\"${stock[0]}\"\n" +
                    "TEXT 127,456,\"ROBOTOR.TTF\",90,13,12,\"${stock[1]}\"\n" +
                    "BAR 90,456, 2, 42\n" +
                    "BAR 324,6, 3, 507\n" +
                    "BAR 23,510, 300, 3\n" +
                    "BAR 21,6, 3, 507\n" +
                    "BAR 23,6, 300, 3\n" +
                    "BAR 141,8, 3, 504\n" +
                    "BAR 300,8, 3, 503\n" +
                    "DIAGONAL 67,29,125,245,2\n" +
                    string4 +
                    "TEXT 108," + oldPricePosition + ",\"ROBOTOR.TTF\",90,18,18,\"${price[0]}\"\n" +
                    "TEXT 121,160,\"ROBOTOR.TTF\",90,10,10,\"${price[1]}\"\n" +
                    "BAR 90,160, 1, 32\n" +
                    nodiscount +
                    "PRINT 1,1\n"
        } else {
            val pricePosition = (286 - (price[0].length - 1) * 50).toString()
            response = "CLS\n" +
                    "CODEPAGE UTF-8\n" +
                    string1 +
                    string2 +
                    string3 +
                    "TEXT 162,24,\"ROBOTOR.TTF\",90,6,6,\"${tag.manufacturer}\"\n" +
                    "TEXT 49,25,\"ROBOTOR.TTF\",90,6,6,\"${tag.barcode}\"\n" +
                    "TEXT 49,273,\"ROBOTOR.TTF\",90,6,6,\"${tag.code}" + plu + "\"\n" +
                    "TEXT 49,430,\"ROBOTOR.TTF\",90,6,6,\"${formatted}\"\n" +
                    "TEXT 127," + pricePosition + ",\"ROBOTOB.TTF\",90,30,30,\"${price[0]}\"\n" +
                    "TEXT 127,344,\"ROBOTOR.TTF\",90,13,12,\"${price[1]}\"\n" +
                    "BAR 90,344, 2, 42\n" +
                    "BAR 324,6, 3, 507\n" +
                    "BAR 23,510, 300, 3\n" +
                    "BAR 21,6, 3, 507\n" +
                    "BAR 23,6, 300, 3\n" +
                    "BAR 141,8, 3, 504\n" +
                    "BAR 300,8, 3, 503\n" +
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
        job?.cancel()
    }
}