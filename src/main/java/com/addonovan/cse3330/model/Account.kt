package com.addonovan.cse3330.model

import com.addonovan.cse3330.sql.execute
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Timestamp

open class Account  : SqlEntity {
    var id: Int = 0

    lateinit var email: String

    lateinit var phoneNumber: String

    lateinit var profileImageURL: String

    lateinit var headerImageURL: String

    var isPrivate: Boolean = true

    var isActive: Boolean = true

    lateinit var createdTime: Timestamp

    override fun fromRow(row: ResultSet) {
        id = row.getInt("Id")
        email = row.getString("Email")
        phoneNumber = row.getString("PhoneNumber")
        profileImageURL = row.getString("ProfileImageURL")
        headerImageURL = row.getString("HeaderImageURL")
        isPrivate = row.getBoolean("IsPrivate")
        isActive = row.getBoolean("IsActive")
        createdTime = row.getTimestamp("CreatedTime")
    }

}
