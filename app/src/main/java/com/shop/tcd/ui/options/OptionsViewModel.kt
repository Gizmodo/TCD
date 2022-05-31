package com.shop.tcd.ui.options

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.core.utils.Constants.DataStore.KEY_BASE_URL
import com.shop.tcd.core.utils.SingleLiveEvent
import com.shop.tcd.core.utils.Util.isValidServerAddress
import com.shop.tcd.data.local.DataStoreRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class OptionsViewModel : ViewModel() {
    private var _exceptionMessage = SingleLiveEvent<String>()
    val exceptionMessage: SingleLiveEvent<String> get() = _exceptionMessage

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onException(throwable)
    }
    private var _url = SingleLiveEvent<String>()
    val url: SingleLiveEvent<String> get() = _url

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
        getBaseURL()
    }

    @Inject
    lateinit var ds: DataStoreRepository

    private fun getBaseURL() {
        viewModelScope.launch(
            Dispatchers.IO + exceptionHandler
        ) {
            _url.postValue(ds.getString(KEY_BASE_URL))
        }
    }

    private fun onException(throwable: Throwable) {
        _exceptionMessage.postValue(throwable.message)
    }

    fun saveBaseUrl(url: String) {
        val isValidDescription = url.isValidServerAddress()
        if (isValidDescription) viewModelScope.launch {
            ds.putString(KEY_BASE_URL, url)
        }
    }
}
