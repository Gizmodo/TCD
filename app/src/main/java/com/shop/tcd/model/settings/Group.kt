package com.shop.tcd.model.settings

data class Group(
    val groupName: String,
    val groupUsers: List<GroupUser>,
)