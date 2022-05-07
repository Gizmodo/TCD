package com.shop.tcd.data.dto.inventory

import com.shop.tcd.data.dto.nomenclature.NomenclatureItem

data class InventoryPair(
    val currentItem: NomenclatureItem?,
    val previousItem: InvItem?,
)