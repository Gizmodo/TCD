package com.shop.tcd.ui.options

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.shop.tcd.R
import com.shop.tcd.core.extension.fancyException
import com.shop.tcd.core.extension.getViewModel
import com.shop.tcd.core.extension.toObservable
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.databinding.FragmentOptionsBinding
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
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
        val subscribeEdtDescription = toObservable(binding.edtServer)
            .toFlowable(BackpressureStrategy.DROP)
            .observeOn(Schedulers.io())
            .subscribe({ viewModel.saveBaseUrl(it) }, { t -> Timber.e(t) })
        subscriptions.add(subscribeEdtDescription)
    }

    private fun initViewModelObservers() {
        viewModel.url.observe(viewLifecycleOwner) {
            Timber.d("URL: $it")
            binding.edtServer.setText(it)
        }

        viewModel.exceptionMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            fancyException { it }
        }
    }

    override fun onDestroy() {
        subscriptions.clear()
        super.onDestroy()
    }
}
