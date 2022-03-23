package com.shop.tcd.v2.screen.nomenclature

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.textfield.TextInputLayout
import com.shop.tcd.R
import com.shop.tcd.databinding.FragmentNomenclatureBinding
import com.shop.tcd.model.NomenclatureItem
import com.shop.tcd.v2.core.extension.getViewModel
import com.shop.tcd.v2.core.extension.longFancy
import com.shop.tcd.v2.core.extension.onChange
import com.shop.tcd.v2.core.extension.viewBindingWithBinder
import com.shop.tcd.v2.ui.adapters.NomenclatureAdapter
import timber.log.Timber
import java.util.*

class NomenclatureFragment : Fragment(R.layout.fragment_nomenclature) {
    private var data: List<NomenclatureItem> = mutableListOf()
    private var dataForSearchResult: ArrayList<NomenclatureItem> = ArrayList()
    private val binding by viewBindingWithBinder(FragmentNomenclatureBinding::bind)
    private lateinit var shimmer: ConstraintLayout
    private lateinit var tilSearch: TextInputLayout
    private lateinit var edtSearch: EditText
    private lateinit var rvNomenclature: RecyclerView
    private val viewModel: NomenclatureViewModel by lazy { getViewModel { NomenclatureViewModel() } }
    private lateinit var newArrayList: ArrayList<NomenclatureItem>
    private var adapterNomeclature = NomenclatureAdapter(mutableListOf())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        initUIListeners()
        initRecyclerView()
        initViewModelObservers()
    }

    private fun initUIListeners() {
        edtSearch.onChange {
            dataForSearchResult.clear()
            val searchtext = it.lowercase(Locale.getDefault())
            if (searchtext.isNotEmpty()) {
                data.forEach { item ->
                    if (
                        item.name.lowercase(Locale.getDefault()).contains(searchtext) ||
                        item.barcode.lowercase(Locale.getDefault()).contains(searchtext) ||
                        item.code.lowercase(Locale.getDefault()).contains(searchtext)
                    ) {
                        Timber.d("Found")
                        dataForSearchResult.add(item)
                    }
                }
            } else {
                Timber.d("Restore data")
                dataForSearchResult.clear()
                dataForSearchResult.addAll(data)

            }
            adapterNomeclature.updateList(dataForSearchResult)
            adapterNomeclature.notifyDataSetChanged()
        }
    }

    private fun initViewModelObservers() {
        viewModel.nomenclatureLiveData.observe(viewLifecycleOwner) { items ->
            data = items
            dataForSearchResult = items as ArrayList<NomenclatureItem>
            adapterNomeclature.updateList(data)
//            newArrayList = items as ArrayList<NomenclatureItem>
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

    private fun initRecyclerView() {
        with(rvNomenclature) {
            val animator = itemAnimator
            if (animator is SimpleItemAnimator) {
                animator.supportsChangeAnimations = false
            }
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    LinearLayout.VERTICAL
                )
            )
            adapter = adapterNomeclature
        }
    }

    private fun initUI() {
        tilSearch = binding.tilSearch
        edtSearch = binding.edtSearch
        rvNomenclature = binding.rvNomenclature
        shimmer = binding.shimmer
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rvNomenclature.adapter = null
    }

    private fun showShimmer() {
        shimmer.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        shimmer.visibility = View.GONE
    }
}