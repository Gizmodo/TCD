package com.shop.tcd.ui.nomenclature

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.data.dto.nomenclature.NomenclatureItem
import com.shop.tcd.data.repository.Repository
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class NomenclatureViewModel : ViewModel() {
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

    private var _nomenclatureLiveData = MutableLiveData<List<NomenclatureItem>>()
    val nomenclatureLiveData: LiveData<List<NomenclatureItem>> get() = _nomenclatureLiveData

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
        loadNomenclature()
    }

    @Inject
    lateinit var repository: Repository

    fun loadNomenclature() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response: List<NomenclatureItem> = repository.getAll() //nomenclatureDao.getAll()
            _nomenclatureLiveData.postValue(response)
        }
    }

    fun loadNomenclatureBySearch(search: String) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response: List<NomenclatureItem> = repository.getNomenclatureBySearch(search)
            _nomenclatureLiveData.postValue(response)
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

    private fun onSuccess(message: String) {
        _successMessage.postValue(message)
        _loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}