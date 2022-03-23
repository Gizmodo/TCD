package com.shop.tcd.v2.screen.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shop.tcd.R
import com.shop.tcd.databinding.FragmentInventoryBinding
import com.shop.tcd.v2.core.extension.viewBindingWithBinder

class InventoryFragment : Fragment() {

    private val binding by viewBindingWithBinder(FragmentInventoryBinding::bind)

    private lateinit var viewModel: InventoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(InventoryViewModel::class.java)
        // TODO: Use the ViewModel
    }

}