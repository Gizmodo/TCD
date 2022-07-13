package com.shop.tcd.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.shop.tcd.R
import com.shop.tcd.core.extension.fancyError
import com.shop.tcd.core.extension.fancyException
import com.shop.tcd.core.extension.getViewModel
import com.shop.tcd.core.extension.navigateExt
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.core.utils.Constants
import com.shop.tcd.core.utils.Constants.Inventory.BARCODE_LENGTH
import com.shop.tcd.core.utils.Constants.Inventory.BARCODE_LENGTH_WO_CRC
import com.shop.tcd.core.utils.Constants.Network.BASE_SHOP_URL
import com.shop.tcd.core.utils.Constants.SelectedObjects.ShopModel
import com.shop.tcd.core.utils.Constants.SelectedObjects.ShopModelPosition
import com.shop.tcd.core.utils.Constants.SelectedObjects.shopTemplate
import com.shop.tcd.core.utils.SearchType
import com.shop.tcd.core.utils.StatefulData
import com.shop.tcd.data.dto.shop.ShopModel
import com.shop.tcd.data.dto.shop.ShopsList
import com.shop.tcd.databinding.FragmentMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class MainFragment : Fragment(R.layout.fragment_main) {
    private val binding by viewBindingWithBinder(FragmentMainBinding::bind)
    private lateinit var shopsList: ShopsList
    private val viewModel: MainViewModel by lazy {
        getViewModel { MainViewModel() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUIListeners()
        setStateUI(enabled = false)
        restoreSelectedShop()
        initViewModelObservers()
    }

    private fun restoreSelectedShop() {
        if (ShopModelPosition != -1) {
            binding.edtShop.setText(ShopModel.name)
            setStateUI(enabled = true)
        }
    }

    private fun setStateUI(enabled: Boolean) {
        binding.btnPrint.isEnabled = enabled
        binding.btnCatalog.isEnabled = enabled
        binding.btnNomenclature.isEnabled = enabled
        binding.btnInventory.isEnabled = enabled
        binding.btnOverEstimate.isEnabled = enabled
        binding.btnRefund.isEnabled = enabled
    }

    private fun initViewModelObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest {
                    when (it) {
                        is StatefulData.Error -> {
                            Timber.e(it.msg)
                            fancyError { it.msg }
                        }
                        StatefulData.Loading -> {
                            Timber.d("Запрос на получение списка магазинов")
                        }
                        is StatefulData.Notify -> {
                            Timber.e(it.msg)
                            fancyException { it.msg }
                        }
                        is StatefulData.Success -> {
                            shopsList = it.result
                            setupShops(binding.edtShop, it.result)
                        }
                    }
                }
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
            if (ShopModelPosition != position) {
                viewModel.clearNomenclature()
            }

            ShopModel = items[position]
            setShopTemplate(ShopModel)
            ShopModelPosition = position
            BASE_SHOP_URL =
                "http://" + items[position].address + "/" + items[position].service + "/hs/TSD/"
            setStateUI(enabled = true)
        }

        if (ShopModelPosition != -1) {
            view.setText(adapter.getItem(ShopModelPosition), false)
        }
    }

    private fun setShopTemplate(shopModel: ShopModel) {
        val template = shopModel.templates.firstOrNull()
        template?.let {
            when (it.length) {
                BARCODE_LENGTH -> {
                    val prefix = it.take(2)
                    var weightPosition: Pair<Int, Int> = Pair(0, 0)
                    var infoPosition: Pair<Int, Int> = Pair(0, 0)
                    var searchType: SearchType = SearchType.Empty

                    val matchTM = it.matches("^\\d{2}Т{5}М{5}К".toRegex())
                    val matchPM = it.matches("^\\d{2}П{5}М{5}К".toRegex())
                    val matchMT = it.matches("^\\d{2}М{5}Т{5}К".toRegex())
                    val matchMP = it.matches("^\\d{2}М{5}П{5}К".toRegex())

                    if (matchPM || matchMP) {
                        searchType = SearchType.SearchByPLU
                    }
                    if (matchMT || matchTM) {
                        searchType = SearchType.SearchByCode
                    }
                    if (matchTM || matchPM) {
                        infoPosition = Pair(2, 7)
                        weightPosition = Pair(7, BARCODE_LENGTH_WO_CRC)
                    }
                    if (matchMT || matchMP) {
                        weightPosition = Pair(2, 7)
                        infoPosition = Pair(7, BARCODE_LENGTH_WO_CRC)
                    }
                    shopTemplate = Constants.ShopTemplate(
                        prefix = prefix,
                        weightPosition = weightPosition,
                        infoPosition = infoPosition,
                        searchType = searchType
                    )
                }
            }
        }
    }

    private fun initUIListeners() {
        binding.btnCatalog.setOnClickListener {
            navigateExt(MainFragmentDirections.actionMainFragmentToCatalogFragment())
        }
        binding.btnNomenclature.setOnClickListener {
            navigateExt(MainFragmentDirections.actionMainFragmentToNomenclatureFragment())
        }
        binding.btnInventory.setOnClickListener {
            navigateExt(MainFragmentDirections.actionMainFragmentToInventoryFragment())
        }
        binding.btnPrint.setOnClickListener {
            navigateExt(MainFragmentDirections.actionMainFragmentToPrintFragment())
        }
        binding.btnOverEstimate.setOnClickListener {
            navigateExt(MainFragmentDirections.actionMainFragmentToOverestimationFragment())
        }
        binding.btnRefund.setOnClickListener {
            navigateExt(MainFragmentDirections.actionMainFragmentToRefundFragment())
        }
    }
}
