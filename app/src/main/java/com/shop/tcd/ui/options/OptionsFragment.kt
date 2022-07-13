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
import com.shop.tcd.core.extension.textChanges
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.core.utils.StatefulData
import com.shop.tcd.databinding.FragmentOptionsBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

class OptionsFragment : Fragment(R.layout.fragment_options) {
    private val binding by viewBindingWithBinder(FragmentOptionsBinding::bind)
    private val viewModel: OptionsViewModel by lazy {
        getViewModel { OptionsViewModel() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
        initUIListener()
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun initUIListener() {
        binding.edtServer
            .textChanges()
            .filterNot { it.isNullOrBlank() }
            .debounce(100.milliseconds)
            .onEach {
                viewModel.saveBaseUrl(it.toString())
            }
            .launchIn(lifecycleScope)
        binding.edtUpdateServer
            .textChanges()
            .filterNot { it.isNullOrBlank() }
            .debounce(100.milliseconds)
            .onEach {
                viewModel.saveUrlUpdateServer(it.toString())
            }
            .launchIn(lifecycleScope)

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
}
