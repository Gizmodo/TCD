package com.shop.tcd.screen.inventory.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.shop.tcd.R
import com.shop.tcd.core.extension.getViewModel
import com.shop.tcd.core.extension.longFancy
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.data.nomenclature.NomenclatureItem
import com.shop.tcd.databinding.FragmentInventoryItemDetailBinding
import timber.log.Timber

class InventoryItemDetailFragment : Fragment(R.layout.fragment_inventory_item_detail) {
    private val binding by viewBindingWithBinder(FragmentInventoryItemDetailBinding::bind)
    private val viewModel: InventoryItemDetailViewModel by lazy {
        getViewModel { InventoryItemDetailViewModel() }
    }
    private val args: InventoryItemDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
        Timber.d("code ${args.code}")
        Timber.d("barcode ${args.barcode}")
        viewModel.loadItem(args.code, args.barcode)
    }

    private fun initViewModelObservers() {
        viewModel.nomenclatureLiveData.observe(viewLifecycleOwner) {
            Timber.d(it.toString())
            bindUI(it)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            longFancy { it }
        }
    }

    private fun bindUI(it: NomenclatureItem?) {
        it?.let {
            with(binding) {
                txtDetailBarcode.text = it.barcode
                txtDetailCode.text = it.code
                txtDetailName.text = it.name
                txtDetailPLU.text = it.plu
                txtDetailPrice.text = it.price
            }
        }
    }
}