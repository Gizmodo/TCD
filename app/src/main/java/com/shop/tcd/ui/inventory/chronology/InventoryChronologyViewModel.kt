package com.shop.tcd.ui.inventory.chronology

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.data.dto.inventory.InvItem
import com.shop.tcd.data.repository.Repository
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class InventoryChronologyViewModel : ViewModel() {
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _exceptionMessage = MutableLiveData<String>()
    val exceptionMessage: LiveData<String> get() = _exceptionMessage

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onException(throwable)
    }

    private var _inventoryLiveData = MutableLiveData<List<InvItem>>()
    val inventoryLiveData: LiveData<List<InvItem>> get() = _inventoryLiveData

    private var job: Job? = null

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
        loadIventoryList()
    }

    @Inject
    lateinit var repository: Repository

    fun updateInventoryQuantity(uid: Int, newQuantity: String) {
        CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            repository.updateInventoryQuantity(uid, newQuantity)
            _inventoryLiveData.postValue(repository.selectAllSuspend())
        }
    }

    private fun loadIventoryList() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.selectAllSuspend()
            _inventoryLiveData.postValue(response)
        }
    }

    private fun onException(throwable: Throwable) {
        Timber.e(throwable)
        _exceptionMessage.postValue(throwable.message)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}
