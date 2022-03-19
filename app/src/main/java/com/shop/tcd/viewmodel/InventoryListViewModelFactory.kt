package com.shop.tcd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shop.tcd.v2.domain.database.DatabaseHelper

class InventoryListViewModelFactory(private val dbHelper: DatabaseHelper) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryListViewModel(dbHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}