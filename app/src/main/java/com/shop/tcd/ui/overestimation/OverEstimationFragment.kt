package com.shop.tcd.ui.overestimation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.shop.tcd.R
import com.shop.tcd.core.extension.getViewModel
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.databinding.FragmentOverestimationBinding
import timber.log.Timber

class OverEstimationFragment : Fragment(R.layout.fragment_overestimation) {
    private val binding by viewBindingWithBinder(FragmentOverestimationBinding::bind)

    private val viewModel: OverEstimationViewModel by lazy { getViewModel { OverEstimationViewModel() } }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_overestimation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.data.observe(viewLifecycleOwner) {
            Timber.d(it.toString())
            binding.textView2.text = it.toString()
        }
    }
}