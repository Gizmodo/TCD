package com.shop.tcd.ui.catalog

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.data.dto.nomenclature.NomenclatureItem
import com.shop.tcd.data.repository.Repository
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class CatalogViewModel : ViewModel() {
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
    }

    @Inject
    lateinit var repository: Repository

    fun loadNomenclatureFull() {
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            when (val response = repository.getNomenclatureFull()) {
                is NetworkResult.Error -> {
                    onError("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException(response.e)
                }
                is NetworkResult.Success -> {
                    val nomenclatureList =
                        response.data.nomenclature as ArrayList<NomenclatureItem>
                    nomenclatureList.forEach { item ->
                        item.plu = item.plu.replace(("[^\\d.]").toRegex(), "")
                    }
                    Timber.d("Загружено объектов ${nomenclatureList.size}")
                    repository.deleteAllNomenclature()
                    repository.insertNomenclature(nomenclatureList)
                }
            }
            _loading.postValue(false)
        }
    }

    fun loadNomenclatureRemainders() {
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            when (val response = repository.getNomenclatureRemainders()) {
                is NetworkResult.Error -> {
                    onError("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException(response.e)
                }
                is NetworkResult.Success -> {
                    val nomenclatureList =
                        response.data.nomenclature as ArrayList<NomenclatureItem>
                    nomenclatureList.forEach { item ->
                        item.plu = item.plu.replace(("[^\\d.]").toRegex(), "")
                    }
                    Timber.d("Загружено объектов ${nomenclatureList.size}")
                    repository.deleteAllNomenclature()
                    repository.insertNomenclature(nomenclatureList)
                }
            }
            _loading.postValue(false)
        }
    }

    fun loadNomenclatureByPeriod(period: String) {
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            when (val response = repository.getNomenclatureByPeriod(period)) {
                is NetworkResult.Error -> {
                    onError("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException(response.e)
                }
                is NetworkResult.Success -> {
                    val nomenclatureList =
                        response.data.nomenclature as ArrayList<NomenclatureItem>
                    nomenclatureList.forEach { item ->
                        item.plu = item.plu.replace(("[^\\d.]").toRegex(), "")
                    }
                    Timber.d("Загружено объектов ${nomenclatureList.size}")
                    repository.deleteAllNomenclature()
                    repository.insertNomenclature(nomenclatureList)
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
        job.cancel()
    }
}