package com.shop.tcd.v2.screen.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.v2.di.DaggerViewModelInjector
import com.shop.tcd.v2.di.DataBaseModule
import com.shop.tcd.v2.di.NetworkModule
import com.shop.tcd.v2.di.ViewModelInjector
import com.shop.tcd.model.newsettigs.UserListItem
import com.shop.tcd.repository.network.settings.SettingsApi
import com.shop.tcd.room.dao.InvDao
import com.shop.tcd.v2.data.shop.ShopsList
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import javax.inject.Inject

class MainViewModel : ViewModel() {
    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .networkModule(NetworkModule())
        .build()

    init {
        injector.inject(this)
    }

    @Inject
    lateinit var settingsApi: SettingsApi

    private var _shopsLiveData = MutableLiveData<ShopsList>()
    val shopsLiveData: LiveData<ShopsList>
        get() = _shopsLiveData

    val errorMessage = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()
    var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }

    fun loadShopsSuspend() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = settingsApi.getShopsSuspend()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    _shopsLiveData.postValue(response.body())
                    loading.value = false
                } else {
                    onError("Error : ${response.message()} ")
                }
            }
        }
    }

    private fun onError(message: String) {
        errorMessage.postValue(message)
        loading.postValue(true)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}