package com.addonovan.cse3330.model

import java.sql.Timestamp

data class Account(
    val id: Int?,
    val email: String,
    val phoneNumber: String,
    val profileImageURL: String?,
    val headerImageURL: String?,
    val isPrivate: Boolean = true,
    val isActive: Boolean = true,
    val createdTime: Timestamp?
) {

    fun insert(): String =
            """
            INSERT INTO "Account"
                (Email, PhoneNumber, ProfileImageURL, HeaderImageURL, IsPrivate, IsActive)
            VALUES
                ($email, $phoneNumber, $profileImageURL, $headerImageURL, $isPrivate, $isActive)
            RETURNING Id;
            """.trimIndent()

}
