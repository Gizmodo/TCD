package com.shop.tcd.screen.catalog.group

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.*
import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.data.dao.NomenclatureDao
import com.shop.tcd.data.group.GroupsList
import com.shop.tcd.data.nomenclature.NomenclatureItem
import com.shop.tcd.data.repository.ShopRepository
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class GroupsViewModel : ViewModel() {
    /**
     * Сотояния для UI
     **/
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _exceptionMessage = MutableLiveData<String>()
    val exceptionMessage: LiveData<String> get() = _exceptionMessage

    private var _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onException(throwable.localizedMessage)
    }

    private var _groupsListLiveData = MutableLiveData<GroupsList>()
    val groupsListLiveData: LiveData<GroupsList> get() = _groupsListLiveData

    private var job: Job = Job()

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
    lateinit var shopRepository: ShopRepository

    private fun loadGroups() {
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            when (val response = shopRepository.getGroupsList()) {
                is NetworkResult.Error -> {
                    onError("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException("${response.e.message}")
                }
                is NetworkResult.Success -> {
                    _groupsListLiveData.postValue(response.data)
                }
            }
            _loading.postValue(false)
        }
    }

    fun loadSelectedGroups(filtered: String) {
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _loading.postValue(true)
            when (val response = shopRepository.getNomenclatureByGroup(filtered)) {
                is NetworkResult.Error -> {
                    onError("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException("${response.e.message}")
                }
                is NetworkResult.Success -> {
                    val nomenclatureList =
                        response.data.nomenclature as ArrayList<NomenclatureItem>
                    nomenclatureList.forEach { item ->
                        item.plu = item.plu.replace(("[^\\d.]").toRegex(), "")
                    }
                    Timber.d("Загружено объектов ${nomenclatureList.size}")
                    nomenclatureDao.deleteAll()
                    nomenclatureDao.insertNomenclature(nomenclatureList)
                }
            }
            _loading.postValue(false)
        }
    }

    private fun onError(message: String) {
        Timber.e(message)
        _errorMessage.postValue(message)
        _loading.postValue(false)
    }

    private fun onException(message: String) {
        Timber.e(message)
        _exceptionMessage.postValue(message)
        _loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}