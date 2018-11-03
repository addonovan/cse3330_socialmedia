package com.addonovan.cse3330.model


data class Profile(
        val account: Account,
        val firstName: String,
        val lastName: String,
        val username: String,
        val password: String,
        val languageId: Int
) {

    fun insert(): String =
            """
            INSERT INTO "Profile"
                (AccountId, FirstName, LastName, Username, Password, LanguageId)
            VALUES
                (${account.id}, $firstName, $lastName, $username, $password, $languageId);
            """.trimIndent()
}
