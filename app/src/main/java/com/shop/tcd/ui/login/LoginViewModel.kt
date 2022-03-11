package com.shop.tcd.ui.login

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
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel : ViewModel() {
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

    private var _usersLiveData = MutableLiveData<List<UserListItem>>()
    val usersLiveData: LiveData<List<UserListItem>>
        get() = _usersLiveData

    init {
        test()
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