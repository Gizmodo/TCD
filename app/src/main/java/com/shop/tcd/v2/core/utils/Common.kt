package com.shop.tcd.v2.core.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.annotation.CheckResult
import com.shop.tcd.model.InvItem
import com.shop.tcd.model.settings.Shop
import com.shop.tcd.room.database.TCDRoomDatabase
import com.shop.tcd.v2.data.nomenclature.NomenclatureItem
import com.shop.tcd.v2.data.printer.Printer
import com.shop.tcd.v2.data.shop.ShopModel
import com.shop.tcd.v2.data.user.UserModel
import com.shop.tcd.v2.domain.database.InvDao
import com.shop.tcd.v2.domain.database.NomenclatureDao
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

object Common {
    const val OK_HTTP_TIMEOUT = 15L
    const val TCP_SERVICE_TCP_TIMEOUT_INT = 2000
    const val TCP_SERVICE_PORT = 9100
    const val TCP_SERVICE_DNS_TIMEOUT = 2000L
    const val TCP_SERVICE_THREAD_TIMEOUT = 2000L
    const val ANIMATION_TIMEOUT = 1000L
    const val ANIMATION_FROM_DEGREE = 0f
    const val ANIMATION_TO_DEGREE = 360f
    const val ANIMATION_PIVOT = 0.5f

    enum class MODESCAN(val modeScan: Int = 0) {
        AUTO(0),
        MANUAL(1)
    }

    enum class SEARCHBY(val modeSearch: Int = 0) {
        BARCODE(0),
        CODE(1)
    }

    var currentScanMode: MODESCAN = MODESCAN.AUTO
    var currentSearchMode: SEARCHBY = SEARCHBY.BARCODE

    var BASE_URL_LOCAL = "http://10.0.2.2/"
    var BASE_URL_MMTR_NODEJS = "http://10.254.1.230:3000/"
    var BASE_URL_HOME_NODEJS = "http://192.168.88.87:3000/"
    var BASE_URL_PRODUCTION = "http://192.168.0.154/"

    /**
     * Постоянный адрес с расположением файла настроек
     */
    var BASE_URL = BASE_URL_HOME_NODEJS

    /**
     * Изменяемый адрес, который указывает на выбранный магазин (сервер 1С)
     */
    var BASE_SHOP_URL = ""

    /**
     * Хранение выбранного магазина
     */
    lateinit var selectedShop: Shop
    lateinit var selectedShopModel: ShopModel
    var selectedShopModelPosition: Int = -1

    /**
     * Хранение выбранного пользователя и его позиции
     */
    lateinit var selectedUserModel: UserModel
    var selectedUserModelPosition: Int = -1

    /**
     * Хранение выбранного принтера и его позиции
     */
    lateinit var selectedPrinter: Printer
    var selectedPrinterPosition: Int = -1

    @DelicateCoroutinesApi
    suspend fun saveNomenclatureList(list: List<NomenclatureItem>, context: Context) {
        val nomenclatureDao: NomenclatureDao
        val databaseTCD: TCDRoomDatabase = TCDRoomDatabase.getDatabase(context)
        nomenclatureDao = databaseTCD.nomDao()
        GlobalScope.launch {
            nomenclatureDao.deleteAll()
            nomenclatureDao.insertNomenclature(list)
        }
    }

    @DelicateCoroutinesApi
    suspend fun insertInv(item: InvItem, context: Context) {
        val invDao: InvDao
        val databaseTCD: TCDRoomDatabase = TCDRoomDatabase.getDatabase(context)
        invDao = databaseTCD.invDao()
        GlobalScope.launch {
            invDao.insert(item)
        }
    }

    fun isEAN13(barcode: String): Boolean {
        var ch = 0
        var nch = 0
        val barcode12 = barcode.take(12)

        barcode12.forEachIndexed { index, c ->
            when {
                index % 2 == 0 -> ch += Character.digit(c, 10)
                else -> nch += Character.digit(c, 10)
            }
        }
        val checksumDigit = ((10 - (ch + 3 * nch) % 10) % 10)
        return (((barcode.length == 13) && (checksumDigit.toString() == barcode.last().toString())))
    }

    fun parseBarcode(barcode: String): ResponseState<String> {
        return if (barcode.first().toString() == "2") {
            val prefix = barcode.take(2)
            when (prefix) {
                selectedShop.shopPrefixPiece -> {
                    if (isEAN13(barcode)) {
                        val productCode = barcode.substring(2, 9)
                        if (productCode.toIntOrNull() == null) {
                            ResponseState.Error(InvalidBarcodeException("Код продукта не определен"))
                        } else {
                            // TODO:  edtCount.setText(productCode)
                            ResponseState.Success(productCode)
                        }
                    } else {
                        ResponseState.Error(InvalidBarcodeException("Штрихкод не EAN13"))
                    }
                }
                selectedShop.shopPrefixWeight -> {
                    if (isEAN13(barcode)) {
                        val productCode = barcode.takeLast(11).take(5)
                        val weight = getWeight(barcode)
                        Timber.d("Код товара: $productCode Вес товара: $weight")
                        // TODO: edtCount.setText(weight)
                        ResponseState.Success(weight)
                    } else {
                        // TODO: edtCount.setText("0")???
                        ResponseState.Error(InvalidBarcodeException("Штрихкод не EAN13"))
                    }
                }
                selectedShop.shopPrefixWeightPLU -> {
                    if (isEAN13(barcode)) {
                        val productPLU = barcode.takeLast(11).take(5)
                        if (productPLU.toIntOrNull() == null) {
                            ResponseState.Error(InvalidBarcodeException("Некорректный PLU! $productPLU"))
                        } else {
                            val weight = getWeight(barcode)
                            Timber.d("PLU товара: $productPLU Вес товара: $weight")
                            // TODO: edtCount.setText(weight)
                            ResponseState.Success(weight)
                        }
                    } else {
                        ResponseState.Error(InvalidBarcodeException("Штрихкод не EAN13"))
                    }
                }
                else -> {
                    ResponseState.Error(InvalidBarcodeException("Штрихкод не соответствует ни одному префиксу магазина"))
                }
            }
        } else {
            ResponseState.Error(InvalidBarcodeException("Первый символ не 2"))
        }
    }

    private fun getWeight(barcode: String): String {
        val productWeight = barcode.takeLast(6).take(5)
        val kg = productWeight.take(2).toInt()
        val gr = productWeight.takeLast(3).toInt()
        return "$kg.$gr".toFloat().toString().replace('.', ',')
    }

    @ExperimentalCoroutinesApi
    @CheckResult
    fun EditText.textChanges(): Flow<CharSequence?> {
        return callbackFlow {
            val listener = (object : TextWatcher {
                override fun afterTextChanged(s: Editable?) = Unit
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                    // TODO: Move method as a parameter
//                    hideKeyboard()
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {
                    trySend(s)
                }
            }).apply {
                addTextChangedListener(this)
            }
            awaitClose { removeTextChangedListener(listener) }
        }.onStart { emit(text) }
    }

    fun EditText.setReadOnly(value: Boolean) {
//        isFocusable = !value
        isEnabled = !value
//        isFocusableInTouchMode = !value
    }
}