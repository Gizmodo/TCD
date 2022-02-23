package com.shop.tcd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shop.tcd.model.InvItem
import com.shop.tcd.room.database.DatabaseHelper
import com.shop.tcd.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import timber.log.Timber

class InventarisationViewModel(private val dbHelper: DatabaseHelper) : ViewModel() {
    private var _items = SingleLiveEvent<List<InvItem>>()
    fun getInventarisationItems(): SingleLiveEvent<List<InvItem>> = _items

    fun fetchInventarisationItems() {
        viewModelScope.launch {
            try {
                _items.postValue(dbHelper.getInventarisationItems())
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}