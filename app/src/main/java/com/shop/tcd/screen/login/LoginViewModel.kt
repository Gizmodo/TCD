package com.shop.tcd.screen.login

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.data.user.UsersList
import com.shop.tcd.domain.rest.SettingsApi
import kotlinx.coroutines.*
import javax.inject.Inject

class LoginViewModel : ViewModel() {
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private var _usersLiveData = MutableLiveData<UsersList>()
    val usersLiveData: LiveData<UsersList>
        get() = _usersLiveData

    var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }
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
        loadUsers()
    }

    @Inject
    lateinit var settingsApi: SettingsApi

    private fun loadUsers() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = settingsApi.getUsersSuspend()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    _usersLiveData.postValue(response.body())
                    _loading.value = false
                } else {
                    onError("Ошибка : ${response.message()} ")
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