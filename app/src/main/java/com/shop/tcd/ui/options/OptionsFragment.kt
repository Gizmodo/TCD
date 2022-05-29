package com.shop.tcd.ui.options

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.shop.tcd.App
import com.shop.tcd.R
import com.shop.tcd.core.di.*
import com.shop.tcd.core.extension.textChanges
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.core.utils.Constants
import com.shop.tcd.core.utils.Constants.DataStore.KEY_BASE_URL
import com.shop.tcd.core.utils.Constants.Network.BASE_URL
import com.shop.tcd.data.local.DataStoreRepository
import com.shop.tcd.databinding.FragmentOptionsBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

class OptionsFragment : Fragment(R.layout.fragment_options) {
    private val binding by viewBindingWithBinder(FragmentOptionsBinding::bind)
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
        Timber.d("Init OptionsFragment")
    }

    @Inject
    lateinit var ds: DataStoreRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        job = serverChangeListener()
        binding.edtServer.setText(BASE_URL)
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun serverChangeListener() = binding.edtServer
        .textChanges()
        .filterNot { it.isNullOrBlank() }
        .debounce(Constants.Inventory.DEBOUNCE_TIME)
        .onEach {
            BASE_URL = it.toString()
         //   ds.putString(KEY_BASE_URL, BASE_URL)
        }
        .launchIn(lifecycleScope)

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }
}