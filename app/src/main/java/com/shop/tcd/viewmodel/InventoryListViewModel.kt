package com.shop.tcd.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shop.tcd.model.InvItem
import com.shop.tcd.room.database.DatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class InventoryListViewModel(private val dbHelper: DatabaseHelper) : ViewModel() {
    private var _items = MutableLiveData<List<InvItem>>()
    fun getInventoryList(): LiveData<List<InvItem>> = _items

    init {
        this.fetchInventoryList()
    }

    private fun fetchInventoryList() {
        viewModelScope.launch {
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
        }
    }

}