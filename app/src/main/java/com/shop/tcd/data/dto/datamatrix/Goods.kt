package com.shop.tcd.data.dto.datamatrix

data class Goods(
    var code: String,
    var serial: String,
    var mrc: String?,
    var idCheckCode: String?,
    var checkCode: String?,
    var weight: String?,
) {
    constructor() : this(
        code = "",
        serial = "",
        mrc = "",
        idCheckCode = "",
        checkCode = "",
        weight = ""
    )
}
