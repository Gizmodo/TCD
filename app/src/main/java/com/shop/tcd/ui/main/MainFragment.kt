package com.shop.tcd.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.shop.tcd.R
import com.shop.tcd.core.extension.*
import com.shop.tcd.core.utils.Constants
import com.shop.tcd.core.utils.Constants.Inventory.BARCODE_LENGTH
import com.shop.tcd.core.utils.Constants.Inventory.BARCODE_LENGTH_WO_CRC
import com.shop.tcd.core.utils.Constants.Network.BASE_SHOP_URL
import com.shop.tcd.core.utils.Constants.SelectedObjects.ShopModel
import com.shop.tcd.core.utils.Constants.SelectedObjects.ShopModelPosition
import com.shop.tcd.core.utils.Constants.SelectedObjects.shopTemplate
import com.shop.tcd.core.utils.SearchType
import com.shop.tcd.data.dto.shop.ShopModel
import com.shop.tcd.data.dto.shop.ShopsList
import com.shop.tcd.databinding.FragmentMainBinding
import timber.log.Timber

class MainFragment : Fragment(R.layout.fragment_main) {
    private val binding by viewBindingWithBinder(FragmentMainBinding::bind)
    private lateinit var btnPrint: Button
    private lateinit var btnCatalog: Button
    private lateinit var btnNomenclature: Button
    private lateinit var btnInventory: Button
    private lateinit var btnOverEstimate: Button
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
        if (ShopModelPosition != -1) {
            binding.edtShop.setText(ShopModel.name)
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
        btnOverEstimate.isEnabled = enabled
    }

    private fun initViewModelObservers() {
        viewModel.shopsLiveData.observe(viewLifecycleOwner) {
            shopsList = it
            setupShops(binding.edtShop, it)
        }

        viewModel.exceptionMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            fancyException { it }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Timber.e(it)
            fancyError { it }
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

                    val matchTM = it.matches("^\\d{2}Т{5}М{5}К{1}".toRegex())
                    val matchPM = it.matches("^\\d{2}П{5}М{5}К{1}".toRegex())
                    val matchMT = it.matches("^\\d{2}М{5}Т{5}К{1}".toRegex())
                    val matchMP = it.matches("^\\d{2}М{5}П{5}К{1}".toRegex())

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

    private fun initUI() {
        btnCatalog = binding.btnCatalog
        btnNomenclature = binding.btnNomenclature
        btnInventory = binding.btnInventory
        btnPrint = binding.btnPrint
        btnOverEstimate = binding.btnOverEstimate
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
        btnOverEstimate.setOnClickListener {
            navigateExt(MainFragmentDirections.actionMainFragmentToOverestimationFragment())
        }
    }
}