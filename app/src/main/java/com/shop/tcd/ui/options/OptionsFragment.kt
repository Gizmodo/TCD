package com.shop.tcd.ui.options

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.shop.tcd.R
import com.shop.tcd.core.extension.fancyError
import com.shop.tcd.core.extension.fancyException
import com.shop.tcd.core.extension.fancySuccessShort
import com.shop.tcd.core.extension.getViewModel
import com.shop.tcd.core.extension.toObservable
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.core.utils.StatefulData
import com.shop.tcd.databinding.FragmentOptionsBinding
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class OptionsFragment : Fragment(R.layout.fragment_options) {
    private val binding by viewBindingWithBinder(FragmentOptionsBinding::bind)
    private val viewModel: OptionsViewModel by lazy {
        getViewModel { OptionsViewModel() }
    }
    private var subscriptions: CompositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
        initUIListener()
    }

    private fun initUIListener() {
        val subscribeEdtServer = toObservable(binding.edtServer)
            .toFlowable(BackpressureStrategy.DROP)
            .observeOn(Schedulers.io())
            .subscribe({ viewModel.saveBaseUrl(it) }, { t -> Timber.e(t) })
        subscriptions.add(subscribeEdtServer)

        val subscribeEdtUpdateServer = toObservable(binding.edtUpdateServer)
            .toFlowable(BackpressureStrategy.DROP)
            .observeOn(Schedulers.io())
            .subscribe({ viewModel.saveUrlUpdateServer(it) }, { t -> Timber.e(t) })
        subscriptions.add(subscribeEdtUpdateServer)

        binding.btnCheckUpdate.setOnClickListener {
            viewModel.checkUpdate()
        }
    }

    private fun initViewModelObservers() {
        viewModel.url.observe(viewLifecycleOwner) {
            binding.edtServer.setText(it)
        }

        viewModel.urlUpdateServer.observe(viewLifecycleOwner) {
            binding.edtUpdateServer.setText(it)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest {
                    when (it) {
                        is StatefulData.Error -> {
                            fancyError { it.msg }
                        }
                        StatefulData.Loading -> {
                            Timber.d("Запрос на получение обновлений")
                        }
                        is StatefulData.Notify -> {
                            fancyException { it.msg }
                        }
                        is StatefulData.Success -> {
                            fancySuccessShort { "Доступна новая версия ${it.result.version}" }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        subscriptions.clear()
        super.onDestroy()
    }
}
