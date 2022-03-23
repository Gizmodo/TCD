package com.shop.tcd.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Group(
    @ColumnInfo(name = "code") var code: String, // 10000
    @ColumnInfo(name = "name") var name: String, // 01.Сахар, Соль, Сода
    @Ignore
    var checked: Boolean,
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int? = null

    constructor() : this("", "", false)
}
/*

{
    constructor():this("","",false)
}
*/