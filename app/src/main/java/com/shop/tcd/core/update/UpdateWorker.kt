package com.shop.tcd.core.update

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shop.tcd.App
import com.shop.tcd.BuildConfig
import com.shop.tcd.core.di.AppModule
import com.shop.tcd.core.di.DaggerViewModelInjector
import com.shop.tcd.core.di.DataBaseModule
import com.shop.tcd.core.di.DataStoreModule
import com.shop.tcd.core.di.NetworkModule
import com.shop.tcd.core.di.NetworkModule_ProvidesUpdateApiFactory
import com.shop.tcd.core.di.NetworkModule_ProvidesUpdateOkHttpClientFactory
import com.shop.tcd.core.di.NetworkModule_ProvidesUpdateRetrofitFactory
import com.shop.tcd.core.di.ViewModelInjector
import com.shop.tcd.core.extension.NetworkResult
import com.shop.tcd.core.utils.Constants.DataStore.KEY_URL_UPDATE_SERVER
import com.shop.tcd.core.utils.Constants.Network.BASE_URL_UPDATE_SERVER
import com.shop.tcd.core.utils.ServiceNotification
import com.shop.tcd.data.dto.ato.UpdateRequest
import com.shop.tcd.data.local.DataStoreRepository
import com.shop.tcd.data.remote.UpdateRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UpdateWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {

    private val context = App.applicationContext() as Application
    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .app(AppModule(context))
        .nm(NetworkModule)
        .dbm(DataBaseModule(context))
        .datastore(DataStoreModule)
        .build()

    @Inject
    lateinit var ds: DataStoreRepository

    @Inject
    lateinit var serviceNotification: ServiceNotification

    private lateinit var result: Result
    private var content: String = ""

    init {
        injector.inject(this)
    }

    override suspend fun doWork(): Result {
        val job = CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            val baseUrlUpdateServer = ds.getString(KEY_URL_UPDATE_SERVER)
            if (baseUrlUpdateServer.isNullOrEmpty()) {
                ds.putString(KEY_URL_UPDATE_SERVER, BASE_URL_UPDATE_SERVER)
            }

            val api = NetworkModule_ProvidesUpdateApiFactory(
                NetworkModule_ProvidesUpdateRetrofitFactory(
                    NetworkModule_ProvidesUpdateOkHttpClientFactory()
                )
            ).get()
            val updateRepository = UpdateRepository(api)

            when (
                val response = updateRepository.checkUpdatePost(
                    UpdateRequest("TCD", BuildConfig.VERSION_NAME)
                )
            ) {
                is NetworkResult.Error -> {
                    Timber.d("Error")
                    onError("WorkManager - задача выполнена с ошибкой ${response.code} ${response.message}")
                    result = Result.failure()
                }
                is NetworkResult.Exception -> {
                    Timber.d("Exception")
                    onException(response.e)
                    result = Result.failure()
                }
                is NetworkResult.Success -> {
                    content =
                        BASE_URL_UPDATE_SERVER.take(BASE_URL_UPDATE_SERVER.length - 1) + response.data.url
                    serviceNotification.showNotification(content)
                    result = Result.success()
                }
            }
        }
        job.join()
        return result
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onException(throwable)
    }

    private fun onException(throwable: Throwable) {
        Timber.e(throwable)
    }

    private fun onError(message: String) {
        Timber.e(message)
    }
}
