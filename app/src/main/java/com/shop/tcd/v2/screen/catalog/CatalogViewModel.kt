package com.shop.tcd.v2.screen.catalog

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.v2.data.nomenclature.NomenclatureItem
import com.shop.tcd.v2.core.di.*
import com.shop.tcd.v2.domain.database.NomenclatureDao
import com.shop.tcd.v2.domain.rest.ShopApi
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class CatalogViewModel : ViewModel() {
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> get() = _successMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private var job: Job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Возникло исключение: ${throwable.localizedMessage}")
    }
    private val context = App.applicationContext() as Application
    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .app(AppModule(context))
        .nm(NetworkModule)
        .dbm(DataBaseModule(context))
        .build()

    init {
        injector.inject(this)
    }

    @Inject
    lateinit var nomenclatureDao: NomenclatureDao

    @Inject
    lateinit var shopAPI: ShopApi

    fun loadNomenclatureFull() {
        Timber.d("Загрузка полного списка")
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = shopAPI.getNomenclatureFull()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    if (response.body()?.result.equals("success", false)) {
                        val nomenclatureList =
                            response.body()?.nomenclature as ArrayList<NomenclatureItem>
                        onSuccess("Загружено объектов ${nomenclatureList.size}")
                        nomenclatureDao.deleteAll()
                        nomenclatureDao.insertNomenclature(nomenclatureList)
                    } else {
                        onError("Данные не получены: ${response.body()?.message}")
                    }
                    _loading.value = false
                } else {
                    onError("Ошибка выполенения запроса : ${response.message()} ")
                }
            }
        }
    }

    fun loadNomenclatureRemainders() {
        Timber.d("Загрузка остатков")
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = shopAPI.getNomenclatureRemainders()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    if (response.body()?.result.equals("success", false)) {
                        val nomenclatureList =
                            response.body()?.nomenclature as ArrayList<NomenclatureItem>
                        onSuccess("Загружено объектов ${nomenclatureList.size}")
                        nomenclatureDao.deleteAll()
                        nomenclatureDao.insertNomenclature(nomenclatureList)
                    } else {
                        onError("Данные не получены: ${response.body()?.message.toString()}")
                    }
                    _loading.value = false
                } else {
                    onError("Ошибка выполенения запроса : ${response.message()} ")
                }
            }
        }
    }

    fun loadNomenclatureByPeriod(period: String) {
        Timber.d("Загрузка за период")
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = shopAPI.getNomenclatureByPeriod(period)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    if (response.body()?.result.equals("success", false)) {
                        val nomenclatureList =
                            response.body()?.nomenclature as ArrayList<NomenclatureItem>
                        onSuccess("Загружено объектов ${nomenclatureList.size}")
                        nomenclatureDao.deleteAll()
                        nomenclatureDao.insertNomenclature(nomenclatureList)
                    } else {
                        onError("Данные не получены: ${response.body()?.message.toString()}")
                    }
                    _loading.value = false
                } else {
                    onError("Ошибка выполенения запроса : ${response.message()} ")
                }
            }
        }
    }

    private fun onSuccess(message: String) {
        _successMessage.postValue(message)
        _loading.postValue(false)
    }

    private fun onError(message: String) {
        _errorMessage.postValue(message)
        _loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}