package com.shop.tcd.v2.screen.login

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.model.newsettigs.UserListItem
import com.shop.tcd.repository.network.settings.SettingsApi
import com.shop.tcd.room.dao.InvDao
import com.shop.tcd.v2.data.user.UsersList
import com.shop.tcd.v2.di.DaggerViewModelInjector
import com.shop.tcd.v2.di.DataBaseModule
import com.shop.tcd.v2.di.NetworkModule
import com.shop.tcd.v2.di.ViewModelInjector
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import javax.inject.Inject

class LoginViewModel : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }
    val loading = MutableLiveData<Boolean>()

    private var usersListObservable: List<UserListItem>? = null
    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .networkModule(NetworkModule())
        .databaseModule(DataBaseModule(App.applicationContext() as Application))
        .build()

    init {
        injector.inject(this)
    }

    @Inject
    lateinit var homeApi: SettingsApi

    @Inject
    lateinit var invDao: InvDao

    @Inject
    lateinit var settingsApi: SettingsApi

    private var _usersLiveData = MutableLiveData<UsersList>()
    val usersLiveData: LiveData<UsersList>
        get() = _usersLiveData

    init {
//        test()
//        loadUsersObservable()
//        loadUsersSuspend()
    }

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

    private fun loadUsersObservable() {
        settingsApi.getUsersObservable().observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                usersListObservable = it
            }, {
                usersListObservable = null
            })
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