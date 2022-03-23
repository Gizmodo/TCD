package com.shop.tcd.v2.screen.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.v2.core.di.*
import com.shop.tcd.v2.data.shop.ShopsList
import com.shop.tcd.v2.domain.rest.SettingsApi
import kotlinx.coroutines.*
import javax.inject.Inject

class MainViewModel : ViewModel() {
    private val context = App.applicationContext() as Application
    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .app(AppModule(context))
        .nm(NetworkModule)
        .dbm(DataBaseModule(context))
        .dbh(DataSourceModule)
        .build()

    // TODO:  Сделать DataBaseModule без context как DataSourceModule
    init {
        injector.inject(this)
    }
//@Inject lateinit var dbhelpr: DatabaseHelper


    @Inject
    lateinit var settingsApi: SettingsApi

    private var _shopsLiveData = MutableLiveData<ShopsList>()
    val shopsLiveData: LiveData<ShopsList>
        get() = _shopsLiveData

    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }

    fun loadShops() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = settingsApi.getShopsSuspend()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    _shopsLiveData.postValue(response.body())
                    _loading.value = false
                } else {
                    onError("Error : ${response.message()} ")
                }
            }
        }
    }

    private fun onError(message: String) {
        _errorMessage.postValue(message)
        _loading.postValue(true)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}