package com.shop.tcd.data.dto.inventory

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class InvItem(
    @ColumnInfo(name = "code") var code: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "plu") var plu: String,
    @ColumnInfo(name = "barcode") var barcode: String,
    @ColumnInfo(name = "quantity") var quantity: String,
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int? = null

    constructor() : this("", "", "", "", "")
}
