package com.shop.tcd.data.inventory

import com.shop.tcd.data.nomenclature.NomenclatureItem

data class InventoryPair(
    val currentItem: NomenclatureItem?,
    val previousItem: InvItem?,
)