package com.shop.tcd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shop.tcd.room.database.DatabaseHelper

class InventarisationViewModelFactory(private val dbHelper: DatabaseHelper) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventarisationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventarisationViewModel(dbHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}