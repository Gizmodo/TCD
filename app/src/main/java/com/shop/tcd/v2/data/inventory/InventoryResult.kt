package com.shop.tcd.v2.data.inventory

import com.google.gson.annotations.SerializedName
import com.shop.tcd.model.InvItem

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