package com.shop.tcd.v2.screen.nomenclature

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.v2.core.di.*
import com.shop.tcd.v2.data.group.GroupsList
import com.shop.tcd.v2.domain.database.NomenclatureDao
import kotlinx.coroutines.*
import javax.inject.Inject

class NomenclatureViewModel : ViewModel() {
    private var _nomenclatureLiveData = MutableLiveData<List<NomenclatureItem>>()
    val nomenclatureLiveData: LiveData<List<NomenclatureItem>> get() = _nomenclatureLiveData
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> get() = _successMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private var _groupsListLiveData = MutableLiveData<GroupsList>()
    val groupsListLiveData: LiveData<GroupsList> get() = _groupsListLiveData

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
        loadNomenclature()
    }

    @Inject
    lateinit var nomenclatureDao: NomenclatureDao

    fun loadNomenclature() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response: List<NomenclatureItem> = nomenclatureDao.getAll()
            _nomenclatureLiveData.postValue(response)
            /*withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    _groupsListLiveData.postValue(response.body())
                } else {
                    onError("Ошибка : ${response.message()} ")
                }
                _loading.value = false
            }*/
        }
    }

    fun loadNomenclatureBySearch(search: String) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response: List<NomenclatureItem> = nomenclatureDao.getNomenclatureBySearch(search)
            _nomenclatureLiveData.postValue(response)
        }
    }

    private fun onError(message: String) {
        _errorMessage.postValue(message)
        _loading.postValue(false)
    }

    private fun onSuccess(message: String) {
        _successMessage.postValue(message)
        _loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}