package com.addonovan.cse3330.model

import java.sql.ResultSet

class Profile : Account() {

    lateinit var firstName: String

    lateinit var lastName: String

    lateinit var username: String

    lateinit var password: String

    var languageId: Int = 0

    override fun fromRow(row: ResultSet) {
        super.fromRow(row)
        firstName = row.getString("FirstName")
        lastName = row.getString("LastName")
        username = row.getString("Username")
        password = row.getString("Password")
        languageId = row.getInt("LanguageId")
    }

    override fun asInsert(): String =
            """
            INSERT INTO "Profile"
                (AccountId, FirstName, LastName, Username, Password, LanguageId)
            VALUES
                ($id, $firstName, $lastName, $username, $password, $languageId);
            """.trimIndent()
}
