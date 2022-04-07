package com.shop.tcd.v2.screen.inventory.chronology

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.model.InvItem
import com.shop.tcd.v2.core.di.*
import com.shop.tcd.v2.data.dao.InventoryDao
import kotlinx.coroutines.*
import javax.inject.Inject

class InventoryChronologyViewModel : ViewModel() {
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

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

    fun updateInventoryQuantity(uid: Int, newQuantity: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            inventoryDao.updateInventoryQuantity(uid, newQuantity)
            _inventoryLiveData.postValue(inventoryDao.selectAllSuspend())
        }
    }

    private fun loadIventoryList() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = inventoryDao.selectAllSuspend()
            _inventoryLiveData.postValue(response)
        }
    }

    private fun onError(message: String) {
        _errorMessage.postValue(message)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}