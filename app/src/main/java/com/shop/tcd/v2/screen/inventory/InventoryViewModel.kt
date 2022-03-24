package com.shop.tcd.v2.screen.inventory

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.model.InvItem
import com.shop.tcd.v2.core.di.*
import com.shop.tcd.v2.domain.database.InvDao
import kotlinx.coroutines.*
import javax.inject.Inject

class InventoryViewModel : ViewModel() {
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private var _inventoryList = MutableLiveData<List<InvItem>>()
    val inventoryList: LiveData<List<InvItem>> get() = _inventoryList

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
        loadInventoryList()
    }

    @Inject
    lateinit var inventoryDao: InvDao

    private fun loadInventoryList() {
        _loading.value = true
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response: List<InvItem> = inventoryDao.loadInventoryGrouped()
            _inventoryList.postValue(response)
            _loading.value = false
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