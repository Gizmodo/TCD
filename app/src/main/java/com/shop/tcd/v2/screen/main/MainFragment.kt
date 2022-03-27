package com.shop.tcd.v2.screen.main

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.shop.tcd.R
import com.shop.tcd.databinding.FragmentMainBinding
import com.shop.tcd.v2.core.extension.getViewModel
import com.shop.tcd.v2.core.extension.longFancy
import com.shop.tcd.v2.core.extension.navigateExt
import com.shop.tcd.v2.core.extension.viewBindingWithBinder
import com.shop.tcd.v2.core.utils.Common
import com.shop.tcd.v2.data.shop.ShopsList
import timber.log.Timber

class MainFragment : Fragment(R.layout.fragment_main) {
    private val binding by viewBindingWithBinder(FragmentMainBinding::bind)
    private lateinit var btnPrint: Button
    private lateinit var btnCatalog: Button
    private lateinit var btnNomenclature: Button
    private lateinit var btnInventory: Button
    private lateinit var shimmer: ConstraintLayout
    private lateinit var shopsList: ShopsList
    private val viewModel: MainViewModel by lazy {
        getViewModel { MainViewModel() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initUIListeners()
        setStateUI(enabled = false)
        restoreSelectedShop()
        initViewModelObservers()
    }

    private fun restoreSelectedShop() {
        if (Common.selectedShopModelPosition != -1) {
            binding.edtShop.setText(Common.selectedShopModel.name)
            setStateUI(enabled = true)
        }
    }

    private fun showShimmer() {
        shimmer.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        shimmer.visibility = View.GONE
    }

    private fun setStateUI(enabled: Boolean) {
        btnPrint.isEnabled = enabled
        btnCatalog.isEnabled = enabled
        btnNomenclature.isEnabled = enabled
        btnInventory.isEnabled = enabled
    }

    private fun initViewModelObservers() {
        viewModel.shopsLiveData.observe(viewLifecycleOwner) {
            Timber.d(it.toString())
            shopsList = it
            setupShops(binding.edtShop, it)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            longFancy { it }
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            when {
                it -> showShimmer()
                else -> hideShimmer()
            }
        }
    }

    private fun setupShops(view: AutoCompleteTextView, items: ShopsList) {
        val names: AbstractList<String?> = object : AbstractList<String?>() {
            override fun get(index: Int): String {
                return items[index].name
            }

            override val size: Int
                get() = items.size
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)
        view.setAdapter(adapter)

        view.setOnItemClickListener { _, _, position, _ ->
            Common.selectedShopModel = items[position]
            Common.selectedShopModelPosition = position
            Common.BASE_SHOP_URL =
                "http://" + items[position].address + "/" + items[position].service + "/hs/TSD/"
            setStateUI(enabled = true)
        }
        if (Common.selectedShopModelPosition != -1) {
            view.setText(adapter.getItem(Common.selectedShopModelPosition), false)
        }
    }

    private fun initUI() {
        btnCatalog = binding.btnCatalog
        btnNomenclature = binding.btnNomenclature
        btnInventory = binding.btnInventory
        btnPrint = binding.btnPrint
        shimmer = binding.shimmer
    }

    private fun initUIListeners() {
        btnCatalog.setOnClickListener {
            navigateExt(MainFragmentDirections.actionMainFragmentToCatalogFragment())
        }
        btnNomenclature.setOnClickListener {
            navigateExt(MainFragmentDirections.actionMainFragmentToNomenclatureFragment())
        }
        btnInventory.setOnClickListener {
            navigateExt(MainFragmentDirections.actionMainFragmentToInventoryFragment())
        }
        btnPrint.setOnClickListener {
            navigateExt(MainFragmentDirections.actionMainFragmentToPrintFragment())
        }
    }
}