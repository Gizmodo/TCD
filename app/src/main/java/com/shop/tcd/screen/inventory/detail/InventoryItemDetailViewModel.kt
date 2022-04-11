package com.shop.tcd.screen.inventory.detail

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.data.dao.NomenclatureDao
import com.shop.tcd.data.nomenclature.NomenclatureItem
import kotlinx.coroutines.*
import javax.inject.Inject

class InventoryItemDetailViewModel : ViewModel() {
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private var _nomenclatureLiveData = MutableLiveData<NomenclatureItem?>()
    val nomenclatureLiveData: LiveData<NomenclatureItem?> get() = _nomenclatureLiveData

    var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Возникло исключение: ${throwable.localizedMessage}")
    }

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

    fun loadItem(code: String, barcode: String) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = nomenclatureDao.getItemForDetail(code, barcode)
            _nomenclatureLiveData.postValue(response)
        }
    }

    @Inject
    lateinit var nomenclatureDao: NomenclatureDao

    private fun onError(message: String) {
        _errorMessage.postValue(message)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}