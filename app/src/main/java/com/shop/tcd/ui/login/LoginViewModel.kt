package com.shop.tcd.ui.login

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.utils.SingleLiveEvent
import com.shop.tcd.data.dto.user.UsersList
import com.shop.tcd.data.local.DataStoreRepository
import com.shop.tcd.data.remote.SettingsRepository
import com.shop.tcd.data.remote.SettingsRepository_Factory
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel : ViewModel() {
    /**
     * Сотояния для UI
     **/
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _exceptionMessage = SingleLiveEvent<String>()
    val exceptionMessage: SingleLiveEvent<String> get() = _exceptionMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onException(throwable)
    }

    private var _usersLiveData = MutableLiveData<UsersList>()
    val usersLiveData: LiveData<UsersList>
        get() = _usersLiveData

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
        Timber.d("Init LoginViewModel")
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var ds: DataStoreRepository
    /**
     * This is the job for all coroutines started by this ViewModel.
     * Cancelling this job will cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = SupervisorJob()
    /**
     * This is the main scope for all coroutines launched by MainViewModel.
     * Since we pass viewModelJob, you can cancel all coroutines
     * launched by uiScope by calling viewModelJob.cancel()
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    fun loadUsers() {
        Timber.d("loadUsers")
        /*  viewModelScope.launch(exceptionHandler) {
              val baseUrl = ds.getString(KEY_BASE_URL)
              BASE_URL = if (baseUrl.isNullOrEmpty()) {
                  "http://192.168.0.154/"
              } else {
                  baseUrl
              }
          }*/
        job?.cancel()
        job = viewModelScope.launch(exceptionHandler) {
            _loading.postValue(true)
            Timber.d("Before delay")
            delay(5000)
            Timber.d("After delay")
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
    /**
     * Heavy operation that cannot be done in the Main Thread
     */
    fun launchDataLoad() {
        uiScope.launch {
            sortList() // happens on the background
            // Modify UI
        }
    }
    // Move the execution off the main thread using withContext(Dispatchers.Default)
    suspend fun sortList() = withContext(Dispatchers.Default+exceptionHandler) {
        // Heavy work
        _loading.postValue(true)
        Timber.d("Before delay")
        delay(5000)
        Timber.d("After delay")
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
    private fun onError(message: String) {
        Timber.e(message)
        _errorMessage.postValue(message)
        _loading.postValue(false)
    }

    private fun onException(throwable: Throwable) {
        _exceptionMessage.postValue(throwable.message)
        _loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("Cancel job")
        job?.cancel()
        viewModelJob.cancel()
    }
}