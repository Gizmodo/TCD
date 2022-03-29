package com.shop.tcd.v2.screen.inventory.chronology

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.model.InvItem
import com.shop.tcd.v2.core.di.*
import com.shop.tcd.v2.domain.database.InventoryDao
import kotlinx.coroutines.*
import javax.inject.Inject

class InventoryChronologyViewModel : ViewModel() {
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private var _inventoryLiveData = MutableLiveData<List<InvItem>>()
    val inventoryLiveData: LiveData<List<InvItem>> get() = _inventoryLiveData

    private var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Возникло исключение: ${throwable.localizedMessage}")
    }
    private val context = App.applicationContext() as Application

    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .app(AppModule(context))
        .dbm(DataBaseModule(context))
        .datastore(DataStoreModule)
        .build()

    init {
        injector.inject(this)
        loadIventoryList()
    }

    @Inject
    lateinit var inventoryDao: InventoryDao

    private fun loadIventoryList() {
        _loading.value = true
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response: List<InvItem> = inventoryDao.selectAllSuspend()
            _inventoryLiveData.postValue(response)
            _loading.postValue(false)
        }
    }

    private fun onError(message: String) {
        _errorMessage.postValue(message)
        _loading.postValue(false)
    }

    private fun onSuccess(message: String) {
        _loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}