package com.shop.tcd.v2.screen.inventory.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shop.tcd.R

class InventoryItemDetailFragment : Fragment() {

    companion object {
        fun newInstance() = InventoryItemDetailFragment()
    }

    private lateinit var viewModel: InventoryItemDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_inventory_item_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(InventoryItemDetailViewModel::class.java)
        // TODO: Use the ViewModel
    }

}