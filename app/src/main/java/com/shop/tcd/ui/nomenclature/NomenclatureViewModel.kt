package com.shop.tcd.ui.nomenclature

import android.app.Application
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
import com.shop.tcd.data.dto.nomenclature.NomenclatureItem
import com.shop.tcd.data.repository.Repository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class NomenclatureViewModel : ViewModel() {
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _exceptionMessage = MutableLiveData<String>()
    val exceptionMessage: LiveData<String> get() = _exceptionMessage

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onException(throwable)
    }

    private var _nomenclatureLiveData = MutableLiveData<List<NomenclatureItem>>()
    val nomenclatureLiveData: LiveData<List<NomenclatureItem>> get() = _nomenclatureLiveData

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
        loadNomenclature()
    }

    @Inject
    lateinit var repository: Repository

    fun clearNomenclature() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            repository.deleteAllNomenclature()
            loadNomenclature()
        }
    }

    fun loadNomenclature() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response: List<NomenclatureItem> = repository.getAll()
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

    private fun onException(throwable: Throwable) {
        Timber.e(throwable)
        _exceptionMessage.postValue(throwable.message)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}
