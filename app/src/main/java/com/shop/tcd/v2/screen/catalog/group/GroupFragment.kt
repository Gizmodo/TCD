package com.shop.tcd.v2.screen.catalog.group

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.shop.tcd.R
import com.shop.tcd.databinding.FragmentGroupBinding
import com.shop.tcd.v2.core.extension.viewBindingWithBinder

class GroupFragment : Fragment(R.layout.fragment_group) {

    companion object {
        fun newInstance() = GroupFragment()
    }

    private lateinit var viewModel: GroupViewModel
    private val binding by viewBindingWithBinder(FragmentGroupBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}