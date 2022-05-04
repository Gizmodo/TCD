package com.shop.tcd.screen.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.data.repository.SettingsRepository
import com.shop.tcd.data.shop.ShopsList
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class MainViewModel : ViewModel() {
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
        onException(throwable.localizedMessage)
    }

    private var _shopsLiveData = MutableLiveData<ShopsList>()
    val shopsLiveData: LiveData<ShopsList> get() = _shopsLiveData

    var job: Job? = null
    private val context = App.applicationContext() as Application

    // TODO:  Сделать DataBaseModule без context как DataSourceModule
    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .app(AppModule(context))
        .nm(NetworkModule)
        .dbm(DataBaseModule(context))
        .dbh(DataSourceModule)
        .build()

    init {
        injector.inject(this)
        loadShops()
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private fun loadShops() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            when (val response = settingsRepository.shops()) {
                is NetworkResult.Error -> {
                    onError("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException("${response.e.message}")
                }
                is NetworkResult.Success -> {
                    _shopsLiveData.postValue(response.data)
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

    private fun onException(message: String) {
        Timber.e(message)
        _exceptionMessage.postValue(message)
        _loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}