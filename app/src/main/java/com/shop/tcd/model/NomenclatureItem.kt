package com.shop.tcd.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "nomenclature")
data class NomenclatureItem(
    @ColumnInfo(name = "barcode", index = true) var barcode: String, // 4601248016370
    @ColumnInfo(name = "code") var code: String, // 56014
    @ColumnInfo(name = "name") var name: String, // Нектар Angry Birds 0,2л Смесь ягод и фруктов /24
    @ColumnInfo(name = "plu") var plu: String, // 0
    @ColumnInfo(name = "price") var price: String, // 27.5
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int? = null

    constructor() : this("", "", "", "", "")
}