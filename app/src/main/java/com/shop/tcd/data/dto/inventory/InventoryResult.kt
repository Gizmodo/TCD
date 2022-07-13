package com.shop.tcd.data.dto.inventory

import com.google.gson.annotations.SerializedName

data class InventoryResult(
    @SerializedName("autor")
    val autor: String,
    @SerializedName("document")
    val document: List<InvItem>,
    @SerializedName("message")
    val message: String,
    @SerializedName("operation")
    val operation: String,
    @SerializedName("prefix")
    val prefix: String,
    @SerializedName("result")
    val result: String,
    @SerializedName("shop")
    val shop: String,
)
