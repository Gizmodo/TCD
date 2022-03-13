package com.shop.tcd.ui.print

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shop.tcd.R

class PrintFragment : Fragment() {

    companion object {
        fun newInstance() = PrintFragment()
    }

    private lateinit var viewModel: PrintViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_print, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PrintViewModel::class.java)
        // TODO: Use the ViewModel
    }

}