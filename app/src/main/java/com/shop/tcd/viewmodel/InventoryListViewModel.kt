package com.shop.tcd.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shop.tcd.model.InvItem
import com.shop.tcd.v2.domain.database.DatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class InventoryListViewModel(private val dbHelper: DatabaseHelper) : ViewModel() {
    private var _items = MutableLiveData<List<InvItem>>()
    val inventoryList: LiveData<List<InvItem>> get() = _items

    init {
        fetchInventoryList()
    }

    fun fetchInventoryList() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _items.postValue(dbHelper.getInventarisationItems())
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun updateInventoryQuantity(uid: Int, newQuantity: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dbHelper.updateInventoryQuantity(uid, newQuantity)
            fetchInventoryList()
        }
    }
}