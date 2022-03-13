package com.shop.tcd.v2.screen.login

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shop.tcd.App
import com.shop.tcd.di.DaggerViewModelInjector
import com.shop.tcd.di.DataBaseModule
import com.shop.tcd.di.NetworkModule
import com.shop.tcd.di.ViewModelInjector
import com.shop.tcd.model.InvItem
import com.shop.tcd.model.newsettigs.UserListItem
import com.shop.tcd.repository.network.settings.SettingsApi
import com.shop.tcd.room.dao.InvDao
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val movieList = MutableLiveData<List<UserListItem>>()
    var job: Job? = null
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }
    val loading = MutableLiveData<Boolean>()

    private var usersList: List<UserListItem>? = null
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

    private var _usersLiveData = MutableLiveData<List<UserListItem>>()
    val usersLiveData: LiveData<List<UserListItem>>
        get() = _usersLiveData

    init {
//        test()
        loadUsers()
    }

    fun getAllMovies() {
        job = CoroutineScope(Dispatchers.IO).launch {
            val response = mainRepository.getAllMovies()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    movieList.postValue(response.body())
                    loading.value = false
                } else {
                    onError("Error : ${response.message()} ")
                }
            }
        }

    }

    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }

    private fun loadUsers() {
        settingsApi.getUsers().observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                usersList = it
            }, {
                usersList = null
            })
    }

    private fun test() {
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
    }
}