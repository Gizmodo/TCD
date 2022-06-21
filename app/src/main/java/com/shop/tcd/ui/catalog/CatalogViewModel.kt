package com.shop.tcd.ui.catalog

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shop.tcd.App
import com.shop.tcd.core.di.AppModule
import com.shop.tcd.core.di.DaggerViewModelInjector
import com.shop.tcd.core.di.DataBaseModule
import com.shop.tcd.core.di.DataStoreModule
import com.shop.tcd.core.di.NetworkModule
import com.shop.tcd.core.di.ViewModelInjector
import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.utils.Constants.SelectedObjects.ShopModel
import com.shop.tcd.core.utils.ShimmerState
import com.shop.tcd.data.dto.nomenclature.NomenclatureItem
import com.shop.tcd.data.remote.ShopRepository
import com.shop.tcd.data.repository.Repository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CatalogViewModel : ViewModel() {
    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private var _exceptionMessage = MutableLiveData<String>()
    val exceptionMessage: LiveData<String> get() = _exceptionMessage

    private val _state: MutableStateFlow<ShimmerState> = MutableStateFlow(ShimmerState.Empty)
    val state: StateFlow<ShimmerState> = _state.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onException(throwable)
    }

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
    }

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var shopRepository: ShopRepository

    fun cancelCurrentJob() {
        job.cancel()
    }

    fun loadNomenclatureFull() {
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _state.value = ShimmerState.Loading
            when (val response = shopRepository.getNomenclatureFull(ShopModel.prefix)) {
                is NetworkResult.Error -> {
                    onError("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException(response.e)
                }
                is NetworkResult.Success -> {
                    val itemsCount = "Сохранение в БД ${response.data.nomenclature.size} записей"
                    _state.value = ShimmerState.State(itemsCount)
                    val list = response.data.nomenclature as ArrayList<NomenclatureItem>
                    list.forEach { item ->
                        item.plu = item.plu.replace(("[^\\d.]").toRegex(), "")
                    }
                    repository.deleteAllNomenclature()
                    val jobInsert = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                        repository.insertNomenclature(list)
                    }
                    jobInsert.join()
                }
            }
            _state.value = ShimmerState.Finishing
        }
    }

    fun loadNomenclatureRemainders() {
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _state.value = ShimmerState.Loading
            when (val response = shopRepository.getNomenclatureRemainders()) {
                is NetworkResult.Error -> {
                    onError("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException(response.e)
                }
                is NetworkResult.Success -> {
                    val itemsCount = "Сохранение в БД ${response.data.nomenclature.size} записей"
                    _state.value = ShimmerState.State(itemsCount)
                    val list = response.data.nomenclature as ArrayList<NomenclatureItem>
                    list.forEach { item ->
                        item.plu = item.plu.replace(("[^\\d.]").toRegex(), "")
                    }
                    repository.deleteAllNomenclature()
                    val jobInsert = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                        repository.insertNomenclature(list)
                    }
                    jobInsert.join()
                }
            }
            _state.value = ShimmerState.Finishing
        }
    }

    fun loadNomenclatureByPeriod(period: String) {
        job.cancel()
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            _state.value = ShimmerState.Loading
            when (val response = shopRepository.getNomenclatureByPeriod(period)) {
                is NetworkResult.Error -> {
                    onError("${response.code} ${response.message}")
                }
                is NetworkResult.Exception -> {
                    onException(response.e)
                }
                is NetworkResult.Success -> {
                    val itemsCount = "Сохранение в БД ${response.data.nomenclature.size} записей"
                    _state.value = ShimmerState.State(itemsCount)
                    val list = response.data.nomenclature as ArrayList<NomenclatureItem>
                    list.forEach { item ->
                        item.plu = item.plu.replace(("[^\\d.]").toRegex(), "")
                    }
                    repository.deleteAllNomenclature()
                    val jobInsert = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                        repository.insertNomenclature(list)
                    }
                    jobInsert.join()
                }
            }
            _state.value = ShimmerState.Finishing
        }
    }

    private fun onError(message: String) {
        Timber.e(message)
        _errorMessage.postValue(message)
        _state.value = ShimmerState.Finishing
    }

    private fun onException(throwable: Throwable) {
        Timber.e(throwable)
        _exceptionMessage.postValue(throwable.message)
        _state.value = ShimmerState.Finishing
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}
