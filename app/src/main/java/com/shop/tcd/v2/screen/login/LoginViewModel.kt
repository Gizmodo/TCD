package com.shop.tcd.v2.screen.login

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.repository.network.settings.SettingsApi
import com.shop.tcd.room.dao.InvDao
import com.shop.tcd.v2.data.user.UsersList
import com.shop.tcd.v2.datastore.DataStoreRepository
import com.shop.tcd.v2.di.*
import kotlinx.coroutines.*
import javax.inject.Inject

class LoginViewModel : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }
    val loading = MutableLiveData<Boolean>()
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
    lateinit var dataStoreRepositoryImpl: DataStoreRepository

    @Inject
    lateinit var homeApi: SettingsApi

    @Inject
    lateinit var invDao: InvDao

    @Inject
    lateinit var settingsApi: SettingsApi

    private var _usersLiveData = MutableLiveData<UsersList>()
    val usersLiveData: LiveData<UsersList>
        get() = _usersLiveData

    fun loadUsersSuspend() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = settingsApi.getUsersSuspend()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    _usersLiveData.postValue(response.body())
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

    /* private fun test() {
         homeApi.getUsers().observeOn(AndroidSchedulers.mainThread())
             .subscribe({
                 _usersLiveData.value = it
             }, {
                 val item: InvItem = InvItem("code", "name", "PLU", "2216017008221", "100500")
                 viewModelScope.launch {
                     invDao.insert(item)
                 }
                 Timber.e(it)
             })
     }*/
}