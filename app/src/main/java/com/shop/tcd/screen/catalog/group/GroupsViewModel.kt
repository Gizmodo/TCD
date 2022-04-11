package com.shop.tcd.screen.catalog.group

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.data.dao.NomenclatureDao
import com.shop.tcd.data.group.GroupsList
import com.shop.tcd.data.nomenclature.NomenclatureItem
import com.shop.tcd.domain.rest.ShopApi
import kotlinx.coroutines.*
import javax.inject.Inject

class GroupsViewModel : ViewModel() {
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
        .nm(NetworkModule)
        .dbm(DataBaseModule(context))
        .datastore(DataStoreModule)
        .build()

    init {
        injector.inject(this)
        loadGroups()
    }

    @Inject
    lateinit var nomenclatureDao: NomenclatureDao

    @Inject
    lateinit var shopApi: ShopApi

    private fun loadGroups() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = shopApi.getGroupsList()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    _groupsListLiveData.postValue(response.body())
                } else {
                    onError("Ошибка : ${response.message()} ")
                }
                _loading.value = false
            }
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

    fun loadSelectedGroups(filtered: String) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = shopApi.getNomenclatureByGroup(filtered)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    if (response.body()?.result.equals("success", false)) {
                        val nomenclatureList =
                            response.body()?.nomenclature as ArrayList<NomenclatureItem>
                        onSuccess("Загружено объектов ${nomenclatureList.size}")
                        nomenclatureDao.deleteAll()
                        nomenclatureDao.insertNomenclature(nomenclatureList)
                    } else {
                        onError("Данные не получены: ${response.body()?.message}")
                    }
                    _loading.value = false
                } else {
                    onError("Ошибка выполенения запроса : ${response.message()} ")
                }
            }
        }
    }
}