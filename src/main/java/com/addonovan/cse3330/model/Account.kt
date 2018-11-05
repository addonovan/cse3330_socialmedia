package com.addonovan.cse3330.model

import java.sql.ResultSet
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

    companion object {
        fun fromRow(row: ResultSet) = Account(
                row.getInt("Id"),
                row.getString("Email"),
                row.getString("PhoneNumber"),
                row.getString("ProfileImageURL"),
                row.getString("HeaderImageURL"),
                row.getBoolean("IsPrivate"),
                row.getBoolean("IsActive"),
                row.getTimestamp("CreatedTime")
        )
    }

    fun insert(): String =
            """
            INSERT INTO "Account"
                (Email, PhoneNumber, ProfileImageURL, HeaderImageURL, IsPrivate, IsActive)
            VALUES
                ($email, $phoneNumber, $profileImageURL, $headerImageURL, $isPrivate, $isActive)
            RETURNING Id;
            """.trimIndent()

}
