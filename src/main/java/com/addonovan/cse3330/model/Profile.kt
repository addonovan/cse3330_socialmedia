package com.addonovan.cse3330.model

import com.addonovan.cse3330.sql.execute
import java.sql.Connection
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

    override fun insertInto(connection: Connection): Boolean {
        // insert the base account first, which affects the value of the id
        // attribute
        super.insertInto(connection)

        connection.execute("""
            |INSERT INTO "Profile" (AccountId, FirstName, LastName, Username, Password, LanguageId)
            |VALUES (?, ?, ?, ?, ?, ?)
            |RETURNING AccountId
        """.trimMargin(), id, firstName, lastName, username, password, languageId)

        return true
    }
}
