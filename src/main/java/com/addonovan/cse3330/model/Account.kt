package com.addonovan.cse3330.model

import java.sql.Timestamp

data class Account(
    val id: Int,
    val email: String,
    val phoneNumber: String,
    val profileImageURL: String,
    val headerImageURL: String,
    val isPrivate: Boolean,
    val isActive: Boolean,
    val createdTime: Timestamp
)
