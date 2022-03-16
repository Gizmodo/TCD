package com.shop.tcd.v2.screen.print

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class PrintViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrintViewModel::class.java)) {
            return PrintViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}