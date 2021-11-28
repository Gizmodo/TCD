package com.shop.tcd.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
@kotlinx.serialization.Serializable
data class InvItem(
    @ColumnInfo(name = "code") var code: String,
    @ColumnInfo(name = "name") @kotlin.jvm.Transient var name: String,
    @ColumnInfo(name = "plu") var plu: String,
    @ColumnInfo(name = "barcode") var barcode: String,
    @ColumnInfo(name = "quantity") var quantity: String,
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int? = null

    constructor() : this("", "", "", "", "")
}
