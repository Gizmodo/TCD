package com.shop.tcd.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.di.DaggerViewModelInjector
import com.shop.tcd.di.NetworkModule
import com.shop.tcd.di.ViewModelInjector
import com.shop.tcd.model.newsettigs.UserListItem
import com.shop.tcd.repository.network.settings.SettingsApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel : ViewModel() {
      private val injector: ViewModelInjector = DaggerViewModelInjector
          .builder()
          .networkModule(NetworkModule())
          .build()

      init {
          injector.inject(this)
      }

    @Inject
    lateinit var homeApi: SettingsApi
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
                Timber.e(it)
            })
    }
}