package com.shop.tcd.ui.login

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.data.dto.user.UsersList
import com.shop.tcd.data.remote.SettingsRepository
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel : ViewModel() {
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

    private var _usersLiveData = MutableLiveData<UsersList>()
    val usersLiveData: LiveData<UsersList>
        get() = _usersLiveData

    var job: Job? = null

    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .app(AppModule)
        .nm(NetworkModule)
        .dbm(DataBaseModule(Application()))
        .datastore(DataStoreModule)
        .build()

    init {
        injector.inject(this)
        loadUsers()
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private fun loadUsers() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            when (val response = settingsRepository.users()) {
                is NetworkResult.Error -> {
                    onError("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException(response.e)
                }
                is NetworkResult.Success -> {
                    _usersLiveData.postValue(response.data)
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
        job?.cancel()
    }
}