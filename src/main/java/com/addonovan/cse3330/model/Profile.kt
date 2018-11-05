package com.addonovan.cse3330.model

import java.sql.ResultSet


data class Profile(
        val account: Account,
        val firstName: String,
        val lastName: String,
        val username: String,
        val password: String,
        val languageId: Int
) {

    companion object {
        fun fromRow(row: ResultSet) = Profile(
                Account.fromRow(row),
                row.getString("FirstName"),
                row.getString("LastName"),
                row.getString("Username"),
                row.getString("Password"),
                row.getInt("LanguageId")
        )
    }

    fun insert(): String =
            """
            INSERT INTO "Profile"
                (AccountId, FirstName, LastName, Username, Password, LanguageId)
            VALUES
                (${account.id}, $firstName, $lastName, $username, $password, $languageId);
            """.trimIndent()
}
