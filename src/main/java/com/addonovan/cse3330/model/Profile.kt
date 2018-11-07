package com.addonovan.cse3330.model

import java.sql.ResultSet

/**
 * The class model representing the SQL `Profile` entity. This is a
 * specialization of the [Account] entity.
 */
class Profile : Account() {

    /** The first name of this profile's user. */
    lateinit var firstName: String

    /** The last name of this profile's user. */
    lateinit var lastName: String

    /** The unique username of this profile. */
    lateinit var username: String

    /** This user's password (which will probably be hashed in the future) */
    lateinit var password: String

    /** The primary language of this user. */
    var languageId: Int = 0

    override fun fromRow(row: ResultSet) {
        super.fromRow(row)
        firstName = row.getString("FirstName")
        lastName = row.getString("LastName")
        username = row.getString("Username")
        password = row.getString("Password")
        languageId = row.getInt("LanguageId")
    }
}
