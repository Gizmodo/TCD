package com.shop.tcd.ui.options

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shop.tcd.App
import com.shop.tcd.BuildConfig
import com.shop.tcd.core.di.AppModule
import com.shop.tcd.core.di.DaggerViewModelInjector
import com.shop.tcd.core.di.DataBaseModule
import com.shop.tcd.core.di.DataStoreModule
import com.shop.tcd.core.di.NetworkModule
import com.shop.tcd.core.di.NetworkModule_ProvideOkHttpClientSettingsFactory
import com.shop.tcd.core.di.NetworkModule_ProvideRetrofitInterfaceFactory
import com.shop.tcd.core.di.NetworkModule_ProvideSettingsApiFactory
import com.shop.tcd.core.di.ViewModelInjector
import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.utils.Constants.DataStore.KEY_BASE_URL
import com.shop.tcd.core.utils.Constants.DataStore.KEY_URL_UPDATE_SERVER
import com.shop.tcd.core.utils.SingleLiveEvent
import com.shop.tcd.core.utils.StatefulData
import com.shop.tcd.core.utils.Util.isValidServerAddress
import com.shop.tcd.data.dto.ato.UpdateRequest
import com.shop.tcd.data.dto.ato.UpdateResponse
import com.shop.tcd.data.local.DataStoreRepository
import com.shop.tcd.data.remote.SettingsRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class OptionsViewModel : ViewModel() {
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onException(throwable)
    }

    private var _state = MutableStateFlow<StatefulData<UpdateResponse>>(StatefulData.Loading)
    val state: StateFlow<StatefulData<UpdateResponse>> get() = _state

    private var _url = SingleLiveEvent<String>()
    val url: SingleLiveEvent<String> get() = _url

    private var _urlUpdateServer = SingleLiveEvent<String>()
    val urlUpdateServer: SingleLiveEvent<String> get() = _urlUpdateServer

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
        loadSettings()
    }

    @Inject
    lateinit var ds: DataStoreRepository

    private fun loadSettings() {
        viewModelScope.launch(
            Dispatchers.IO + exceptionHandler
        ) {
            _url.postValue(ds.getString(KEY_BASE_URL))
            _urlUpdateServer.postValue(ds.getString(KEY_URL_UPDATE_SERVER))
        }
    }

    private fun onException(throwable: Throwable) {
        _state.value = StatefulData.Error(throwable.message.toString())
    }

    fun saveBaseUrl(url: String) {
        val isValid = url.isValidServerAddress()
        if (isValid) viewModelScope.launch {
            ds.putString(KEY_BASE_URL, url)
        }
    }

    fun saveUrlUpdateServer(url: String) {
        val isValid = url.isValidServerAddress()
        if (isValid) viewModelScope.launch {
            ds.putString(KEY_URL_UPDATE_SERVER, url)
        }
    }

    fun checkUpdate() {
        viewModelScope.launch(exceptionHandler) {
            // TODO: сделать еще один сетевой модуль для обращения к серверу обновлений.
            // TODO: В последствии предложить разрабочику бэкенда странслировать сетевые запросы/ответы через 1С. Тогда можно будет убрать сетевой модуль, отвечающий за опрос сервера обновлений.
            _state.value = StatefulData.Loading
            val urlUpdateServer = ds.getString(KEY_URL_UPDATE_SERVER)
            val api = NetworkModule_ProvideSettingsApiFactory(
                NetworkModule_ProvideRetrofitInterfaceFactory(
                    NetworkModule_ProvideOkHttpClientSettingsFactory()
                )
            ).get()
            val settingsRepository = SettingsRepository(api)
            val updateRequestBody = UpdateRequest(
                pkgname = "TCD",
                version = BuildConfig.VERSION_NAME
            )
            Timber.d(updateRequestBody.toString())
            when (
                val response: NetworkResult<UpdateResponse> =
                    settingsRepository.checkUpdatePost(updateRequestBody)
            ) {
                is NetworkResult.Error -> {
                    _state.value = StatefulData.Notify("Нет доступных обновлений")
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
}
