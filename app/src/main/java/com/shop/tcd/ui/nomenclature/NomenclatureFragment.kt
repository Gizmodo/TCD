package com.shop.tcd.ui.nomenclature

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.textfield.TextInputLayout
import com.shop.tcd.R
import com.shop.tcd.core.extension.*
import com.shop.tcd.data.dto.nomenclature.NomenclatureItem
import com.shop.tcd.databinding.FragmentNomenclatureBinding
import com.shop.tcd.ui.nomenclature.adapter.NomenclatureAdapter
import timber.log.Timber
import java.util.*

@Suppress("ktPropBy")
class NomenclatureFragment : Fragment(R.layout.fragment_nomenclature) {
    private var data: List<NomenclatureItem> = mutableListOf()
    private val binding by viewBindingWithBinder(FragmentNomenclatureBinding::bind)
    private lateinit var shimmer: ConstraintLayout
    private lateinit var tilSearch: TextInputLayout
    private lateinit var edtSearch: EditText
    private lateinit var rvNomenclature: RecyclerView
    private val viewModel: NomenclatureViewModel by lazy { getViewModel { NomenclatureViewModel() } }
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
            if (it.isNotEmpty()) {
                viewModel.loadNomenclatureBySearch(it.lowercase(Locale.getDefault()))
            } else {
                viewModel.loadNomenclature()
            }
        }
        binding.fabClear.setOnClickListener {
            clearNomenclature()
        }
    }

    private fun clearNomenclature() {
        val builderAlert = AlertDialog.Builder(requireContext())
        with(builderAlert) {
            setTitle("Внимание")
            setMessage("Очистить номенклатуру?")
            setPositiveButton("Да") { _: DialogInterface, _: Int ->
                viewModel.clearNomenclature()
            }
            setNegativeButton("Нет") { _, _ -> fancyInfo { "Очистка отменена" } }
            show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initViewModelObservers() {
        viewModel.nomenclatureLiveData.observe(viewLifecycleOwner) { items ->
            data = items
            adapterNomeclature.updateList(data)
            adapterNomeclature.notifyDataSetChanged()
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