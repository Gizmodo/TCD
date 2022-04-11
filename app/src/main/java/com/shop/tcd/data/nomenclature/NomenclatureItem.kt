package com.shop.tcd.data.nomenclature

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "nomenclature")
data class NomenclatureItem(
    @SerializedName("barcode") @ColumnInfo(
        name = "barcode",
        index = true
    ) var barcode: String,
    @SerializedName("code") @ColumnInfo(name = "code") var code: String,
    @SerializedName("name") @ColumnInfo(name = "name") var name: String,
    @SerializedName("plu") @ColumnInfo(name = "plu") var plu: String,
    @ColumnInfo(name = "price") var price: String,
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int? = null

    constructor() : this("", "", "", "", "")
}