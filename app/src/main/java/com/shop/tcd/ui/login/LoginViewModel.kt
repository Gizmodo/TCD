package com.shop.tcd.ui.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shop.tcd.App
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
import com.shop.tcd.core.utils.StatefulData
import com.shop.tcd.data.dto.user.UsersList
import com.shop.tcd.data.local.DataStoreRepository
import com.shop.tcd.data.remote.SettingsRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel : ViewModel() {
    private var _state = MutableStateFlow<StatefulData<UsersList>>(StatefulData.Loading)
    val state: StateFlow<StatefulData<UsersList>> get() = _state

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
        .datastore(DataStoreModule)
        .build()

    init {
        injector.inject(this)
    }

    @Inject
    lateinit var ds: DataStoreRepository

    fun loadOptions() {
        job?.cancel()
        job = viewModelScope.launch(exceptionHandler) {
            val ok = NetworkModule_ProvideOkHttpClientSettingsFactory()
            val retro = NetworkModule_ProvideRetrofitInterfaceFactory(ok)
            val api = NetworkModule_ProvideSettingsApiFactory(retro)

            val settingsRepository = SettingsRepository(settingsApi = api.get())

            _state.value = StatefulData.Loading
            when (val response = settingsRepository.users()) {
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
        _state.value = StatefulData.Error(throwable.message.toString())
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("Cancel job")
        job?.cancel()
    }
}
