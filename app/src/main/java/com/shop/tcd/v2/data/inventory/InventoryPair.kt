package com.shop.tcd.v2.data.inventory

import com.shop.tcd.model.InvItem
import com.shop.tcd.v2.data.nomenclature.NomenclatureItem

data class InventoryPair(
    val currentItem: NomenclatureItem?,
    val previousItem: InvItem?,
)