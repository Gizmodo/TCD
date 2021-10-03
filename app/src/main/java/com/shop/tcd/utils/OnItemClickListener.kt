package com.shop.tcd.utils

interface OnItemClickListener<in V> {
    fun onClick(item: V)
}