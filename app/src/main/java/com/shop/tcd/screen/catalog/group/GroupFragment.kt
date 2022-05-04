package com.shop.tcd.screen.catalog.group

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.shop.tcd.R
import com.shop.tcd.adapters.GroupsAdapter
import com.shop.tcd.core.extension.fancyError
import com.shop.tcd.core.extension.fancyException
import com.shop.tcd.core.extension.getViewModel
import com.shop.tcd.core.extension.viewBindingWithBinder
import com.shop.tcd.data.group.Group
import com.shop.tcd.databinding.FragmentGroupBinding
import timber.log.Timber

class GroupFragment : Fragment(R.layout.fragment_group) {

    private var data: List<Group> = mutableListOf()
    private val binding by viewBindingWithBinder(FragmentGroupBinding::bind)
    private lateinit var btnLoad: Button
    private lateinit var rvGroups: RecyclerView
    private lateinit var shimmer: ConstraintLayout
    private var adapterGroups = GroupsAdapter(mutableListOf())

    private val viewModel: GroupsViewModel by lazy { getViewModel { GroupsViewModel() } }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        initUIListeners()
        initRecyclerView()
        setStateUI(false)
        initViewModelObservers()
    }

    private fun setStateUI(enabled: Boolean) {
        btnLoad.isEnabled = enabled
    }

    private fun initViewModelObservers() {
        viewModel.groupsListLiveData.observe(viewLifecycleOwner) { groups ->
            data = groups.groups
            adapterGroups.updateList(data)
            setStateUI(true)
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
        with(rvGroups) {
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
            adapter = adapterGroups
        }
    }

    private fun initUIListeners() {
        btnLoad.setOnClickListener {
            val filtered: String = data.filter { it.checked }.joinToString { it.code }
            Timber.d(filtered)
            viewModel.loadSelectedGroups(filtered)
        }
    }

    private fun initUI() {
        btnLoad = binding.btnLoad
        rvGroups = binding.rvGroups
        shimmer = binding.shimmer
    }

    private fun showShimmer() {
        shimmer.visibility = View.VISIBLE
    }

    private fun hideShimmer() {
        shimmer.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rvGroups.adapter = null
    }
}