package com.shop.tcd.ui.nomenclature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shop.tcd.R

class NomenclatureFragment : Fragment() {

    companion object {
        fun newInstance() = NomenclatureFragment()
    }

    private lateinit var viewModel: NomenclatureViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nomenclature, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NomenclatureViewModel::class.java)
        // TODO: Use the ViewModel
    }

}