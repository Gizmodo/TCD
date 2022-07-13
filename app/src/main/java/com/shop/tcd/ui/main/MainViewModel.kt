package com.shop.tcd.ui.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.AppModule
import com.shop.tcd.core.di.DaggerViewModelInjector
import com.shop.tcd.core.di.DataBaseModule
import com.shop.tcd.core.di.DataSourceModule
import com.shop.tcd.core.di.NetworkModule
import com.shop.tcd.core.di.ViewModelInjector
import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.utils.StatefulData
import com.shop.tcd.data.dto.shop.ShopsList
import com.shop.tcd.data.remote.SettingsRepository
import com.shop.tcd.data.repository.Repository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MainViewModel : ViewModel() {
    private var _exceptionMessage = MutableLiveData<String>()
    val exceptionMessage: LiveData<String> get() = _exceptionMessage

    private var _state = MutableStateFlow<StatefulData<ShopsList>>(StatefulData.Loading)
    val state: StateFlow<StatefulData<ShopsList>> get() = _state

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onException(throwable)
    }

    var job: Job? = null
    private val context = App.applicationContext() as Application

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

    @Inject
    lateinit var repository: Repository

    fun clearNomenclature() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            repository.deleteAllNomenclature()
        }
    }

    private fun loadShops() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _state.value = StatefulData.Loading
            when (val response = settingsRepository.shops()) {
                is NetworkResult.Error -> {
                    _state.value = StatefulData.Notify("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException(response.e)
                }
                is NetworkResult.Success -> {
                    _state.value = StatefulData.Success(response.data)
                }
            }
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
