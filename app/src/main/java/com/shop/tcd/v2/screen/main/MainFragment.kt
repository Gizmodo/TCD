package com.shop.tcd.v2.screen.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.shashank.sony.fancytoastlib.FancyToast
import com.shop.tcd.databinding.FragmentMainBinding
import com.shop.tcd.utils.Common
import com.shop.tcd.v2.data.shop.ShopsList
import timber.log.Timber

class MainFragment : Fragment() {
    private lateinit var nav: NavController
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentMainBinding
    private lateinit var btnPrint: Button
    private lateinit var btnCatalog: Button
    private lateinit var btnNomenclature: Button
    private lateinit var btnInventory: Button
    private lateinit var shimmer: ConstraintLayout
    private lateinit var shopsList: ShopsList

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, MainViewModelFactory())[MainViewModel::class.java]
        nav = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        setStateUI(enabled = false)
        restoreSelectedShop()
        initViewModelObservers()
        viewModel.loadShops()
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
// TODO: Двойной вызов! 
        viewModel.shopsLiveData.observe(this) {
            Timber.d(it.toString())
            shopsList = it
            setupShops(binding.edtShop, it)
        }

        viewModel.errorMessage.observe(this) {
            Timber.e(it)
            FancyToast.makeText(
                activity?.applicationContext,
                it,
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                false
            ).show()
        }

        viewModel.loading.observe(this) {
            when {
                it -> {
                    showShimmer()
                }
                else -> {
                    hideShimmer()
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
        initUIListeners()
    }

    private fun initUIListeners() {
        btnCatalog.setOnClickListener {
            navigateCatalogFragment()
        }
        btnNomenclature.setOnClickListener {
            navigateNomenclatureFragment()
        }
        btnInventory.setOnClickListener {
            navigateInventoryFragment()
        }
        btnPrint.setOnClickListener {
            navigatePrintFragment()
        }
    }

    private fun navigateCatalogFragment() {
        nav.navigate(MainFragmentDirections.actionMainFragmentToCatalogFragment())
    }

    private fun navigateNomenclatureFragment() {
        nav.navigate(MainFragmentDirections.actionMainFragmentToNomenclatureFragment())
    }

    private fun navigateInventoryFragment() {
        nav.navigate(MainFragmentDirections.actionMainFragmentToInventoryFragment())
    }

    private fun navigatePrintFragment() {
        nav.navigate(MainFragmentDirections.actionMainFragmentToPrintFragment())
    }
}